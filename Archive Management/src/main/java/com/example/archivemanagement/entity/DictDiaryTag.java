package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日记标签字典实体类
 */
@Data
@TableName("dict_diary_tag")
public class DictDiaryTag {
    
    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * 标签类型(emotion情绪/theme主题/milestone里程碑)
     */
    private String tagType;
    
    /**
     * 标签图标(emoji或图标类名)
     */
    private String tagIcon;
    
    /**
     * 标签颜色(hex颜色值)
     */
    private String tagColor;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 是否系统标签(1是/0否)
     */
    private Integer isSystem;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
