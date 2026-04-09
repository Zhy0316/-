package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordPractice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordPracticeMapper extends BaseMapper<RecordPractice> {
    List<RecordPractice> selectByStudentId(@Param("studentId") Long studentId);
}
