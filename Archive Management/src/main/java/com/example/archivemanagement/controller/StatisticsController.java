package com.example.archivemanagement.controller;

import com.example.archivemanagement.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据统计接口
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 学院整体统计概览（学生数、平均绩点、获奖数等）
     * GET /api/stats/college-overview
     */
    @GetMapping("/college-overview")
    public ResponseEntity<Map<String, Object>> collegeOverview() {
        return ResponseEntity.ok(statisticsService.getCollegeOverview());
    }

    /**
     * 单个学生成长趋势数据（各学期综合分）
     * GET /api/stats/student-growth/{studentId}
     */
    @GetMapping("/student-growth/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> studentGrowth(@PathVariable Long studentId) {
        return ResponseEntity.ok(statisticsService.getStudentGrowthTrend(studentId));
    }

    /**
     * 获奖级别分布（饼图数据）
     * GET /api/stats/award-distribution
     */
    @GetMapping("/award-distribution")
    public ResponseEntity<List<Map<String, Object>>> awardDistribution() {
        return ResponseEntity.ok(statisticsService.getAwardDistribution());
    }

    /**
     * 绩点分布（柱状图数据）
     * GET /api/stats/gpa-distribution
     */
    @GetMapping("/gpa-distribution")
    public ResponseEntity<Map<String, Object>> gpaDistribution() {
        return ResponseEntity.ok(statisticsService.getGpaDistribution());
    }

    /**
     * 管理员首页统计（含各角色用户数）
     * GET /api/stats/admin-dashboard
     */
    @GetMapping("/admin-dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard() {
        return ResponseEntity.ok(statisticsService.getAdminDashboard());
    }
}
