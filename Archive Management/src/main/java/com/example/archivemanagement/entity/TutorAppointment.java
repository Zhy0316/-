package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导师预约实体类
 */
@Data
@TableName("tutor_appointment")
public class TutorAppointment {
    
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
     * 预约时间
     */
    private LocalDateTime appointmentTime;
    
    /**
     * 时长(分钟)
     */
    private Integer duration;
    
    /**
     * 咨询主题
     */
    private String topic;
    
    /**
     * 问题描述
     */
    private String description;
    
    /**
     * 状态(0待确认/1已确认/2已完成/3已取消)
     */
    private Integer status;
    
    /**
     * 反馈记录
     */
    private String feedback;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
