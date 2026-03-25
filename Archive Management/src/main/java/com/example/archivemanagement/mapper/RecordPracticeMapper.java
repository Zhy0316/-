package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordPractice;

import java.util.List;

public interface RecordPracticeMapper extends BaseMapper<RecordPractice> {
    List<RecordPractice> selectByStudentId(Long studentId);
    RecordPractice selectById(Long id);
}
