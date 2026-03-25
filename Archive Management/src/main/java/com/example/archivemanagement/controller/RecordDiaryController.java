package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.RecordDiary;
import com.example.archivemanagement.service.RecordDiaryService;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class RecordDiaryController {

    private final RecordDiaryService service;

    @PostMapping
    public ResponseEntity<RecordDiary> createDiary(
            @RequestParam("studentId") Long studentId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "mood", required = false) String mood,
            @RequestParam(value = "visibility", required = false, defaultValue = "1") Integer visibility,
            @RequestParam(value = "recordDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date recordDate,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        RecordDiary diary = new RecordDiary();
        diary.setStudentId(studentId);
        diary.setTitle(title);
        diary.setContent(content);
        diary.setMood(mood);
        diary.setVisibility(visibility);
        diary.setRecordDate(recordDate != null ? recordDate : new Date());

        if (file != null && !file.isEmpty()) {
            try {
                String attachmentPath = FileUploadUtil.uploadFile(file, studentId, "diary");
                diary.setAttachmentPath(attachmentPath);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).build();
            }
        }

        boolean result = service.save(diary);
        if (result) {
            return ResponseEntity.ok(diary);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<RecordDiary>> getDiaries(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        List<RecordDiary> records = service.getRecords(studentId, startDate, endDate);
        return ResponseEntity.ok(records);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("studentId") Long studentId) {
        try {
            String filePath = FileUploadUtil.uploadFile(file, studentId, "diary");
            return ResponseEntity.ok(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("图片上传失败：" + e.getMessage());
        }
    }
}
