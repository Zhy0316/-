package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.dto.StudentProfileDTO;
import com.example.archivemanagement.entity.StudentTutorRelation;
import com.example.archivemanagement.service.InfoStudentService;
import com.example.archivemanagement.service.StudentTutorRelationService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relation")
@RequiredArgsConstructor
public class StudentTutorRelationController {

    private final StudentTutorRelationService service;
    private final InfoStudentService studentService;

    /**
     * 申请绑定导师（支持自荐信 + 附件上传）
     * POST /api/relation/apply  multipart/form-data
     */
    @PostMapping("/apply")
    public Result<Void> applyForTutor(
            @RequestParam Long tutorId,
            @RequestParam(required = false) String applyNote,
            @RequestParam(required = false) MultipartFile attachFile,
            HttpServletRequest request) throws Exception {

        Long studentId = (Long) request.getAttribute("userId");
        if (studentId == null) {
            // 兼容旧 JSON 调用（前端传 studentId）
            throw new BusinessException("请先登录");
        }

        String filePath = null;
        if (attachFile != null && !attachFile.isEmpty()) {
            filePath = FileUploadUtil.uploadFile(attachFile, studentId, "apply");
        }

        if (!service.applyForTutorWithNote(studentId, tutorId, applyNote, filePath)) {
            throw new BusinessException("申请失败，已存在绑定关系");
        }
        return Result.ok("申请成功");
    }

    /**
     * 兼容旧 JSON 格式申请（无附件）
     */
    @PostMapping("/apply-json")
    public Result<Void> applyJson(@RequestBody Map<String, Object> body,
                                  HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        Long tutorId = Long.valueOf(body.get("tutorId").toString());
        String applyNote = body.containsKey("applyNote") ? body.get("applyNote").toString() : null;
        if (!service.applyForTutorWithNote(studentId, tutorId, applyNote, null)) {
            throw new BusinessException("申请失败，已存在绑定关系");
        }
        return Result.ok("申请成功");
    }

    @PutMapping("/audit")
    public Result<Void> auditRelation(@RequestBody Map<String, Object> request) {
        Long id = Long.parseLong(request.get("id").toString());
        Integer status = Integer.parseInt(request.get("status").toString());
        String rejectNote = request.containsKey("rejectNote") ? request.get("rejectNote").toString() : null;
        if (!service.auditRelation(id, status, rejectNote)) throw new BusinessException("审核失败");
        return Result.ok("审核成功");
    }
    
    /**
     * 学生重新提交申请（可以修改自荐信和附件）
     */
    @PutMapping("/resubmit/{id}")
    public Result<Void> resubmitRelation(
            @PathVariable Long id,
            @RequestParam(required = false) String applyNote,
            @RequestParam(required = false) MultipartFile attachFile,
            HttpServletRequest request) throws Exception {
        Long studentId = (Long) request.getAttribute("userId");
        String filePath = null;
        if (attachFile != null && !attachFile.isEmpty()) {
            filePath = FileUploadUtil.uploadFile(attachFile, studentId, "apply");
        }
        if (!service.resubmitRelation(id, applyNote, filePath)) throw new BusinessException("重新提交失败");
        return Result.ok("重新提交成功");
    }

    @GetMapping("/student/{studentId}")
    public Result<List<StudentTutorRelation>> getStudentRelations(@PathVariable Long studentId) {
        return Result.ok(service.getStudentRelations(studentId));
    }

    @GetMapping("/tutor/{tutorId}")
    public Result<List<StudentTutorRelation>> getTutorRelations(@PathVariable Long tutorId) {
        return Result.ok(service.getTutorRelations(tutorId));
    }

    /**
     * 导师查看待审核申请（含学生基本信息 + 自荐信）
     * GET /api/relation/tutor/{tutorId}/pending-detail
     */
    @GetMapping("/tutor/{tutorId}/pending-detail")
    public Result<List<Map<String, Object>>> getPendingWithDetail(@PathVariable Long tutorId) {
        return Result.ok(service.getPendingWithStudentInfo(tutorId));
    }

    @GetMapping("/tutor/{tutorId}/pending")
    public Result<List<StudentTutorRelation>> getTutorPendingRelations(@PathVariable Long tutorId) {
        return Result.ok(service.getTutorPendingRelations(tutorId));
    }

    @GetMapping("/student/{studentId}/pending")
    public Result<List<StudentTutorRelation>> getStudentPendingRelations(@PathVariable Long studentId) {
        return Result.ok(service.getStudentPendingRelations(studentId));
    }

    /**
     * 导师查看某学生基本信息预览（绑定前）
     * GET /api/relation/student-preview/{studentId}
     */
    @GetMapping("/student-preview/{studentId}")
    public Result<StudentProfileDTO> studentPreview(@PathVariable Long studentId) {
        StudentProfileDTO profile = studentService.getStudentProfile(studentId);
        if (profile == null) throw BusinessException.notFound("学生信息不存在");
        // 隐藏敏感字段（手机号脱敏）
        if (profile.getPhone() != null && profile.getPhone().length() >= 7) {
            profile.setPhone(profile.getPhone().substring(0, 3) + "****" + profile.getPhone().substring(7));
        }
        return Result.ok(profile);
    }
}
