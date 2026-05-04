package com.example.archivemanagement.service;

import com.example.archivemanagement.dto.AcademicHealthDTO;
import com.example.archivemanagement.entity.RecordAcademic;
import com.example.archivemanagement.mapper.RecordAcademicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学业分析服务
 */
@Service
@RequiredArgsConstructor
public class AcademicAnalysisService {
    
    private final RecordAcademicMapper academicMapper;
    
    /**
     * 获取学业健康度报告
     */
    public AcademicHealthDTO getHealthReport(Long studentId) {
        AcademicHealthDTO report = new AcademicHealthDTO();
        
        // 查询所有有效成绩
        List<RecordAcademic> records = academicMapper.selectByStudentId(studentId);
        
        if (records == null || records.isEmpty()) {
            return report;
        }
        
        // 过滤掉无效成绩(重修前的成绩)
        records = records.stream()
                .filter(r -> !"是".equals(r.getIsInvalidated()))
                .sorted(Comparator.comparing(RecordAcademic::getAcademicYear))
                .collect(Collectors.toList());
        
        if (records.isEmpty()) {
            return report;
        }
        
        // 1. 计算GPA统计
        calculateGPAStats(report, records);
        
        // 2. 计算学分进度
        calculateCreditProgress(report, records);
        
        // 3. 课程类型分布
        calculateCourseTypeStats(report, records);
        
        // 4. GPA趋势
        calculateGPATrends(report, records);
        
        // 5. 风险课程
        findRiskCourses(report, records);
        
        // 6. 生成学业建议
        generateSuggestions(report, records);
        
        return report;
    }
    
