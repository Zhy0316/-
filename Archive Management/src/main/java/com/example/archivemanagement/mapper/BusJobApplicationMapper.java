package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.BusJobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 求职申请 Mapper
 */
@Mapper
public interface BusJobApplicationMapper extends BaseMapper<BusJobApplication> {

    /** 查询某学生的所有投递记录（含招聘信息） */
    List<java.util.Map<String, Object>> selectByStudentWithDetail(@Param("studentId") Long studentId);

    /** 查询某招聘职位的所有投递（含学生信息） */
    List<java.util.Map<String, Object>> selectByRecruitmentWithDetail(@Param("recruitmentId") Long recruitmentId);
}
