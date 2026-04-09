package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.RecordDiary;
import com.example.archivemanagement.service.RecordDiaryService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class RecordDiaryController {

    private final RecordDiaryService service;

    /** 基础列表（学生本人查全部，其他角色只查公开） */
    @GetMapping("/list")
    public Result<List<RecordDiary>> list(@RequestParam("studentId") Long studentId,
                                          HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        String roleKey = (String) request.getAttribute("roleKey");
        if ("student".equals(roleKey) && !studentId.equals(callerId)) {
            throw BusinessException.forbidden("无权查看其他学生的日记");
        }
        List<RecordDiary> records = service.getRecords(studentId, null, null);
        if (!"student".equals(roleKey) || !studentId.equals(callerId)) {
            records = records.stream()
                    .filter(d -> d.getVisibility() != null && d.getVisibility() == 1)
                    .toList();
        }
        return Result.ok(records);
    }

    /** 兼容旧接口 */
    @GetMapping
    public Result<List<RecordDiary>> getDiaries(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        String roleKey = (String) request.getAttribute("roleKey");
        List<RecordDiary> records = service.getRecords(studentId, startDate, endDate);
        if (!"student".equals(roleKey) || !studentId.equals(callerId)) {
            records = records.stream()
                    .filter(d -> d.getVisibility() != null && d.getVisibility() == 1)
                    .toList();
        }
        return Result.ok(records);
    }

    /** 带筛选条件查询 */
    @GetMapping("/filter")
    public Result<List<RecordDiary>> filter(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "starred", required = false) Boolean starred,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        String roleKey = (String) request.getAttribute("roleKey");
        if ("student".equals(roleKey) && !studentId.equals(callerId)) {
            throw BusinessException.forbidden("无权查看");
        }
        Integer visibility = (!"student".equals(roleKey) || !studentId.equals(callerId)) ? 1 : null;
        return Result.ok(service.getRecordsWithFilter(studentId, starred, category, tag, keyword, visibility));
    }

    @GetMapping("/categories")
    public Result<List<String>> categories(@RequestParam("studentId") Long studentId) {
        return Result.ok(service.getCategories(studentId));
    }

    @GetMapping("/tags")
    public Result<List<String>> tags(@RequestParam("studentId") Long studentId) {
        return Result.ok(service.getTags(studentId));
    }

    @PostMapping
    public Result<RecordDiary> createDiary(
            @RequestParam("studentId") Long studentId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "mood", required = false) String mood,
            @RequestParam(value = "visibility", required = false, defaultValue = "1") Integer visibility,
            @RequestParam(value = "isStarred", required = false, defaultValue = "0") Integer isStarred,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "recordDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date recordDate,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) throws IOException {
        Long callerId = (Long) request.getAttribute("userId");
        if (!studentId.equals(callerId)) throw BusinessException.forbidden("无权操作");

        RecordDiary diary = new RecordDiary();
        diary.setStudentId(studentId);
        diary.setTitle(title);
        diary.setContent(content);
        diary.setMood(mood);
        diary.setVisibility(visibility);
        diary.setIsStarred(isStarred);
        diary.setCategory(category);
        diary.setTags(tags);
        diary.setRecordDate(recordDate != null ? recordDate : new Date());
        if (file != null && !file.isEmpty()) {
            diary.setAttachmentPath(FileUploadUtil.uploadFile(file, studentId, "diary"));
        }
        if (!service.save(diary)) throw new BusinessException("保存失败");
        return Result.ok(diary);
    }

    @PutMapping("/{id}")
    public Result<Void> updateDiary(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        RecordDiary diary = service.getById(id);
        if (diary == null) throw BusinessException.notFound("日记不存在");
        if (!diary.getStudentId().equals(callerId)) throw BusinessException.forbidden("无权修改他人日记");
        if (body.containsKey("title"))      diary.setTitle(body.get("title").toString());
        if (body.containsKey("content"))    diary.setContent(body.get("content").toString());
        if (body.containsKey("mood"))       diary.setMood(body.get("mood").toString());
        if (body.containsKey("visibility")) diary.setVisibility(Integer.valueOf(body.get("visibility").toString()));
        if (body.containsKey("isStarred"))  diary.setIsStarred(Integer.valueOf(body.get("isStarred").toString()));
        if (body.containsKey("category"))   diary.setCategory(body.get("category").toString());
        if (body.containsKey("tags"))       diary.setTags(body.get("tags").toString());
        service.updateById(diary);
        return Result.ok("更新成功");
    }

    @PutMapping("/{id}/star")
    public Result<Void> toggleStar(@PathVariable Long id,
                                   @RequestParam("starred") boolean starred,
                                   HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        RecordDiary diary = service.getById(id);
        if (diary == null) throw BusinessException.notFound("日记不存在");
        if (!diary.getStudentId().equals(callerId)) throw BusinessException.forbidden("无权操作");
        service.toggleStar(id, starred);
        return Result.ok(starred ? "已加星标" : "已取消星标");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDiary(@PathVariable Long id, HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        RecordDiary diary = service.getById(id);
        if (diary == null) throw BusinessException.notFound("日记不存在");
        if (!diary.getStudentId().equals(callerId)) throw BusinessException.forbidden("无权删除他人日记");
        service.removeById(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/upload-image")
    public Result<Void> uploadImage(@RequestParam("file") MultipartFile file,
                                    @RequestParam("studentId") Long studentId) throws IOException {
        return Result.ok(FileUploadUtil.uploadFile(file, studentId, "diary"));
    }
}
