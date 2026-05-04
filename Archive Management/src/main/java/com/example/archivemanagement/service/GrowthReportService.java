package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.archivemanagement.dto.GrowthReportDTO;
import com.example.archivemanagement.entity.*;
import com.example.archivemanagement.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成长报告服务
 */
@Service
@RequiredArgsConstructor
public class GrowthReportService {
    
    private final GrowthReportMapper growthReportMapper;
    private final GrowthReportTemplateMapper templateMapper;
    private final RecordAcademicMapper academicMapper;
    private final RecordAwardMapper awardMapper;
    private final RecordDiaryMapper diaryMapper;
    private final RecordPracticeMapper practiceMapper;
    private final RecordResearchMapper researchMapper;
    private final RecordPortfolioMapper portfolioMapper;
    private final DictAwardLevelMapper awardLevelMapper;
    private final DictDiaryTagMapper diaryTagMapper;
    private final DiaryTagRelationMapper diaryTagRelationMapper;
    private final InfoStudentMapper studentMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 生成成长报告
     */
    public GrowthReportDTO generateReport(Long studentId, String reportType, String termYear, 
                                          LocalDate startDate, LocalDate endDate) {
        // 1. 收集数据
        GrowthReportDTO reportDTO = new GrowthReportDTO();
        reportDTO.setStudentId(studentId);
        reportDTO.setReportType(reportType);
        reportDTO.setTermYear(termYear);
        reportDTO.setStartDate(startDate);
        reportDTO.setEndDate(endDate);
        
        // 获取学生信息
        InfoStudent student = studentMapper.selectById(studentId);
        if (student != null) {
            reportDTO.setStudentName(student.getRealName());
            reportDTO.setStudentNo(student.getStudentNo());
        }
        
        // 生成标题
        String title = generateTitle(reportType, termYear, student);
        reportDTO.setTitle(title);
        
        // 2. 生成各部分内容
        reportDTO.setAcademicPerformance(generateAcademicPerformance(studentId, startDate, endDate));
        reportDTO.setAbilityDevelopment(generateAbilityDevelopment(studentId, startDate, endDate));
        reportDTO.setAchievementSummary(generateAchievementSummary(studentId, startDate, endDate));
        reportDTO.setGrowthTrajectory(generateGrowthTrajectory(studentId, startDate, endDate));
        
        // 3. 生成建议
        reportDTO.setSuggestions(generateSuggestions(reportDTO));
        
        // 4. 生成摘要
        reportDTO.setSummary(generateSummary(reportDTO));
        
        // 5. 保存报告
        saveReport(reportDTO);
        
        return reportDTO;
    }
    
