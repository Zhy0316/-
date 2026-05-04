package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.archivemanagement.entity.*;
import com.example.archivemanagement.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导师工作台服务
 */
@Service
@RequiredArgsConstructor
public class TutorWorkbenchService {
    
    private final StudentTutorRelationMapper relationMapper;
    private final InfoStudentMapper studentMapper;
    private final RecordAcademicMapper academicMapper;
    private final RecordAwardMapper awardMapper;
    private final RecordDiaryMapper diaryMapper;
    private final TutorGuidanceRecordMapper guidanceMapper;
    private final StudentGoalMapper goalMapper;
    private final TutorAppointmentMapper appointmentMapper;
    
    /**
     * 获取导师的学生成长看板数据
     */
    public Map<String, Object> getStudentDashboard(Long tutorId) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // 1. 获取导师的学生列表
        List<Long> studentIds = relationMapper.selectStudentIdsByTutorId(tutorId);
        
        if (studentIds.isEmpty()) {
            dashboard.put("studentCount", 0);
            dashboard.put("students", new ArrayList<>());
            return dashboard;
        }
        
        // 2. 获取学生详细信息
        List<Map<String, Object>> students = new ArrayList<>();
        for (Long studentId : studentIds) {
            Map<String, Object> studentInfo = getStudentKeyIndicators(studentId);
            students.add(studentInfo);
        }
        
        // 3. 统计总体数据
        dashboard.put("studentCount", students.size());
        dashboard.put("students", students);
        dashboard.put("warningCount", students.stream().filter(s -> (Boolean) s.get("needsAttention")).count());
        dashboard.put("activeCount", students.stream().filter(s -> (Boolean) s.get("isActive")).count());
        
