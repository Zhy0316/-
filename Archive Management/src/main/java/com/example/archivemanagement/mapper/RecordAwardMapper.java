package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordAward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordAwardMapper extends BaseMapper<RecordAward> {
    List<RecordAward> selectByStudentId(@Param("studentId") Long studentId);
}
