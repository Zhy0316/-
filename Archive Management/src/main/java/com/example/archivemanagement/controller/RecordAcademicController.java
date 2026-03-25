package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.RecordAcademic;
import com.example.archivemanagement.service.RecordAcademicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.math.BigDecimal;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
public class RecordAcademicController {

    @Autowired
    private RecordAcademicService service;

    @GetMapping("/{studentId}")
    public ResponseEntity<List<RecordAcademic>> getByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    @PostMapping("/upload/{studentId}")
    public ResponseEntity<?> uploadExcel(@PathVariable Long studentId, @RequestParam("file") MultipartFile file) {
        try {
            int count = service.importFromExcel(studentId, file);
            return ResponseEntity.ok(Map.of("message", "成绩导入成功, 共导入" + count + "条数据", "status", "success", "count", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "导入失败: " + e.getMessage(), "status", "error"));
        }
    }

    @PostMapping
    public ResponseEntity<RecordAcademic> add(@RequestBody Map<String, Object> request) {
        RecordAcademic record = mapRequestToRecord(request);
        return ResponseEntity.ok(service.addRecord(record));
    }

    @PutMapping
    public ResponseEntity<RecordAcademic> update(@RequestBody Map<String, Object> request) {
        RecordAcademic record = mapRequestToRecord(request);
        if (request.containsKey("id")) {
            record.setId(Long.parseLong(request.get("id").toString()));
        }
        return ResponseEntity.ok(service.updateRecord(record));
    }

    private RecordAcademic mapRequestToRecord(Map<String, Object> request) {
        RecordAcademic record = new RecordAcademic();
        
        // Map front-end fields to backend fields
        if (request.containsKey("studentId")) {
            record.setStudentId(Long.parseLong(request.get("studentId").toString()));
        }
        if (request.containsKey("semester")) {
            record.setAcademicYear(request.get("semester").toString());
        }
        if (request.containsKey("courseType")) {
            record.setCourseNature(request.get("courseType").toString());
        }
        if (request.containsKey("courseName")) {
            record.setCourseName(request.get("courseName").toString());
        }
        if (request.containsKey("credit")) {
            record.setCredit(new BigDecimal(request.get("credit").toString()));
        }
        if (request.containsKey("score")) {
            record.setScore(request.get("score").toString());
        }
        if (request.containsKey("gpaPoint")) {
            record.setGpa(new BigDecimal(request.get("gpaPoint").toString()));
        }
        if (request.containsKey("isRetake")) {
            int isRetake = Integer.parseInt(request.get("isRetake").toString());
            record.setIsInvalidated(isRetake == 1 ? "是" : "否");
        }
        
        // Calculate creditGpa if needed
        if (record.getCredit() != null && record.getGpa() != null) {
            record.setCreditGpa(record.getCredit().multiply(record.getGpa()));
        }
        
        return record;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteRecord(id);
        return ResponseEntity.ok().build();
    }
}
