package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("forum_post_comment")
public class ForumPostComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long parentId;

    private Long userId;

    private String content;

    private Integer likeCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
