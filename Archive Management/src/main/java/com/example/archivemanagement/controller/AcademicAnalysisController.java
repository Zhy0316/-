package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.dto.AcademicHealthDTO;
import com.example.archivemanagement.service.AcademicAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学业分析 Controller
 */
@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicAnalysisController {
    
    private final AcademicAnalysisService analysisService;
    
    /**
     * 获取学业健康度报告
     */
    @GetMapping("/health-report")
    public Result<AcademicHealthDTO> getHealthReport(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        AcademicHealthDTO report = analysisService.getHealthReport(studentId);
        return Result.ok(report);
    }
    
    /**
     * 获取指定学生的学业健康度报告(导师/管理员)
     */
    @GetMapping("/health-report/{studentId}")
    public Result<AcademicHealthDTO> getStudentHealthReport(@PathVariable Long studentId) {
        AcademicHealthDTO report = analysisService.getHealthReport(studentId);
        return Result.ok(report);
    }
}
