package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("bus_recruitment")
public class BusRecruitment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    private String title;
    private String position;
    private String salaryRange;
    private String location;
    private String requirement;
    /** 职位类型：全职/兼职/实习 */
    private String jobType;
    /** 招聘人数 */
    private Integer headcount;
    /** 截止日期 */
    private LocalDate deadline;
    /** 学历要求 */
    private String education;
    /** 工作经验要求 */
    private String experience;
    /** 职位详细描述 */
    private String description;
    /** 状态：1=招聘中，0=已结束 */
    private Integer status;
    private LocalDateTime createTime;
}
