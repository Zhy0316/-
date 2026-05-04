package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 导师指导记录实体类
 */
@Data
@TableName("tutor_guidance_record")
public class TutorGuidanceRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 导师ID
     */
    private Long tutorId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 指导类型(线上/线下/电话/邮件)
     */
    private String guidanceType;
    
    /**
     * 指导主题
     */
    private String guidanceTopic;
    
    /**
     * 指导内容
     */
    private String guidanceContent;
    
    /**
     * 指导日期
     */
    private LocalDate guidanceDate;
    
    /**
     * 时长(分钟)
     */
    private Integer duration;
    
    /**
     * 学生反馈
     */
    private String studentFeedback;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
