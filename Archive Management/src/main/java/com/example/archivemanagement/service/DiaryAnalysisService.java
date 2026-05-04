package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.archivemanagement.entity.DictDiaryTag;
import com.example.archivemanagement.entity.DiaryTagRelation;
import com.example.archivemanagement.entity.RecordDiary;
import com.example.archivemanagement.mapper.DictDiaryTagMapper;
import com.example.archivemanagement.mapper.DiaryTagRelationMapper;
import com.example.archivemanagement.mapper.RecordDiaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 日记分析服务
 */
@Service
@RequiredArgsConstructor
public class DiaryAnalysisService {
    
    private final RecordDiaryMapper diaryMapper;
    private final DictDiaryTagMapper tagMapper;
    private final DiaryTagRelationMapper relationMapper;
    
    /**
     * 获取所有标签(按类型分组)
     */
    public Map<String, List<DictDiaryTag>> getAllTagsGrouped() {
        List<DictDiaryTag> allTags = tagMapper.selectList(null);
        return allTags.stream()
                .collect(Collectors.groupingBy(DictDiaryTag::getTagType));
    }
    
    /**
     * 根据类型获取标签列表
     */
    public List<DictDiaryTag> getTagsByType(String tagType) {
        return tagMapper.selectByType(tagType);
    }
    
    /**
     * 获取系统标签
     */
    public List<DictDiaryTag> getSystemTags() {
        return tagMapper.selectSystemTags();
    }
    
    /**
     * 为日记添加标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void addTagsToDiary(Long diaryId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        
        // 先删除旧的标签关联
        relationMapper.deleteByDiaryId(diaryId);
        
        // 批量插入新的标签关联
        List<DiaryTagRelation> relations = tagIds.stream()
                .map(tagId -> {
                    DiaryTagRelation relation = new DiaryTagRelation();
                    relation.setDiaryId(diaryId);
                    relation.setTagId(tagId);
                    return relation;
                })
                .collect(Collectors.toList());
        
        if (!relations.isEmpty()) {
            relationMapper.batchInsert(relations);
        }
    }
    
    /**
     * 获取日记的标签列表
     */
    public List<DictDiaryTag> getDiaryTags(Long diaryId) {
        List<Long> tagIds = relationMapper.selectTagIdsByDiaryId(diaryId);
        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        QueryWrapper<DictDiaryTag> wrapper = new QueryWrapper<>();
        wrapper.in("id", tagIds);
        return tagMapper.selectList(wrapper);
    }
    
    /**
     * 情绪统计分析
     */
    public Map<String, Object> analyzeMoodStatistics(Long studentId) {
        List<RecordDiary> diaries = diaryMapper.selectByStudentId(studentId);
        
        // 统计各情绪出现次数
        Map<String, Long> moodCount = diaries.stream()
                .filter(d -> d.getMood() != null && !d.getMood().isEmpty())
                .collect(Collectors.groupingBy(RecordDiary::getMood, Collectors.counting()));
        
        // 计算情绪分布百分比
        long total = diaries.size();
        Map<String, Double> moodPercentage = moodCount.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> total > 0 ? (e.getValue() * 100.0 / total) : 0.0
                ));
        
        Map<String, Object> result = new HashMap<>();
        result.put("moodCount", moodCount);
        result.put("moodPercentage", moodPercentage);
        result.put("totalDiaries", total);
        
        return result;
    }
    
    /**
     * 时光轴数据生成(按月分组)
     */
    public Map<String, List<RecordDiary>> generateTimeline(Long studentId, String groupBy) {
        List<RecordDiary> diaries = diaryMapper.selectByStudentId(studentId);
        
        // 按日期排序
        diaries.sort(Comparator.comparing(RecordDiary::getRecordDate).reversed());
        
        // 根据groupBy参数分组
        Map<String, List<RecordDiary>> timeline = new LinkedHashMap<>();
        
        for (RecordDiary diary : diaries) {
            String key = formatDateKey(diary.getRecordDate(), groupBy);
            timeline.computeIfAbsent(key, k -> new ArrayList<>()).add(diary);
        }
        
        return timeline;
    }
    
    /**
     * 格式化日期键
     */
    private String formatDateKey(Date date, String groupBy) {
        if (date == null) return "未知";
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        
        switch (groupBy) {
            case "year":
                return year + "年";
            case "month":
                return year + "年" + month + "月";
            case "semester":
                // 简单判断:1-7月为春季学期,8-12月为秋季学期
                String semester = month <= 7 ? "春季学期" : "秋季学期";
                return year + "年" + semester;
            default:
                return year + "年" + month + "月";
        }
    }
    
    /**
     * 关键词统计(简单实现,统计标题中的高频词)
     */
    public Map<String, Integer> analyzeKeywords(Long studentId) {
        List<RecordDiary> diaries = diaryMapper.selectByStudentId(studentId);
        
        Map<String, Integer> keywordCount = new HashMap<>();
        
        for (RecordDiary diary : diaries) {
            if (diary.getTitle() != null && !diary.getTitle().isEmpty()) {
                // 简单分词:按空格和标点分割
                String[] words = diary.getTitle().split("[\\s,，。.!！?？;；:：]+");
                for (String word : words) {
                    if (word.length() >= 2) { // 只统计2个字以上的词
                        keywordCount.merge(word, 1, Integer::sum);
                    }
                }
            }
        }
        
        // 按出现次数排序,返回前20个
        return keywordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * 添加导师批注
     */
    @Transactional(rollbackFor = Exception.class)
    public void addTutorComment(Long diaryId, String comment) {
        RecordDiary diary = diaryMapper.selectById(diaryId);
        if (diary != null) {
            diary.setTutorComment(comment);
            diary.setTutorCommentTime(LocalDateTime.now());
            diaryMapper.updateById(diary);
        }
    }
    
    /**
     * 获取里程碑日记列表
     */
    public List<RecordDiary> getMilestoneDiaries(Long studentId) {
        QueryWrapper<RecordDiary> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        wrapper.eq("is_milestone", 1);
        wrapper.orderByDesc("record_date");
        return diaryMapper.selectList(wrapper);
    }
    
    /**
     * 获取导师可见的日记列表
     */
    public List<RecordDiary> getTutorVisibleDiaries(Long studentId) {
        QueryWrapper<RecordDiary> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        wrapper.eq("tutor_visible", 1);
        wrapper.orderByDesc("record_date");
        return diaryMapper.selectList(wrapper);
    }
}
