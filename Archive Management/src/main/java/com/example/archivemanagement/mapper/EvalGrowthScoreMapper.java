package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.EvalGrowthScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成长评分 Mapper
 */
@Mapper
public interface EvalGrowthScoreMapper extends BaseMapper<EvalGrowthScore> {

    /** 查询某学生所有学期的成长分（按学期升序） */
    List<EvalGrowthScore> selectByStudentId(@Param("studentId") Long studentId);

    /** 查询全院学生最新综合成长分排行（取每个学生最新一条） */
    List<java.util.Map<String, Object>> selectRanking();
}