    /**
     * 生成学业表现
     */
    private GrowthReportDTO.AcademicPerformance generateAcademicPerformance(
            Long studentId, LocalDate startDate, LocalDate endDate) {
        
        GrowthReportDTO.AcademicPerformance performance = new GrowthReportDTO.AcademicPerformance();
        
        // 查询学业成绩
        List<RecordAcademic> academics = academicMapper.selectByStudentId(studentId);
        
        if (academics.isEmpty()) {
            performance.setSummary("暂无学业数据");
            return performance;
        }
        
        // 计算GPA
        BigDecimal totalGpa = BigDecimal.ZERO;
        int count = 0;
        List<String> excellentCourses = new ArrayList<>();
        List<String> riskCourses = new ArrayList<>();
        Map<String, Integer> courseTypeCount = new HashMap<>();
        
        for (RecordAcademic academic : academics) {
            if (academic.getGpa() != null) {
                totalGpa = totalGpa.add(academic.getGpa());
                count++;
                
                // 优秀课程(GPA >= 3.7)
                if (academic.getGpa().compareTo(new BigDecimal("3.7")) >= 0) {
                    excellentCourses.add(academic.getCourseName());
                }
                
                // 风险课程(GPA < 2.0)
                if (academic.getGpa().compareTo(new BigDecimal("2.0")) < 0) {
                    riskCourses.add(academic.getCourseName());
                }
            }
            
            // 课程类型统计
            String courseType = academic.getCourseNature() != null ? academic.getCourseNature() : "其他";
            courseTypeCount.put(courseType, courseTypeCount.getOrDefault(courseType, 0) + 1);
        }
        
        double avgGpa = count > 0 ? totalGpa.doubleValue() / count : 0;
        performance.setCurrentGpa(avgGpa);
        performance.setAvgGpa(avgGpa);
        performance.setExcellentCourses(excellentCourses);
        performance.setRiskCourses(riskCourses);
        
        // 课程类型分布
        List<Map<String, Object>> distribution = courseTypeCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", entry.getKey());
                    map.put("value", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        performance.setCourseDistribution(distribution);
        
        // 学分进度
        int totalCredits = academics.stream()
                .mapToInt(a -> a.getCredit() != null ? a.getCredit().intValue() : 0)
                .sum();
        Map<String, Object> creditProgress = new HashMap<>();
        creditProgress.put("completed", totalCredits);
        creditProgress.put("required", 160); // 假设毕业要求160学分
        creditProgress.put("percentage", totalCredits * 100.0 / 160);
        performance.setCreditProgress(creditProgress);
        
        // GPA趋势
        if (avgGpa >= 3.5) {
            performance.setGpaTrend("优秀");
        } else if (avgGpa >= 3.0) {
            performance.setGpaTrend("良好");
        } else if (avgGpa >= 2.5) {
            performance.setGpaTrend("中等");
        } else {
            performance.setGpaTrend("需要提升");
        }
        
        // 生成总结
        String summary = String.format("本周期内平均GPA为%.2f，完成学分%d分。" +
                "其中优秀课程%d门，需要关注的课程%d门。", 
                avgGpa, totalCredits, excellentCourses.size(), riskCourses.size());
        performance.setSummary(summary);
        
        return performance;
    }
    
    /**
     * 生成能力发展
     */
    private GrowthReportDTO.AbilityDevelopment generateAbilityDevelopment(
            Long studentId, LocalDate startDate, LocalDate endDate) {
        
        GrowthReportDTO.AbilityDevelopment development = new GrowthReportDTO.AbilityDevelopment();
        
        // 实践记录
        List<RecordPractice> practices = practiceMapper.selectByStudentId(studentId);
        development.setPracticeCount(practices.size());
        
        // 实践类型分布
        Map<String, Integer> practiceDistribution = new HashMap<>();
        practiceDistribution.put("企业实习", 0);
        practiceDistribution.put("志愿服务", 0);
        practiceDistribution.put("社团活动", 0);
        practiceDistribution.put("其他", practices.size());
        development.setPracticeDistribution(practiceDistribution);
        
        // 科研项目
        List<RecordResearch> researches = researchMapper.selectByStudentId(studentId);
        int researchCount = researches.size();
        development.setResearchCount(researchCount);
        
        // 作品集
        List<RecordPortfolio> portfolios = portfolioMapper.selectByStudentId(studentId);
        int portfolioCount = portfolios.size();
        development.setPortfolioCount(portfolioCount);
        
        // 技能标签(暂时为空,后续可扩展)
        development.setSkillTags(new ArrayList<>());
        development.setNewSkillCount(0);
        
        // 生成总结
        String summary = String.format("本周期内参与实践活动%d次，完成科研项目%d个，产出作品%d件。",
                practices.size(), researchCount, portfolioCount);
        development.setSummary(summary);
        
        return development;
    }
    
    /**
     * 生成荣誉成就
     */
    private GrowthReportDTO.AchievementSummary generateAchievementSummary(
            Long studentId, LocalDate startDate, LocalDate endDate) {
        
        GrowthReportDTO.AchievementSummary summary = new GrowthReportDTO.AchievementSummary();
        
        // 查询获奖记录
        List<RecordAward> awards = awardMapper.selectByStudentId(studentId);
        awards = awards.stream()
                .filter(a -> a.getAuditStatus() != null && a.getAuditStatus() == 1)
                .collect(Collectors.toList());
        
        summary.setTotalAwards(awards.size());
        
        // 计算总分
        int totalScore = awards.stream()
                .mapToInt(a -> a.getAwardScore() != null ? a.getAwardScore() : 0)
                .sum();
        summary.setTotalScore(totalScore);
        
        // 等级分布
        Map<String, Integer> levelDistribution = new HashMap<>();
        Map<Long, DictAwardLevel> levelMap = awardLevelMapper.selectList(null).stream()
                .collect(Collectors.toMap(DictAwardLevel::getId, level -> level));
        
        for (RecordAward award : awards) {
            if (award.getAwardLevelId() != null) {
                DictAwardLevel level = levelMap.get(award.getAwardLevelId());
                if (level != null) {
                    String levelName = level.getLevelName();
                    levelDistribution.put(levelName, levelDistribution.getOrDefault(levelName, 0) + 1);
                }
            }
        }
        summary.setLevelDistribution(levelDistribution);
        
        // 获奖列表
        List<Map<String, Object>> awardList = awards.stream()
                .limit(10) // 只显示前10个
                .map(award -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", award.getAwardName());
                    map.put("level", award.getAwardLevelId());
                    map.put("date", award.getAwardDate());
                    map.put("score", award.getAwardScore());
                    return map;
                })
                .collect(Collectors.toList());
        summary.setAwardList(awardList);
        
        // 同比增长(暂时设为0)
        summary.setGrowthRate("0%");
        
        // 生成总结
        String summaryText = String.format("本周期内共获得各类奖项%d项，累计获奖分数%d分。",
                awards.size(), totalScore);
        summary.setSummary(summaryText);
        
        return summary;
    }
    
