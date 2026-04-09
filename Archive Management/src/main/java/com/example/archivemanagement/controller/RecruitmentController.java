package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.BusJobApplication;
import com.example.archivemanagement.entity.BusRecruitment;
import com.example.archivemanagement.service.RecruitmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @PostMapping("/api/recruitment/publish")
    public Result<BusRecruitment> publish(@RequestBody BusRecruitment recruitment,
                                          HttpServletRequest request) {
        Long enterpriseId = (Long) request.getAttribute("userId");
        return Result.ok(recruitmentService.publish(enterpriseId, recruitment));
    }

    @GetMapping("/api/recruitment/list")
    public Result<List<BusRecruitment>> list() {
        return Result.ok(recruitmentService.listActive());
    }

    @GetMapping("/api/recruitment/my")
    public Result<List<BusRecruitment>> myRecruitments(HttpServletRequest request) {
        Long enterpriseId = (Long) request.getAttribute("userId");
        return Result.ok(recruitmentService.listByEnterprise(enterpriseId));
    }

    @GetMapping("/api/recruitment/{id}")
    public Result<BusRecruitment> detail(@PathVariable Long id) {
        BusRecruitment r = recruitmentService.getById(id);
        if (r == null) throw BusinessException.notFound("招聘信息不存在");
        return Result.ok(r);
    }

    @PutMapping("/api/recruitment/close/{id}")
    public Result<Void> close(@PathVariable Long id, HttpServletRequest request) {
        Long enterpriseId = (Long) request.getAttribute("userId");
        if (!recruitmentService.close(id, enterpriseId)) {
            throw new BusinessException("操作失败，无权限或招聘不存在");
        }
        return Result.ok("招聘已关闭");
    }

    @PostMapping("/api/job-application/apply")
    public Result<BusJobApplication> apply(@RequestBody Map<String, Object> body,
                                           HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        Long recruitmentId = Long.valueOf(body.get("recruitmentId").toString());
        String message = body.containsKey("message") ? body.get("message").toString() : null;
        BusJobApplication app = recruitmentService.apply(studentId, recruitmentId, message);
        if (app == null) throw new BusinessException("已投递过该职位");
        return Result.ok(app);
    }

    @GetMapping("/api/job-application/my")
    public Result<List<Map<String, Object>>> myApplications(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return Result.ok(recruitmentService.myApplications(studentId));
    }

    @GetMapping("/api/job-application/list/{recruitmentId}")
    public Result<List<Map<String, Object>>> applicationList(@PathVariable Long recruitmentId) {
        return Result.ok(recruitmentService.applicationsByRecruitment(recruitmentId));
    }

    @PutMapping("/api/job-application/status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> body) {
        Long applicationId = Long.valueOf(body.get("applicationId").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        if (!recruitmentService.updateApplicationStatus(applicationId, status)) {
            throw new BusinessException("更新失败");
        }
        return Result.ok("状态更新成功");
    }
}
