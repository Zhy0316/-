package com.example.archivemanagement.controller;

import com.example.archivemanagement.service.StudentTutorRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/relation")
public class StudentTutorRelationController {

    @Autowired
    private StudentTutorRelationService service;

    @PostMapping("/apply")
    public ResponseEntity<?> applyForTutor(@RequestBody Map<String, Long> request) {
        Long studentId = request.get("studentId");
        Long tutorId = request.get("tutorId");

        if (studentId == null || tutorId == null) {
            return ResponseEntity.badRequest().body("参数错误");
        }

        boolean result = service.applyForTutor(studentId, tutorId);
        if (result) {
            return ResponseEntity.ok("申请成功");
        } else {
            return ResponseEntity.badRequest().body("申请失败，可能已经存在绑定关系");
        }
    }

    @PutMapping("/audit")
    public ResponseEntity<?> auditRelation(@RequestBody Map<String, Object> request) {
        Long id = Long.parseLong(request.get("id").toString());
        Integer status = Integer.parseInt(request.get("status").toString());

        boolean result = service.auditRelation(id, status);
        if (result) {
            return ResponseEntity.ok("审核成功");
        } else {
            return ResponseEntity.badRequest().body("审核失败");
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentRelations(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentRelations(studentId));
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<?> getTutorRelations(@PathVariable Long tutorId) {
        return ResponseEntity.ok(service.getTutorRelations(tutorId));
    }

    @GetMapping("/tutor/{tutorId}/pending")
    public ResponseEntity<?> getTutorPendingRelations(@PathVariable Long tutorId) {
        return ResponseEntity.ok(service.getTutorPendingRelations(tutorId));
    }

    @GetMapping("/student/{studentId}/pending")
    public ResponseEntity<?> getStudentPendingRelations(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentPendingRelations(studentId));
    }
}
