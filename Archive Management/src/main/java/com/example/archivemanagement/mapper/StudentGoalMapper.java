package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.StudentGoal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生目标Mapper
 */
@Mapper
public interface StudentGoalMapper extends BaseMapper<StudentGoal> {
    
    /**
     * 根据学生ID查询目标列表
     */
    List<StudentGoal> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 根据导师ID查询其指导学生的目标
     */
    List<StudentGoal> selectByTutorId(@Param("tutorId") Long tutorId);
    
    /**
     * 查询进行中的目标
     */
    List<StudentGoal> selectInProgressByStudentId(@Param("studentId") Long studentId);
}
