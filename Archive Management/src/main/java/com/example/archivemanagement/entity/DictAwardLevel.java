package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 获奖等级字典实体
 */
@Data
@TableName("dict_award_level")
public class DictAwardLevel {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 等级名称 */
    private String levelName;
    
    /** 等级代码 */
    private String levelCode;
    
    /** 对应分数 */
    private Integer score;
    
    /** 排序 */
    private Integer sortOrder;
    
    /** 等级说明 */
    private String description;
}
