package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.dto.GrowthReportDTO;
import com.example.archivemanagement.entity.GrowthReport;
import com.example.archivemanagement.service.GrowthReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 成长报告控制器
 */
@RestController
@RequestMapping("/api/growth-report")
@RequiredArgsConstructor
public class GrowthReportController {
    
    private final GrowthReportService growthReportService;
    
    /**
     * 生成成长报告
     */
    @PostMapping("/generate")
    public Result<GrowthReportDTO> generateReport(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        
        Long studentId = (Long) request.getAttribute("userId");
        
        // 如果是管理员或导师，可以为其他学生生成报告
        if (params.containsKey("studentId")) {
            studentId = Long.valueOf(params.get("studentId").toString());
        }
        
        String reportType = params.get("reportType").toString(); // semester/year
        String termYear = params.get("termYear").toString();
        LocalDate startDate = LocalDate.parse(params.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(params.get("endDate").toString());
        
        GrowthReportDTO report = growthReportService.generateReport(
                studentId, reportType, termYear, startDate, endDate);
        
        return Result.success(report, "报告生成成功");
    }
    
    /**
     * 获取我的报告列表
     */
    @GetMapping("/my-reports")
    public Result<List<GrowthReport>> getMyReports(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        List<GrowthReport> reports = growthReportService.getReportList(studentId);
        return Result.success(reports, "标签添加成功");
    }
    
    /**
     * 获取指定学生的报告列表(导师/管理员)
     */
    @GetMapping("/student/{studentId}")
    public Result<List<GrowthReport>> getStudentReports(@PathVariable Long studentId) {
        List<GrowthReport> reports = growthReportService.getReportList(studentId);
        return Result.success(reports, "标签添加成功");
    }
    
    /**
     * 获取报告详情
     */
    @GetMapping("/{reportId}")
    public Result<GrowthReportDTO> getReportDetail(@PathVariable Long reportId) {
        GrowthReportDTO report = growthReportService.getReportDetail(reportId);
        if (report == null) {
            return Result.fail("报告不存在");
        }
        return Result.success(report, "获取报告详情成功");
    }
    
    /**
     * 删除报告
     */
    @DeleteMapping("/{reportId}")
    public Result<Void> deleteReport(@PathVariable Long reportId) {
        growthReportService.deleteReport(reportId);
        return Result.success(null, "删除成功");
    }
    
    /**
     * 快速生成当前学期报告
     */
    @PostMapping("/generate-current-semester")
    public Result<GrowthReportDTO> generateCurrentSemesterReport(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        
        // 自动计算当前学期
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        // 判断学期(9月-1月为第一学期，2月-8月为第二学期)
        String termYear;
        LocalDate startDate;
        LocalDate endDate;
        
        if (month >= 9 || month <= 1) {
            // 第一学期
            int startYear = month >= 9 ? year : year - 1;
            termYear = startYear + "-" + (startYear + 1) + "-1";
            startDate = LocalDate.of(startYear, 9, 1);
            endDate = LocalDate.of(startYear + 1, 1, 31);
        } else {
            // 第二学期
            termYear = (year - 1) + "-" + year + "-2";
            startDate = LocalDate.of(year, 2, 1);
            endDate = LocalDate.of(year, 8, 31);
        }
        
        GrowthReportDTO report = growthReportService.generateReport(
                studentId, "semester", termYear, startDate, endDate);
        
        return Result.success(report, "当前学期报告生成成功");
    }
}
