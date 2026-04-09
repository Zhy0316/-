package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.EvalComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导师评语 Mapper
 */
@Mapper
public interface EvalCommentMapper extends BaseMapper<EvalComment> {

    /**
     * 查询某学生的所有评语（按时间倒序）
     */
    List<EvalComment> selectByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询某导师对某学生的评语
     */
    List<EvalComment> selectByTutorAndStudent(@Param("tutorId") Long tutorId,
                                               @Param("studentId") Long studentId);
}
