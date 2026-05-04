package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordAward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordAwardMapper extends BaseMapper<RecordAward> {
    List<RecordAward> selectByStudentId(@Param("studentId") Long studentId);

    /** 查询所有已审核通过的获奖记录（用于AI学生推荐统计） */
    @org.apache.ibatis.annotations.Select("SELECT * FROM record_award WHERE audit_status = 1")
    List<RecordAward> selectAllPassed();

    /**
     * 查询导师待审核的获奖记录
     */
    @org.apache.ibatis.annotations.Select("SELECT ra.*, u.real_name as studentName, u.username as studentNo " +
            "FROM record_award ra " +
            "INNER JOIN student_tutor_relation r ON ra.student_id = r.student_id " +
            "INNER JOIN sys_user u ON ra.student_id = u.user_id " +
            "WHERE r.tutor_id = #{tutorId} AND r.status = 1 AND ra.audit_status = 0 " +
            "ORDER BY ra.id DESC")
    List<RecordAward> selectPendingByTutorId(@Param("tutorId") Long tutorId);

    /**
     * 统计导师待审核的获奖记录数量
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) " +
            "FROM record_award ra " +
            "INNER JOIN student_tutor_relation r ON ra.student_id = r.student_id " +
            "WHERE r.tutor_id = #{tutorId} AND r.status = 1 AND ra.audit_status = 0")
    int countPendingByTutorId(@Param("tutorId") Long tutorId);
}
