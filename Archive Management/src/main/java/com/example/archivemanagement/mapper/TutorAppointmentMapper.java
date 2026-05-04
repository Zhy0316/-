package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.TutorAppointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导师预约Mapper
 */
@Mapper
public interface TutorAppointmentMapper extends BaseMapper<TutorAppointment> {
    
    /**
     * 根据导师ID查询预约列表
     */
    List<TutorAppointment> selectByTutorId(@Param("tutorId") Long tutorId);
    
    /**
     * 根据学生ID查询预约列表
     */
    List<TutorAppointment> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询待确认的预约
     */
    List<TutorAppointment> selectPendingByTutorId(@Param("tutorId") Long tutorId);
    
    /**
     * 查询指定时间段的预约
     */
    List<TutorAppointment> selectByTimeRange(
            @Param("tutorId") Long tutorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
