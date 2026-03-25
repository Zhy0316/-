package com.example.archivemanagement.mapper;

import com.example.archivemanagement.entity.RecordAcademic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface RecordAcademicMapper {
    List<RecordAcademic> selectByStudentId(@Param("studentId") Long studentId);
    int insert(RecordAcademic record);
    int update(RecordAcademic record);
    int deleteById(@Param("id") Long id);

    /**
     * 按学年统计加权平均绩点，用于趋势图
     * 返回 List<Map>，每条含 academicYear 和 avgGpa
     */
    List<Map<String, Object>> selectGpaTrendByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询所有学生的加权平均绩点（用于全院绩点分布统计）
     * 返回 List<Map>，每条含 studentId 和 avgGpa
     */
    List<Map<String, Object>> selectAllStudentAvgGpa();
}
