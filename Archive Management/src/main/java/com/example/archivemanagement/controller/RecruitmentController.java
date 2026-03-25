package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.BusJobApplication;
import com.example.archivemanagement.entity.BusRecruitment;
import com.example.archivemanagement.service.RecruitmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 招聘与求职接口
 */
@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    // ==================== 招聘管理 ====================

    /**
     * 企业发布招聘
     * POST /api/recruitment/publish
     */
    @PostMapping("/api/recruitment/publish")
    public ResponseEntity<?> publish(@RequestBody BusRecruitment recruitment,
                                     HttpServletRequest request) {
        Long enterpriseId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(recruitmentService.publish(enterpriseId, recruitment));
    }

    /**
     * 公开浏览招聘列表（招聘中）
     * GET /api/recruitment/list
     */
    @GetMapping("/api/recruitment/list")
    public ResponseEntity<List<BusRecruitment>> list() {
        return ResponseEntity.ok(recruitmentService.listActive());
    }

    /**
     * 企业查看自己发布的招聘
     * GET /api/recruitment/my
     */
    @GetMapping("/api/recruitment/my")
    public ResponseEntity<List<BusRecruitment>> myRecruitments(HttpServletRequest request) {
        Long enterpriseId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(recruitmentService.listByEnterprise(enterpriseId));
    }

    /**
     * 招聘详情
     * GET /api/recruitment/{id}
     */
    @GetMapping("/api/recruitment/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        BusRecruitment r = recruitmentService.getById(id);
        if (r == null) return ResponseEntity.status(404).body("招聘信息不存在");
        return ResponseEntity.ok(r);
    }

    /**
     * 关闭招聘
     * PUT /api/recruitment/close/{id}
     */
    @PutMapping("/api/recruitment/close/{id}")
    public ResponseEntity<?> close(@PathVariable Long id, HttpServletRequest request) {
        Long enterpriseId = (Long) request.getAttribute("userId");
        if (recruitmentService.close(id, enterpriseId)) {
            return ResponseEntity.ok("招聘已关闭");
        }
        return ResponseEntity.badRequest().body("操作失败，无权限或招聘不存在");
    }

    // ==================== 求职申请 ====================

    /**
     * 学生投递简历
     * POST /api/job-application/apply
     * body: { recruitmentId, message(可选) }
     */
    @PostMapping("/api/job-application/apply")
    public ResponseEntity<?> apply(@RequestBody Map<String, Object> body,
                                   HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        Long recruitmentId = Long.valueOf(body.get("recruitmentId").toString());
        String message = body.containsKey("message") ? body.get("message").toString() : null;
        BusJobApplication app = recruitmentService.apply(studentId, recruitmentId, message);
        if (app == null) return ResponseEntity.badRequest().body("已投递过该职位");
        return ResponseEntity.ok(app);
    }

    /**
     * 学生查看自己的投递记录
     * GET /api/job-application/my
     */
    @GetMapping("/api/job-application/my")
    public ResponseEntity<List<Map<String, Object>>> myApplications(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(recruitmentService.myApplications(studentId));
    }

    /**
     * 企业查看某职位的投递列表
     * GET /api/job-application/list/{recruitmentId}
     */
    @GetMapping("/api/job-application/list/{recruitmentId}")
    public ResponseEntity<List<Map<String, Object>>> applicationList(@PathVariable Long recruitmentId) {
        return ResponseEntity.ok(recruitmentService.applicationsByRecruitment(recruitmentId));
    }

    /**
     * 企业更新投递状态
     * PUT /api/job-application/status
     * body: { applicationId, status }  1=有意向，2=不匹配
     */
    @PutMapping("/api/job-application/status")
    public ResponseEntity<?> updateStatus(@RequestBody Map<String, Object> body) {
        Long applicationId = Long.valueOf(body.get("applicationId").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        if (recruitmentService.updateApplicationStatus(applicationId, status)) {
            return ResponseEntity.ok("状态更新成功");
        }
        return ResponseEntity.badRequest().body("更新失败");
    }
}
