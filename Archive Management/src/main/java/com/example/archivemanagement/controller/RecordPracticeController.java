package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.RecordPractice;
import com.example.archivemanagement.service.RecordPracticeService;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class RecordPracticeController {

    private final RecordPracticeService recordPracticeService;

    @PostMapping("/upload")
    public Result<Void> uploadPractice(
            @RequestParam("studentId") Long studentId,
            @RequestParam("activityName") String activityName,
            @RequestParam("organization") String organization,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        RecordPractice practice = new RecordPractice();
        practice.setStudentId(studentId);
        practice.setActivityName(activityName);
        practice.setOrganization(organization);
        practice.setStartDate(java.sql.Date.valueOf(startDate));
        practice.setEndDate(java.sql.Date.valueOf(endDate));
        practice.setContent(content);
        if (file != null && !file.isEmpty()) {
            practice.setProofFile(FileUploadUtil.uploadFile(file, studentId, "practice"));
        }
        if (!recordPracticeService.save(practice)) throw new BusinessException("社会实践保存失败");
        return Result.ok("社会实践上传成功");
    }

    @GetMapping("/list/{studentId}")
    public Result<List<RecordPractice>> getPracticeList(@PathVariable Long studentId) {
        return Result.ok(recordPracticeService.getByStudentId(studentId));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deletePractice(@PathVariable Long id) {
        if (!recordPracticeService.removeById(id)) throw new BusinessException("删除失败");
        return Result.ok("删除成功");
    }

    @PutMapping("/update")
    public Result<Void> updatePractice(@RequestBody RecordPractice practice) {
        if (!recordPracticeService.updateById(practice)) throw new BusinessException("更新失败");
        return Result.ok("更新成功");
    }

    /** 在线预览证明材料（保留 ResponseEntity 以便设置 Content-Type） */
    @GetMapping("/preview/{id}")
    public ResponseEntity<FileSystemResource> previewFile(@PathVariable Long id) {
        RecordPractice practice = recordPracticeService.getById(id);
        if (practice == null || practice.getProofFile() == null) {
            return ResponseEntity.notFound().build();
        }
        String absolutePath = FileUploadUtil.getBaseUploadDir() + practice.getProofFile().substring(9);
        File file = new File(absolutePath);
        if (!file.exists()) return ResponseEntity.notFound().build();

        FileSystemResource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");
        String name = file.getName().toLowerCase();
        MediaType mediaType = name.endsWith(".pdf") ? MediaType.APPLICATION_PDF
                : (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))
                ? MediaType.IMAGE_JPEG : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().headers(headers).contentType(mediaType).body(resource);
    }
}
