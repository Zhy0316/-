package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("forum_post")
public class ForumPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userRole;

    private String title;

    private String content;

    private String coverImage;

    private String contentType;

    private String category;

    private String tags;

    private Integer viewCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer commentCount;

    private Integer isTop;

    private Integer isEssence;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
