package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习资源实体（对应 learn_resource 表）
 */
@Data
@TableName("learn_resource")
public class LearnResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 资源标题 */
    private String title;

    /** 分类：课程资料 / 竞赛资源 / 就业指导 / 其他 */
    private String category;

    /** 资源描述 */
    private String description;

    /** 文件路径（上传文件时填写） */
    private String fileUrl;

    /** 外链地址（填写链接时使用） */
    private String linkUrl;

    /** 上传者ID */
    private Long uploaderId;

    /** 上传者角色 */
    private String uploaderRole;

    /** 浏览次数 */
    private Integer viewCount;

    /** 点赞次数 */
    private Integer likeCount;

    /** 创建时间 */
    private LocalDateTime createTime;
}
