package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 成长报告模板实体类
 */
@Data
@TableName("growth_report_template")
public class GrowthReportTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板类型(semester/year/custom)
     */
    private String templateType;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 报告章节配置(JSON格式)
     */
    private String sections;
    
    /**
     * 是否默认模板
     */
    private Boolean isDefault;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
