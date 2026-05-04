package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.InfoTutor;
import com.example.archivemanagement.entity.StudentGoal;
import com.example.archivemanagement.entity.TutorAppointment;
import com.example.archivemanagement.entity.TutorGuidanceRecord;
import com.example.archivemanagement.mapper.InfoTutorMapper;
import com.example.archivemanagement.service.TutorWorkbenchService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 导师工作台控制器
 */
@RestController
@RequestMapping("/api/tutor-workbench")
@RequiredArgsConstructor
public class TutorWorkbenchController {
    
    private final TutorWorkbenchService workbenchService;
    private final InfoTutorMapper tutorMapper;
    
    /**
     * 获取学生成长看板
     */
    @GetMapping("/dashboard/{tutorId}")
    public Result<Map<String, Object>> getStudentDashboard(@PathVariable Long tutorId) {
        Map<String, Object> dashboard = workbenchService.getStudentDashboard(tutorId);
        return Result.success(dashboard, "标签添加成功");
    }
    
    /**
     * 获取单个学生的关键指标
     */
    @GetMapping("/student-indicators/{studentId}")
    public Result<Map<String, Object>> getStudentIndicators(@PathVariable Long studentId) {
        Map<String, Object> indicators = workbenchService.getStudentKeyIndicators(studentId);
        return Result.success(indicators, "标签添加成功");
    }
    
    /**
     * 识别预警学生
     */
    @GetMapping("/warning-students/{tutorId}")
    public Result<List<Map<String, Object>>> getWarningStudents(@PathVariable Long tutorId) {
        List<Map<String, Object>> students = workbenchService.identifyWarningStudents(tutorId);
        return Result.success(students, "标签添加成功");
    }
    
    /**
     * 添加指导记录
     */
    @PostMapping("/guidance-record")
    public Result<Void> addGuidanceRecord(@RequestBody TutorGuidanceRecord record) {
        workbenchService.addGuidanceRecord(record);
        return Result.success(null, "指导记录添加成功");
    }
    
    /**
     * 获取指导记录列表
     */
    @GetMapping("/guidance-records")
    public Result<List<TutorGuidanceRecord>> getGuidanceRecords(
            @RequestParam Long tutorId,
            @RequestParam(required = false) Long studentId) {
        List<TutorGuidanceRecord> records = workbenchService.getGuidanceRecords(tutorId, studentId);
        return Result.success(records, "标签添加成功");
    }
    
    /**
     * 获取指导统计
     */
    @GetMapping("/guidance-statistics/{tutorId}")
    public Result<Map<String, Object>> getGuidanceStatistics(@PathVariable Long tutorId) {
        Map<String, Object> stats = workbenchService.getGuidanceStatistics(tutorId);
        return Result.success(stats, "标签添加成功");
    }
    
    /**
     * 创建学生目标
     */
    @PostMapping("/student-goal")
    public Result<Void> createStudentGoal(@RequestBody StudentGoal goal) {
        workbenchService.createStudentGoal(goal);
        return Result.success(null, "目标创建成功");
    }
    
    /**
     * 更新目标进度
     */
    @PutMapping("/student-goal/{goalId}/progress")
    public Result<Void> updateGoalProgress(
            @PathVariable Long goalId,
            @RequestParam Integer progress) {
        workbenchService.updateGoalProgress(goalId, progress);
        return Result.success(null, "进度更新成功");
    }
    
    /**
     * 获取学生目标列表
     */
    @GetMapping("/student-goals/{studentId}")
    public Result<List<StudentGoal>> getStudentGoals(@PathVariable Long studentId) {
        List<StudentGoal> goals = workbenchService.getStudentGoals(studentId);
        return Result.success(goals, "标签添加成功");
    }
    
    /**
     * 创建预约
     */
    @PostMapping("/appointment")
    public Result<Void> createAppointment(@RequestBody TutorAppointment appointment) {
        workbenchService.createAppointment(appointment);
        return Result.success(null, "预约创建成功");
    }
    
    /**
     * 确认预约
     */
    @PutMapping("/appointment/{appointmentId}/confirm")
    public Result<Void> confirmAppointment(@PathVariable Long appointmentId) {
        workbenchService.confirmAppointment(appointmentId);
        return Result.success(null, "预约已确认");
    }
    
    /**
     * 获取待确认的预约
     */
    @GetMapping("/appointments/pending/{tutorId}")
    public Result<List<TutorAppointment>> getPendingAppointments(@PathVariable Long tutorId) {
        List<TutorAppointment> appointments = workbenchService.getPendingAppointments(tutorId);
        return Result.success(appointments, "标签添加成功");
    }
    
    /**
     * 获取预约列表
     */
    @GetMapping("/appointments")
    public Result<List<TutorAppointment>> getAppointments(
            @RequestParam Long tutorId,
            @RequestParam(required = false) Long studentId) {
        List<TutorAppointment> appointments = workbenchService.getAppointments(tutorId, studentId);
        return Result.success(appointments, "标签添加成功");
    }

    /**
     * 上传导师头像
     */
    @PostMapping("/avatar/upload")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                   HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        String filePath = FileUploadUtil.uploadFile(file, userId, "avatar");
        InfoTutor tutor = tutorMapper.selectByUserId(userId);
        if (tutor != null) {
            tutor.setAvatar(filePath);
            tutorMapper.updateById(tutor);
        }
        return Result.success(Map.of("filePath", filePath));
    }
}
