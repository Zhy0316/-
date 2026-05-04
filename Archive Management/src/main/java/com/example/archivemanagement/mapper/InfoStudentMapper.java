package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.InfoStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface InfoStudentMapper extends BaseMapper<InfoStudent> {

    /**
     * 批量查询所有学生摘要（含用户名、专业、学院等），用于 AI 推荐
     */
    @Select("SELECT s.user_id AS userId, u.real_name AS realName, " +
            "s.student_no AS studentNo, s.college, s.major, s.enrollment_year AS enrollmentYear " +
            "FROM info_student s LEFT JOIN sys_user u ON u.user_id = s.user_id " +
            "WHERE u.status = 1 " +
            "ORDER BY s.user_id LIMIT 100")
    List<Map<String, Object>> selectAllStudentSummary();
}
