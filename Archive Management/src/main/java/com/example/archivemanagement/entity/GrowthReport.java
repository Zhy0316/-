package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生成长报告实体类
 */
@Data
@TableName("growth_report")
public class GrowthReport {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 报告类型(semester学期/year学年/custom自定义)
     */
    private String reportType;
    
    /**
     * 统计周期(如2023-2024-1)
     */
    private String termYear;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 报告标题
     */
    private String title;
    
    /**
     * 报告摘要
     */
    private String summary;
    
    /**
     * 学业表现总结
     */
    private String academicSummary;
    
    /**
     * 能力发展总结
     */
    private String abilitySummary;
    
    /**
     * 荣誉成就总结
     */
    private String achievementSummary;
    
    /**
     * 成长轨迹总结
     */
    private String growthSummary;
    
    /**
     * 未来建议
     */
    private String suggestions;
    
    /**
     * 报告详细数据(JSON格式)
     */
    private String reportData;
    
    /**
     * PDF文件路径
     */
    private String pdfUrl;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 状态(0草稿/1已生成/2已归档)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
