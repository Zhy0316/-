package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日记标签关联实体类
 */
@Data
@TableName("diary_tag_relation")
public class DiaryTagRelation {
    
    /**
     * 关联ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 日记ID
     */
    private Long diaryId;
    
    /**
     * 标签ID
     */
    private Long tagId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
