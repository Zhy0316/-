package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.RecordDiary;
import com.example.archivemanagement.service.RecordDiaryService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class RecordDiaryController {

    private final RecordDiaryService service;

    /** 分页查询日记列表（学生查自己，导师查绑定学生的公开日记） */
    @GetMapping("/list")
    public ResponseEntity<List<RecordDiary>> list(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "onlyPublic", required = false, defaultValue = "false") Boolean onlyPublic) {
        List<RecordDiary> records = service.getRecords(studentId, null, null);
        if (onlyPublic) {
            records = records.stream().filter(d -> d.getVisibility() != null && d.getVisibility() == 1).toList();
        }
        return ResponseEntity.ok(records);
    }

    /** 兼容旧接口：GET /api/diary?studentId=xxx */
    @GetMapping
    public ResponseEntity<List<RecordDiary>> getDiaries(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(service.getRecords(studentId, startDate, endDate));
    }

    /** 新增日记（支持附件上传） */
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
                diary.setAttachmentPath(FileUploadUtil.uploadFile(file, studentId, "diary"));
            } catch (Exception e) {
                return ResponseEntity.status(500).build();
            }
        }
        return service.save(diary) ? ResponseEntity.ok(diary) : ResponseEntity.badRequest().build();
    }

    /** 编辑日记（JSON body，不含文件） */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiary(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        RecordDiary diary = service.getById(id);
        if (diary == null) return ResponseEntity.status(404).body("日记不存在");
        if (body.containsKey("title"))      diary.setTitle(body.get("title").toString());
        if (body.containsKey("content"))    diary.setContent(body.get("content").toString());
        if (body.containsKey("mood"))       diary.setMood(body.get("mood").toString());
        if (body.containsKey("visibility")) diary.setVisibility(Integer.valueOf(body.get("visibility").toString()));
        service.updateById(diary);
        return ResponseEntity.ok("更新成功");
    }

    /** 删除日记 */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long id) {
        service.removeById(id);
        return ResponseEntity.ok("删除成功");
    }

    /** 上传日记图片（富文本编辑器使用） */
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("studentId") Long studentId) {
        try {
            return ResponseEntity.ok(FileUploadUtil.uploadFile(file, studentId, "diary"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("图片上传失败：" + e.getMessage());
        }
    }
}
