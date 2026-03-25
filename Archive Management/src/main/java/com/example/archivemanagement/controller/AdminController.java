package com.example.archivemanagement.controller;

import com.example.archivemanagement.service.AdminService;
import com.example.archivemanagement.service.EvalGrowthScoreService;
import com.example.archivemanagement.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员接口（所有接口需 admin 角色，由 AuthInterceptor 统一拦截）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final StatisticsService statisticsService;
    private final EvalGrowthScoreService growthScoreService;

    /**
     * 用户列表（分页 + 按角色/关键字筛选）
     * GET /api/admin/users?page=1&size=10&roleKey=student&keyword=张
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleKey,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(adminService.listUsers(page, size, roleKey, keyword));
    }

    /**
     * 启用/禁用用户
     * PUT /api/admin/user/status
     * body: { "userId": 1, "status": 0 }
     */
    @PutMapping("/user/status")
    public ResponseEntity<?> updateUserStatus(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        if (status != 0 && status != 1) {
            return ResponseEntity.badRequest().body("status 值无效（0=禁用，1=启用）");
        }
        if (adminService.updateUserStatus(userId, status)) {
            return ResponseEntity.ok(status == 1 ? "用户已启用" : "用户已禁用");
        }
        return ResponseEntity.badRequest().body("操作失败，用户不存在");
    }

    /**
     * 管理员首页统计数据
     * GET /api/admin/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(statisticsService.getAdminDashboard());
    }

    /**
     * 全院成长分排行榜
     * GET /api/admin/growth-scores
     */
    @GetMapping("/growth-scores")
    public ResponseEntity<List<Map<String, Object>>> growthScores() {
        return ResponseEntity.ok(growthScoreService.getRanking());
    }
}
