package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("forum_post_resource")
public class ForumPostResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private String resourceType;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String fileExt;

    private String thumbnailUrl;

    private Integer duration;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
