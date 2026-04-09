package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.service.AdminService;
import com.example.archivemanagement.service.EvalGrowthScoreService;
import com.example.archivemanagement.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final StatisticsService statisticsService;
    private final EvalGrowthScoreService growthScoreService;

    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleKey,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.listUsers(page, size, roleKey, keyword));
    }

    @PutMapping("/user/status")
    public Result<Void> updateUserStatus(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        if (status != 0 && status != 1) throw new com.example.archivemanagement.common.BusinessException("status 值无效（0=禁用，1=启用）");
        if (!adminService.updateUserStatus(userId, status)) throw com.example.archivemanagement.common.BusinessException.notFound("用户不存在");
        return Result.ok(status == 1 ? "用户已启用" : "用户已禁用");
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(statisticsService.getAdminDashboard());
    }

    @GetMapping("/growth-scores")
    public Result<List<Map<String, Object>>> growthScores() {
        return Result.ok(growthScoreService.getRanking());
    }
}
