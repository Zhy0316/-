package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.RecordDiary;
import com.example.archivemanagement.mapper.RecordDiaryMapper;
import com.example.archivemanagement.service.RecordDiaryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecordDiaryServiceImpl extends ServiceImpl<RecordDiaryMapper, RecordDiary> implements RecordDiaryService {

    @Override
    public List<RecordDiary> getRecords(Long studentId, Date startDate, Date endDate) {
        return baseMapper.selectByStudentIdAndDateRange(studentId, startDate, endDate);
    }

    @Override
    public List<RecordDiary> getRecordsWithFilter(Long studentId, Boolean starred,
                                                   String category, String tag,
                                                   String keyword, Integer visibility) {
        return baseMapper.selectWithFilter(studentId, starred, category, tag, keyword, visibility);
    }

    @Override
    public List<String> getCategories(Long studentId) {
        return baseMapper.selectCategories(studentId);
    }

    @Override
    public List<String> getTags(Long studentId) {
        // 将所有 tags 字符串拆分、去重、排序
        List<String> rawList = baseMapper.selectAllTags(studentId);
        return rawList.stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public boolean toggleStar(Long id, boolean star) {
        return baseMapper.toggleStar(id, star ? 1 : 0) > 0;
    }

    @Override
    public boolean save(RecordDiary record) {
        if (record.getRecordDate() == null) record.setRecordDate(new Date());
        if (record.getVisibility() == null) record.setVisibility(1);
        if (record.getIsStarred() == null) record.setIsStarred(0);
        return super.save(record);
    }
}
