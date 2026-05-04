package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生成长目标实体类
 */
@Data
@TableName("student_goal")
public class StudentGoal {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 指导导师ID
     */
    private Long tutorId;
    
    /**
     * 目标类型(学期/学年/长期)
     */
    private String goalType;
    
    /**
     * 目标标题
     */
    private String title;
    
    /**
     * 目标描述
     */
    private String description;
    
    /**
     * 目标日期
     */
    private LocalDate targetDate;
    
    /**
     * 状态(0进行中/1已完成/2已放弃)
     */
    private Integer status;
    
    /**
     * 完成进度0-100
     */
    private Integer progress;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