    /**
     * 生成成长轨迹
     */
    private GrowthReportDTO.GrowthTrajectory generateGrowthTrajectory(
            Long studentId, LocalDate startDate, LocalDate endDate) {
        
        GrowthReportDTO.GrowthTrajectory trajectory = new GrowthReportDTO.GrowthTrajectory();
        
        // 查询日记
        List<RecordDiary> diaries = diaryMapper.selectByStudentId(studentId);
        
        // 关键里程碑(标记为里程碑的日记)
        List<Map<String, Object>> milestones = diaries.stream()
                .filter(d -> d.getIsMilestone() != null && d.getIsMilestone() == 1)
                .map(diary -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", diary.getRecordDate());
                    map.put("title", diary.getTitle());
                    map.put("content", diary.getContent());
                    return map;
                })
                .collect(Collectors.toList());
        trajectory.setMilestones(milestones);
        
        // 情绪分布统计
        Map<String, Integer> moodDistribution = new HashMap<>();
        for (RecordDiary diary : diaries) {
            if (diary.getMood() != null && !diary.getMood().isEmpty()) {
                moodDistribution.put(diary.getMood(), 
                        moodDistribution.getOrDefault(diary.getMood(), 0) + 1);
            }
        }
        trajectory.setMoodDistribution(moodDistribution);
        
        // 高频主题(通过标签统计)
        List<Map<String, Object>> topThemes = new ArrayList<>();
        trajectory.setTopThemes(topThemes);
        
        // 日记更新频率
        String frequency;
        if (diaries.size() >= 30) {
            frequency = "高频更新";
        } else if (diaries.size() >= 10) {
            frequency = "中等频率";
        } else {
            frequency = "较少更新";
        }
        trajectory.setDiaryFrequency(frequency);
        
        // 生成总结
        String summary = String.format("本周期内共记录成长日记%d篇，其中里程碑事件%d个。",
                diaries.size(), milestones.size());
        trajectory.setSummary(summary);
        
