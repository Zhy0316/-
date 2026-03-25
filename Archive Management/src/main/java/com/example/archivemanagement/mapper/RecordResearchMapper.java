package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordResearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordResearchMapper extends BaseMapper<RecordResearch> {
    List<RecordResearch> selectByStudentId(@Param("studentId") Long studentId);
}
