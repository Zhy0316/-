package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.StudentTutorRelation;
import com.example.archivemanagement.service.StudentTutorRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relation")
@RequiredArgsConstructor
public class StudentTutorRelationController {

    private final StudentTutorRelationService service;

    @PostMapping("/apply")
    public Result<Void> applyForTutor(@RequestBody Map<String, Long> request) {
        Long studentId = request.get("studentId");
        Long tutorId = request.get("tutorId");
        if (studentId == null || tutorId == null) throw new BusinessException("参数错误");
        if (!service.applyForTutor(studentId, tutorId)) throw new BusinessException("申请失败，已存在绑定关系");
        return Result.ok("申请成功");
    }

    @PutMapping("/audit")
    public Result<Void> auditRelation(@RequestBody Map<String, Object> request) {
        Long id = Long.parseLong(request.get("id").toString());
        Integer status = Integer.parseInt(request.get("status").toString());
        if (!service.auditRelation(id, status)) throw new BusinessException("审核失败");
        return Result.ok("审核成功");
    }

    @GetMapping("/student/{studentId}")
    public Result<List<StudentTutorRelation>> getStudentRelations(@PathVariable Long studentId) {
        return Result.ok(service.getStudentRelations(studentId));
    }

    @GetMapping("/tutor/{tutorId}")
    public Result<List<StudentTutorRelation>> getTutorRelations(@PathVariable Long tutorId) {
        return Result.ok(service.getTutorRelations(tutorId));
    }

    @GetMapping("/tutor/{tutorId}/pending")
    public Result<List<StudentTutorRelation>> getTutorPendingRelations(@PathVariable Long tutorId) {
        return Result.ok(service.getTutorPendingRelations(tutorId));
    }

    @GetMapping("/student/{studentId}/pending")
    public Result<List<StudentTutorRelation>> getStudentPendingRelations(@PathVariable Long studentId) {
        return Result.ok(service.getStudentPendingRelations(studentId));
    }
}
