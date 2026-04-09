package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.RecordResearch;
import com.example.archivemanagement.service.RecordResearchService;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class RecordResearchController {

    private final RecordResearchService service;

    @GetMapping("/list/{studentId}")
    public Result<List<RecordResearch>> getResearchList(@PathVariable Long studentId) {
        return Result.ok(service.getByStudentId(studentId));
    }

    @PostMapping("/upload")
    public Result<Void> uploadResearch(
            @RequestParam("studentId") Long studentId,
            @RequestParam("projectName") String projectName,
            @RequestParam("role") String role,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        RecordResearch research = new RecordResearch();
        research.setStudentId(studentId);
        research.setProjectName(projectName);
        research.setRole(role);
        research.setStartDate(java.sql.Date.valueOf(startDate));
        research.setEndDate(java.sql.Date.valueOf(endDate));
        research.setDescription(description);
        if (file != null && !file.isEmpty()) {
            research.setResultFile(FileUploadUtil.uploadFile(file, studentId, "research"));
        }
        if (!service.save(research)) throw new BusinessException("科研项目保存失败");
        return Result.ok("科研项目上传成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteResearch(@PathVariable Long id) {
        if (!service.removeById(id)) throw new BusinessException("删除失败");
        return Result.ok("删除成功");
    }

    @PutMapping("/update")
    public Result<Void> updateResearch(@RequestBody RecordResearch research) {
        if (!service.updateById(research)) throw new BusinessException("更新失败");
        return Result.ok("更新成功");
    }
}
