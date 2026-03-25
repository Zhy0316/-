package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.RecordResearch;
import com.example.archivemanagement.service.RecordResearchService;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class RecordResearchController {

    private final RecordResearchService service;

    @GetMapping("/list/{studentId}")
    public ResponseEntity<List<RecordResearch>> getResearchList(@PathVariable Long studentId) {
        List<RecordResearch> researchList = service.getByStudentId(studentId);
        return ResponseEntity.ok(researchList);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadResearch(
            @RequestParam("studentId") Long studentId,
            @RequestParam("projectName") String projectName,
            @RequestParam("role") String role,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = FileUploadUtil.uploadFile(file, studentId, "research");
            RecordResearch research = new RecordResearch();
            research.setStudentId(studentId);
            research.setProjectName(projectName);
            research.setRole(role);
            research.setStartDate(java.sql.Date.valueOf(startDate));
            research.setEndDate(java.sql.Date.valueOf(endDate));
            research.setDescription(description);
            research.setResultFile(filePath);
            boolean result = service.save(research);
            if (result) {
                return ResponseEntity.ok("科研项目上传成功");
            } else {
                return ResponseEntity.badRequest().body("科研项目上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteResearch(@PathVariable Long id) {
        boolean result = service.removeById(id);
        if (result) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateResearch(@RequestBody RecordResearch research) {
        boolean result = service.updateById(research);
        if (result) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.badRequest().body("更新失败");
        }
    }
}
