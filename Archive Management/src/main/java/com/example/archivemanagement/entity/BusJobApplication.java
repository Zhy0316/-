package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 求职申请实体（对应 bus_job_application 表）
 */
@Data
@TableName("bus_job_application")
public class BusJobApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 招聘职位ID */
    private Long recruitmentId;

    /** 申请学生ID */
    private Long studentId;

    /** 状态：0=已投递，1=有意向，2=不匹配 */
    private Integer status;

    /** 留言信息 */
    private String message;

    /** 投递时间 */
    private LocalDateTime applyTime;
}
