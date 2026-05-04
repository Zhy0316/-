package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.TutorGuidanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 导师指导记录Mapper
 */
@Mapper
public interface TutorGuidanceRecordMapper extends BaseMapper<TutorGuidanceRecord> {
    
    /**
     * 根据导师ID查询指导记录
     */
    List<TutorGuidanceRecord> selectByTutorId(@Param("tutorId") Long tutorId);
    
    /**
     * 根据学生ID查询指导记录
     */
    List<TutorGuidanceRecord> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 统计导师指导次数(按学生分组)
     */
    List<Map<String, Object>> countByTutorGroupByStudent(@Param("tutorId") Long tutorId);
    
    /**
     * 统计导师指导主题分布
     */
    List<Map<String, Object>> countTopicDistribution(@Param("tutorId") Long tutorId);
}
