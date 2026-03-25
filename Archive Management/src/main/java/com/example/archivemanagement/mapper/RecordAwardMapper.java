package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordAward;

import java.util.List;

public interface RecordAwardMapper extends BaseMapper<RecordAward> {
    List<RecordAward> selectByStudentId(Long studentId);
    RecordAward selectById(Long id);
}
