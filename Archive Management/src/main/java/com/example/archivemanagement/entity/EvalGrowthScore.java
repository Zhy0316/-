package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生成长综合评分实体（对应 eval_growth_score 表）
 */
@Data
@TableName("eval_growth_score")
public class EvalGrowthScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 统计年度/学期，如 "2023-2024-1" */
    private String termYear;

    /** 学业成绩分（满分100，权重40%） */
    private BigDecimal academicScore;

    /** 获奖加分（满分100，权重20%） */
    private BigDecimal awardScore;

    /** 科研能力分（满分100，权重20%） */
    private BigDecimal researchScore;

    /** 实践表现分（满分100，权重20%） */
    private BigDecimal practiceScore;

    /** 综合成长分 = 学业×0.4 + 获奖×0.2 + 科研×0.2 + 实践×0.2 */
    private BigDecimal totalScore;

    /** 最后更新时间 */
    private LocalDateTime updateTime;
}
