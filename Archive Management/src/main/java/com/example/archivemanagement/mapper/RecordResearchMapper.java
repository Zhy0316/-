package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordResearch;

import java.util.List;

public interface RecordResearchMapper extends BaseMapper<RecordResearch> {
    List<RecordResearch> selectByStudentId(Long studentId);
    RecordResearch selectById(Long id);
}
