package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bus_job_application")
public class BusJobApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recruitmentId;
    private Long studentId;
    /** 旧状态字段（兼容）：0=已投递，1=有意向，2=不匹配 */
    private Integer status;
    private String message;
    private LocalDateTime applyTime;
    /** 面试时间 */
    private LocalDateTime interviewTime;
    /** 面试备注/通知内容 */
    private String interviewNote;
    /**
     * 最终状态：0=待筛选，1=简历通过，2=面试邀请，3=录用，4=淘汰
     */
    private Integer finalStatus;
}
