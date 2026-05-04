package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.DictDiaryTag;
import com.example.archivemanagement.entity.RecordDiary;
import com.example.archivemanagement.service.DiaryAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 日记分析控制器
 */
@RestController
@RequestMapping("/api/diary-analysis")
@RequiredArgsConstructor
public class DiaryAnalysisController {
    
    private final DiaryAnalysisService diaryAnalysisService;
    
    /**
     * 获取所有标签(按类型分组)
     */
    @GetMapping("/tags/grouped")
    public Result<Map<String, List<DictDiaryTag>>> getAllTagsGrouped() {
        Map<String, List<DictDiaryTag>> tags = diaryAnalysisService.getAllTagsGrouped();
        return Result.success(tags, "标签添加成功");
    }
    
    /**
     * 根据类型获取标签列表
     */
    @GetMapping("/tags/type/{tagType}")
    public Result<List<DictDiaryTag>> getTagsByType(@PathVariable String tagType) {
        List<DictDiaryTag> tags = diaryAnalysisService.getTagsByType(tagType);
        return Result.success(tags, "标签添加成功");
    }
    
    /**
     * 获取系统标签
     */
    @GetMapping("/tags/system")
    public Result<List<DictDiaryTag>> getSystemTags() {
        List<DictDiaryTag> tags = diaryAnalysisService.getSystemTags();
        return Result.success(tags, "标签添加成功");
    }
    
    /**
     * 为日记添加标签
     */
    @PostMapping("/tags/add")
    public Result<Void> addTagsToDiary(@RequestBody Map<String, Object> params) {
        Long diaryId = Long.valueOf(params.get("diaryId").toString());
        @SuppressWarnings("unchecked")
        List<Long> tagIds = (List<Long>) params.get("tagIds");
        
        diaryAnalysisService.addTagsToDiary(diaryId, tagIds);
        return Result.success(null, "标签添加成功");
    }
    
    /**
     * 获取日记的标签列表
     */
    @GetMapping("/tags/diary/{diaryId}")
    public Result<List<DictDiaryTag>> getDiaryTags(@PathVariable Long diaryId) {
        List<DictDiaryTag> tags = diaryAnalysisService.getDiaryTags(diaryId);
        return Result.success(tags, "标签添加成功");
    }
    
    /**
     * 情绪统计分析
     */
    @GetMapping("/mood-statistics/{studentId}")
    public Result<Map<String, Object>> analyzeMoodStatistics(@PathVariable Long studentId) {
        Map<String, Object> statistics = diaryAnalysisService.analyzeMoodStatistics(studentId);
        return Result.success(statistics, "标签添加成功");
    }
    
    /**
     * 时光轴数据生成
     * @param studentId 学生ID
     * @param groupBy 分组方式: year(年)/month(月)/semester(学期)
     */
    @GetMapping("/timeline/{studentId}")
    public Result<Map<String, List<RecordDiary>>> generateTimeline(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "month") String groupBy) {
        Map<String, List<RecordDiary>> timeline = diaryAnalysisService.generateTimeline(studentId, groupBy);
        return Result.success(timeline, "标签添加成功");
    }
    
    /**
     * 关键词统计
     */
    @GetMapping("/keywords/{studentId}")
    public Result<Map<String, Integer>> analyzeKeywords(@PathVariable Long studentId) {
        Map<String, Integer> keywords = diaryAnalysisService.analyzeKeywords(studentId);
        return Result.success(keywords, "关键词统计获取成功");
    }
    
    /**
     * 添加导师批注
     */
    @PostMapping("/tutor-comment")
    public Result<Void> addTutorComment(@RequestBody Map<String, Object> params) {
        Long diaryId = Long.valueOf(params.get("diaryId").toString());
        String comment = params.get("comment").toString();
        
        diaryAnalysisService.addTutorComment(diaryId, comment);
        return Result.success(null, "批注添加成功");
    }
    
    /**
     * 获取里程碑日记列表
     */
    @GetMapping("/milestones/{studentId}")
    public Result<List<RecordDiary>> getMilestoneDiaries(@PathVariable Long studentId) {
        List<RecordDiary> diaries = diaryAnalysisService.getMilestoneDiaries(studentId);
        return Result.success(diaries, "标签添加成功");
    }
    
    /**
     * 获取导师可见的日记列表
     */
    @GetMapping("/tutor-visible/{studentId}")
    public Result<List<RecordDiary>> getTutorVisibleDiaries(@PathVariable Long studentId) {
        List<RecordDiary> diaries = diaryAnalysisService.getTutorVisibleDiaries(studentId);
        return Result.success(diaries, "标签添加成功");
    }
}