    /**
     * 计算GPA统计
     */
    private void calculateGPAStats(AcademicHealthDTO report, List<RecordAcademic> records) {
        // 计算加权平均GPA
        BigDecimal totalCreditGPA = records.stream()
                .map(r -> r.getCreditGpa() != null ? r.getCreditGpa() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredit = records.stream()
                .map(r -> r.getCredit() != null ? r.getCredit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalCredit.compareTo(BigDecimal.ZERO) > 0) {
            report.setAvgGPA(totalCreditGPA.divide(totalCredit, 2, RoundingMode.HALF_UP));
        } else {
            report.setAvgGPA(BigDecimal.ZERO);
        }
        
        // 当前学期GPA(最新学年)
        String latestYear = records.stream()
                .map(RecordAcademic::getAcademicYear)
                .max(Comparator.naturalOrder())
                .orElse("");
        
        List<RecordAcademic> currentTermRecords = records.stream()
                .filter(r -> latestYear.equals(r.getAcademicYear()))
                .collect(Collectors.toList());
        
        if (!currentTermRecords.isEmpty()) {
            BigDecimal currentCreditGPA = currentTermRecords.stream()
                    .map(r -> r.getCreditGpa() != null ? r.getCreditGpa() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal currentCredit = currentTermRecords.stream()
                    .map(r -> r.getCredit() != null ? r.getCredit() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (currentCredit.compareTo(BigDecimal.ZERO) > 0) {
                report.setCurrentGPA(currentCreditGPA.divide(currentCredit, 2, RoundingMode.HALF_UP));
            }
        }
        
        // 最高和最低GPA
        report.setMaxGPA(records.stream()
                .map(r -> r.getGpa() != null ? r.getGpa() : BigDecimal.ZERO)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));
        
        report.setMinGPA(records.stream()
                .map(r -> r.getGpa() != null ? r.getGpa() : BigDecimal.ZERO)
                .filter(gpa -> gpa.compareTo(BigDecimal.ZERO) > 0)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));
    }
    
    /**
     * 计算学分进度
     */
    private void calculateCreditProgress(AcademicHealthDTO report, List<RecordAcademic> records) {
        AcademicHealthDTO.CreditProgress progress = new AcademicHealthDTO.CreditProgress();
        
        BigDecimal completed = records.stream()
                .map(r -> r.getCredit() != null ? r.getCredit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        progress.setCompleted(completed);
        progress.setRequired(new BigDecimal("160")); // 假设毕业要求160学分
        
        if (progress.getRequired().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = completed.divide(progress.getRequired(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            progress.setPercentage(percentage.setScale(2, RoundingMode.HALF_UP));
        }
        
        report.setCreditProgress(progress);
    }
    
    /**
     * 课程类型分布统计
     */
    private void calculateCourseTypeStats(AcademicHealthDTO report, List<RecordAcademic> records) {
        Map<String, List<RecordAcademic>> groupedByType = records.stream()
                .collect(Collectors.groupingBy(r -> 
                    r.getCourseNature() != null ? r.getCourseNature() : "其他"
                ));
        
        List<AcademicHealthDTO.CourseTypeStats> stats = new ArrayList<>();
        
        for (Map.Entry<String, List<RecordAcademic>> entry : groupedByType.entrySet()) {
            AcademicHealthDTO.CourseTypeStats stat = new AcademicHealthDTO.CourseTypeStats();
            stat.setCourseType(entry.getKey());
            stat.setCount(entry.getValue().size());
            
            // 计算该类型课程的平均GPA
            BigDecimal totalCreditGPA = entry.getValue().stream()
                    .map(r -> r.getCreditGpa() != null ? r.getCreditGpa() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalCredit = entry.getValue().stream()
                    .map(r -> r.getCredit() != null ? r.getCredit() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (totalCredit.compareTo(BigDecimal.ZERO) > 0) {
                stat.setAvgGPA(totalCreditGPA.divide(totalCredit, 2, RoundingMode.HALF_UP));
            } else {
                stat.setAvgGPA(BigDecimal.ZERO);
            }
            
            stat.setTotalCredit(totalCredit);
            stats.add(stat);
        }
        
        report.setCourseTypeStats(stats);
    }
    
    /**
     * 计算GPA趋势
     */
    private void calculateGPATrends(AcademicHealthDTO report, List<RecordAcademic> records) {
        Map<String, List<RecordAcademic>> groupedByYear = records.stream()
                .collect(Collectors.groupingBy(RecordAcademic::getAcademicYear));
        
        List<AcademicHealthDTO.GPATrend> trends = new ArrayList<>();
        
        for (Map.Entry<String, List<RecordAcademic>> entry : groupedByYear.entrySet()) {
            AcademicHealthDTO.GPATrend trend = new AcademicHealthDTO.GPATrend();
            trend.setTerm(entry.getKey());
            
            BigDecimal totalCreditGPA = entry.getValue().stream()
                    .map(r -> r.getCreditGpa() != null ? r.getCreditGpa() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalCredit = entry.getValue().stream()
                    .map(r -> r.getCredit() != null ? r.getCredit() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (totalCredit.compareTo(BigDecimal.ZERO) > 0) {
                trend.setGpa(totalCreditGPA.divide(totalCredit, 2, RoundingMode.HALF_UP));
            } else {
                trend.setGpa(BigDecimal.ZERO);
            }
            
            trend.setCredit(totalCredit);
            trends.add(trend);
        }
        
        // 按学年排序
        trends.sort(Comparator.comparing(AcademicHealthDTO.GPATrend::getTerm));
        report.setGpaTrends(trends);
    }
    
    /**
     * 查找风险课程
     */
    private void findRiskCourses(AcademicHealthDTO report, List<RecordAcademic> records) {
        List<AcademicHealthDTO.RiskCourse> riskCourses = records.stream()
                .filter(r -> r.getGpa() != null && r.getGpa().compareTo(new BigDecimal("2.0")) < 0)
                .map(r -> {
                    AcademicHealthDTO.RiskCourse risk = new AcademicHealthDTO.RiskCourse();
                    risk.setCourseName(r.getCourseName());
                    risk.setAcademicYear(r.getAcademicYear());
                    risk.setGpa(r.getGpa());
                    risk.setScore(r.getScore());
                    return risk;
                })
                .collect(Collectors.toList());
        
        report.setRiskCourses(riskCourses);
    }
    
    /**
     * 生成学业建议
     */
    private void generateSuggestions(AcademicHealthDTO report, List<RecordAcademic> records) {
        List<String> suggestions = new ArrayList<>();
        
        // 基于GPA给建议
        if (report.getAvgGPA() != null) {
            if (report.getAvgGPA().compareTo(new BigDecimal("3.5")) >= 0) {
                suggestions.add("学业表现优秀,继续保持!");
            } else if (report.getAvgGPA().compareTo(new BigDecimal("3.0")) >= 0) {
                suggestions.add("学业表现良好,可以尝试挑战更高难度的课程");
            } else if (report.getAvgGPA().compareTo(new BigDecimal("2.5")) >= 0) {
                suggestions.add("学业表现中等,建议加强薄弱科目的学习");
            } else {
                suggestions.add("学业表现需要提升,建议寻求导师或同学的帮助");
            }
        }
        
        // 基于学分进度给建议
        if (report.getCreditProgress() != null && report.getCreditProgress().getPercentage() != null) {
            BigDecimal percentage = report.getCreditProgress().getPercentage();
            if (percentage.compareTo(new BigDecimal("75")) < 0) {
                suggestions.add("学分进度偏慢,建议合理安排选课计划");
            }
        }
        
        // 基于风险课程给建议
        if (report.getRiskCourses() != null && !report.getRiskCourses().isEmpty()) {
            suggestions.add("有" + report.getRiskCourses().size() + "门课程成绩偏低,建议重点复习");
        }
        
        // 基于课程类型给建议
        if (report.getCourseTypeStats() != null) {
            for (AcademicHealthDTO.CourseTypeStats stat : report.getCourseTypeStats()) {
                if (stat.getAvgGPA() != null && stat.getAvgGPA().compareTo(new BigDecimal("2.5")) < 0) {
                    suggestions.add(stat.getCourseType() + "类课程表现较弱,建议加强该类课程的学习");
                }
            }
        }
        
        report.setSuggestions(suggestions);
    }
}
