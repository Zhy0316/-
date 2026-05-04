package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("record_diary")
public class RecordDiary {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    private String title;
    private String content;
    
    /**
     * 情绪标签(开心/焦虑/奋斗/迷茫等)
     */
    private String mood;
    
    /**
     * 是否里程碑事件(1是/0否)
     */
    private Integer isMilestone;
    
    /**
     * 导师可见(1可见/0不可见)
     */
    private Integer tutorVisible;
    
    /**
     * 导师批注
     */
    private String tutorComment;
    
    /**
     * 导师批注时间
     */
    private LocalDateTime tutorCommentTime;
    
    private String attachmentPath;
    private Date recordDate;
    private Date createTime;
    private Integer visibility;
    
    /** 星标：0=否，1=是 */
    private Integer isStarred;
    
    /** 分组/分类名称 */
    private String category;
    
    /** 标签，多个用逗号分隔(旧版,保留兼容) */
    private String tags;
}