        return dashboard;
    }
    
    /**
     * 获取单个学生的关键指标
     */
    public Map<String, Object> getStudentKeyIndicators(Long studentId) {
        Map<String, Object> indicators = new HashMap<>();
        
        // 基本信息
        InfoStudent student = studentMapper.selectById(studentId);
        if (student != null) {
            indicators.put("studentId", studentId);
            indicators.put("studentName", student.getRealName());
            indicators.put("studentNo", student.getStudentNo());
            indicators.put("major", student.getMajor());
            indicators.put("grade", student.getGrade());
        }
        
        // 学业指标
        List<RecordAcademic> academics = academicMapper.selectByStudentId(studentId);
        BigDecimal avgGpa = calculateAverageGpa(academics);
        indicators.put("currentGpa", avgGpa);
        indicators.put("courseCount", academics.size());
        
        // 获奖数量
        QueryWrapper<RecordAward> awardWrapper = new QueryWrapper<>();
        awardWrapper.eq("student_id", studentId);
        long awardCount = awardMapper.selectCount(awardWrapper);
        indicators.put("awardCount", awardCount);
        
        // 日记更新频率(最近30天)
        QueryWrapper<RecordDiary> diaryWrapper = new QueryWrapper<>();
        diaryWrapper.eq("student_id", studentId);
        diaryWrapper.ge("create_time", LocalDateTime.now().minusDays(30));
        long recentDiaryCount = diaryMapper.selectCount(diaryWrapper);
        indicators.put("recentDiaryCount", recentDiaryCount);
        
        // 待审核事项数量(获奖待审核)
        QueryWrapper<RecordAward> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("student_id", studentId);
        pendingWrapper.eq("audit_status", "待审核");
        long pendingCount = awardMapper.selectCount(pendingWrapper);
        indicators.put("pendingCount", pendingCount);
        
        // 判断是否需要关注
        boolean needsAttention = false;
        List<String> warningReasons = new ArrayList<>();
        
        if (avgGpa.compareTo(new BigDecimal("2.5")) < 0) {
            needsAttention = true;
            warningReasons.add("GPA偏低");
        }
        if (recentDiaryCount == 0) {
            needsAttention = true;
            warningReasons.add("长期未更新日记");
        }
        
        indicators.put("needsAttention", needsAttention);
        indicators.put("warningReasons", warningReasons);
        indicators.put("isActive", recentDiaryCount > 0);
        
        return indicators;
    }
    
    /**
     * 计算平均GPA
     */
    private BigDecimal calculateAverageGpa(List<RecordAcademic> academics) {
        if (academics == null || academics.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalCreditGpa = academics.stream()
                .filter(a -> a.getCreditGpa() != null)
                .map(RecordAcademic::getCreditGpa)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredit = academics.stream()
                .filter(a -> a.getCredit() != null)
                .map(RecordAcademic::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalCredit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalCreditGpa.divide(totalCredit, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 识别预警学生
     */
    public List<Map<String, Object>> identifyWarningStudents(Long tutorId) {
        List<Long> studentIds = relationMapper.selectStudentIdsByTutorId(tutorId);
        
        return studentIds.stream()
                .map(this::getStudentKeyIndicators)
                .filter(s -> (Boolean) s.get("needsAttention"))
                .collect(Collectors.toList());
    }
    
    /**
     * 添加指导记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void addGuidanceRecord(TutorGuidanceRecord record) {
        guidanceMapper.insert(record);
    }
    
    /**
     * 获取指导记录列表
     */
    public List<TutorGuidanceRecord> getGuidanceRecords(Long tutorId, Long studentId) {
        if (studentId != null) {
            return guidanceMapper.selectByStudentId(studentId);
        }
        return guidanceMapper.selectByTutorId(tutorId);
    }
    
    /**
     * 获取指导统计
     */
    public Map<String, Object> getGuidanceStatistics(Long tutorId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<TutorGuidanceRecord> records = guidanceMapper.selectByTutorId(tutorId);
        stats.put("totalCount", records.size());
        
        // 按类型统计
        Map<String, Long> typeCount = records.stream()
                .collect(Collectors.groupingBy(TutorGuidanceRecord::getGuidanceType, Collectors.counting()));
        stats.put("typeDistribution", typeCount);
        
        // 总时长
        int totalDuration = records.stream()
                .filter(r -> r.getDuration() != null)
                .mapToInt(TutorGuidanceRecord::getDuration)
                .sum();
        stats.put("totalDuration", totalDuration);
        
        // 按学生统计
        List<Map<String, Object>> byStudent = guidanceMapper.countByTutorGroupByStudent(tutorId);
        stats.put("byStudent", byStudent);
        
        return stats;
    }
    
    /**
     * 创建学生目标
     */
    @Transactional(rollbackFor = Exception.class)
    public void createStudentGoal(StudentGoal goal) {
        goalMapper.insert(goal);
    }
    
    /**
     * 更新目标进度
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGoalProgress(Long goalId, Integer progress) {
        StudentGoal goal = goalMapper.selectById(goalId);
        if (goal != null) {
            goal.setProgress(progress);
            if (progress >= 100) {
                goal.setStatus(1); // 已完成
                goal.setCompleteTime(LocalDateTime.now());
            }
            goalMapper.updateById(goal);
        }
    }
    
    /**
     * 获取学生目标列表
     */
    public List<StudentGoal> getStudentGoals(Long studentId) {
        return goalMapper.selectByStudentId(studentId);
    }
    
    /**
     * 创建预约
     */
    @Transactional(rollbackFor = Exception.class)
    public void createAppointment(TutorAppointment appointment) {
        appointmentMapper.insert(appointment);
    }
    
    /**
     * 确认预约
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmAppointment(Long appointmentId) {
        TutorAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment != null) {
            appointment.setStatus(1); // 已确认
            appointmentMapper.updateById(appointment);
        }
    }
    
    /**
     * 获取待确认的预约
     */
    public List<TutorAppointment> getPendingAppointments(Long tutorId) {
        return appointmentMapper.selectPendingByTutorId(tutorId);
    }
    
    /**
     * 获取预约列表
     */
    public List<TutorAppointment> getAppointments(Long tutorId, Long studentId) {
        if (studentId != null) {
            return appointmentMapper.selectByStudentId(studentId);
        }
        return appointmentMapper.selectByTutorId(tutorId);
    }
}
