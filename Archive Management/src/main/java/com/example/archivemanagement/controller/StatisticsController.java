package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/college-overview")
    public Result<Map<String, Object>> collegeOverview() {
        return Result.ok(statisticsService.getCollegeOverview());
    }

    @GetMapping("/student-growth/{studentId}")
    public Result<List<Map<String, Object>>> studentGrowth(@PathVariable Long studentId) {
        return Result.ok(statisticsService.getStudentGrowthTrend(studentId));
    }

    @GetMapping("/award-distribution")
    public Result<List<Map<String, Object>>> awardDistribution() {
        return Result.ok(statisticsService.getAwardDistribution());
    }

    @GetMapping("/gpa-distribution")
    public Result<Map<String, Object>> gpaDistribution() {
        return Result.ok(statisticsService.getGpaDistribution());
    }

    @GetMapping("/admin-dashboard")
    public Result<Map<String, Object>> adminDashboard() {
        return Result.ok(statisticsService.getAdminDashboard());
    }
}
