package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业招聘信息实体（对应 bus_recruitment 表）
 */
@Data
@TableName("bus_recruitment")
public class BusRecruitment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布企业ID */
    private Long enterpriseId;

    /** 招聘标题 */
    private String title;

    /** 职位名称 */
    private String position;

    /** 薪资范围，如 "10k-15k" */
    private String salaryRange;

    /** 工作地点 */
    private String location;

    /** 任职要求 */
    private String requirement;

    /** 状态：1=招聘中，0=已结束 */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime createTime;
}
