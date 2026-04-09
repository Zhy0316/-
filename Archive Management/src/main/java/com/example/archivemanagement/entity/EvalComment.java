package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导师评语实体（对应 eval_comment 表）
 */
@Data
@TableName("eval_comment")
public class EvalComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 导师ID */
    private Long tutorId;

    /** 学生ID */
    private Long studentId;

    /** 关联目标ID（如日记ID、作品ID），综合评价时为 null */
    private Long targetId;

    /** 类型：DIARY / PORTFOLIO / GENERAL */
    private String targetType;

    /** 评语内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
