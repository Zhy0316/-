package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordDiary;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface RecordDiaryMapper extends BaseMapper<RecordDiary> {
    List<RecordDiary> selectByStudentIdAndDateRange(
            @Param("studentId") Long studentId, 
            @Param("startDate") Date startDate, 
            @Param("endDate") Date endDate);
}