        return trajectory;
    }
    
    /**
     * 生成建议
     */
    private List<String> generateSuggestions(GrowthReportDTO reportDTO) {
        List<String> suggestions = new ArrayList<>();
        
        // 基于学业表现的建议
        GrowthReportDTO.AcademicPerformance academic = reportDTO.getAcademicPerformance();
        if (academic != null) {
            if (academic.getCurrentGpa() != null && academic.getCurrentGpa() < 3.0) {
                suggestions.add("建议加强学业投入，提升课程成绩，争取GPA达到3.0以上");
            }
            if (academic.getRiskCourses() != null && !academic.getRiskCourses().isEmpty()) {
                suggestions.add("关注薄弱课程，建议寻求导师或同学帮助，及时补足知识短板");
            }
        }
        
        // 基于能力发展的建议
        GrowthReportDTO.AbilityDevelopment ability = reportDTO.getAbilityDevelopment();
        if (ability != null) {
            if (ability.getPracticeCount() != null && ability.getPracticeCount() < 2) {
                suggestions.add("建议增加实践经历，可以参与企业实习、志愿服务或社团活动");
            }
            if (ability.getResearchCount() != null && ability.getResearchCount() == 0) {
                suggestions.add("可以尝试参与科研项目，培养科研思维和创新能力");
            }
        }
        
        // 基于获奖情况的建议
        GrowthReportDTO.AchievementSummary achievement = reportDTO.getAchievementSummary();
        if (achievement != null) {
            if (achievement.getTotalAwards() != null && achievement.getTotalAwards() < 3) {
                suggestions.add("建议积极参加各类竞赛和评优活动，丰富荣誉经历");
            }
        }
        
        // 基于成长轨迹的建议
        GrowthReportDTO.GrowthTrajectory growth = reportDTO.getGrowthTrajectory();
        if (growth != null) {
            if ("较少更新".equals(growth.getDiaryFrequency())) {
                suggestions.add("建议保持成长日记的记录习惯，定期反思和总结");
            }
        }
        
        // 通用建议
        suggestions.add("继续保持积极向上的学习态度，全面发展综合素质");
        
        return suggestions;
    }
    
    /**
     * 生成摘要
     */
    private String generateSummary(GrowthReportDTO reportDTO) {
        StringBuilder summary = new StringBuilder();
        
        summary.append(reportDTO.getStudentName()).append("同学在本周期内");
        
        // 学业
        if (reportDTO.getAcademicPerformance() != null) {
            Double gpa = reportDTO.getAcademicPerformance().getCurrentGpa();
            if (gpa != null) {
                summary.append("平均GPA为").append(String.format("%.2f", gpa)).append("，");
            }
        }
        
        // 获奖
        if (reportDTO.getAchievementSummary() != null) {
            Integer awards = reportDTO.getAchievementSummary().getTotalAwards();
            if (awards != null && awards > 0) {
                summary.append("获得各类奖项").append(awards).append("项，");
            }
        }
        
        // 实践
        if (reportDTO.getAbilityDevelopment() != null) {
            Integer practices = reportDTO.getAbilityDevelopment().getPracticeCount();
            if (practices != null && practices > 0) {
                summary.append("参与实践活动").append(practices).append("次，");
            }
        }
        
        summary.append("展现出良好的成长态势。");
        
        return summary.toString();
    }
    
    /**
     * 生成标题
     */
    private String generateTitle(String reportType, String termYear, InfoStudent student) {
        String studentName = student != null ? student.getRealName() : "学生";
        String typeName = "semester".equals(reportType) ? "学期" : "学年";
        return String.format("%s %s%s成长报告", studentName, termYear, typeName);
    }
    
    /**
     * 保存报告
     */
    private void saveReport(GrowthReportDTO reportDTO) {
        GrowthReport report = new GrowthReport();
        report.setStudentId(reportDTO.getStudentId());
        report.setReportType(reportDTO.getReportType());
        report.setTermYear(reportDTO.getTermYear());
        report.setStartDate(reportDTO.getStartDate());
        report.setEndDate(reportDTO.getEndDate());
        report.setTitle(reportDTO.getTitle());
        report.setSummary(reportDTO.getSummary());
        
        // 保存各部分总结
        if (reportDTO.getAcademicPerformance() != null) {
            report.setAcademicSummary(reportDTO.getAcademicPerformance().getSummary());
        }
        if (reportDTO.getAbilityDevelopment() != null) {
            report.setAbilitySummary(reportDTO.getAbilityDevelopment().getSummary());
        }
        if (reportDTO.getAchievementSummary() != null) {
            report.setAchievementSummary(reportDTO.getAchievementSummary().getSummary());
        }
        if (reportDTO.getGrowthTrajectory() != null) {
            report.setGrowthSummary(reportDTO.getGrowthTrajectory().getSummary());
        }
        
        // 保存建议
        if (reportDTO.getSuggestions() != null) {
            report.setSuggestions(String.join("\n", reportDTO.getSuggestions()));
        }
        
        // 保存完整数据为JSON
        try {
            String reportData = objectMapper.writeValueAsString(reportDTO);
            report.setReportData(reportData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        report.setStatus(1); // 已生成
        report.setViewCount(0);
        
        growthReportMapper.insert(report);
        reportDTO.setId(report.getId());
    }
    
    /**
     * 获取报告列表
     */
    public List<GrowthReport> getReportList(Long studentId) {
        LambdaQueryWrapper<GrowthReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrowthReport::getStudentId, studentId)
               .orderByDesc(GrowthReport::getCreateTime);
        return growthReportMapper.selectList(wrapper);
    }
    
    /**
     * 获取报告详情
     */
    public GrowthReportDTO getReportDetail(Long reportId) {
        GrowthReport report = growthReportMapper.selectById(reportId);
        if (report == null) {
            return null;
        }
        
        // 增加查看次数
        report.setViewCount(report.getViewCount() + 1);
        growthReportMapper.updateById(report);
        
        // 解析JSON数据
        try {
            if (report.getReportData() != null) {
                return objectMapper.readValue(report.getReportData(), GrowthReportDTO.class);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 删除报告
     */
    public void deleteReport(Long reportId) {
        growthReportMapper.deleteById(reportId);
    }
}
