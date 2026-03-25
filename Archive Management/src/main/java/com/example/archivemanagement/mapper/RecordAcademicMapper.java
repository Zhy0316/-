package com.example.archivemanagement.mapper;

import com.example.archivemanagement.entity.RecordAcademic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RecordAcademicMapper {
    List<RecordAcademic> selectByStudentId(@Param("studentId") Long studentId);
    int insert(RecordAcademic record);
    int update(RecordAcademic record);
    int deleteById(@Param("id") Long id);
}
