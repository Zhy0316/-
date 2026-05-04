package com.example.archivemanagement.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 成长报告DTO
 */
@Data
public class GrowthReportDTO {
    
    /**
     * 报告ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 报告类型
     */
    private String reportType;
    
    /**
     * 统计周期
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
     * 学业表现
     */
    private AcademicPerformance academicPerformance;
    
    /**
     * 能力发展
     */
    private AbilityDevelopment abilityDevelopment;
    
    /**
     * 荣誉成就
     */
    private AchievementSummary achievementSummary;
    
    /**
     * 成长轨迹
     */
    private GrowthTrajectory growthTrajectory;
    
    /**
     * 未来建议
     */
    private List<String> suggestions;
    
    /**
     * 学业表现
     */
    @Data
    public static class AcademicPerformance {
        /**
         * 当前GPA
         */
        private Double currentGpa;
        
        /**
         * 平均GPA
         */
        private Double avgGpa;
        
        /**
         * GPA趋势(上升/下降/稳定)
         */
        private String gpaTrend;
        
        /**
         * 学分完成情况
         */
        private Map<String, Object> creditProgress;
        
        /**
         * 课程类型分布
         */
        private List<Map<String, Object>> courseDistribution;
        
        /**
         * 优秀课程列表
         */
        private List<String> excellentCourses;
        
        /**
         * 风险课程列表
         */
        private List<String> riskCourses;
        
        /**
         * 总结
         */
        private String summary;
    }
    
    /**
     * 能力发展
     */
    @Data
    public static class AbilityDevelopment {
        /**
         * 新增技能数量
         */
        private Integer newSkillCount;
        
        /**
         * 技能标签列表
         */
        private List<String> skillTags;
        
        /**
         * 实践经历数量
         */
        private Integer practiceCount;
        
        /**
         * 实践类型分布
         */
        private Map<String, Integer> practiceDistribution;
        
        /**
         * 作品产出数量
         */
        private Integer portfolioCount;
        
        /**
         * 科研项目数量
         */
        private Integer researchCount;
        
        /**
         * 总结
         */
        private String summary;
    }
    
    /**
     * 荣誉成就
     */
    @Data
    public static class AchievementSummary {
        /**
         * 获奖总数
         */
        private Integer totalAwards;
        
        /**
         * 获奖总分
         */
        private Integer totalScore;
        
        /**
         * 等级分布
         */
        private Map<String, Integer> levelDistribution;
        
        /**
         * 获奖列表
         */
        private List<Map<String, Object>> awardList;
        
        /**
         * 同比增长
         */
        private String growthRate;
        
        /**
         * 总结
         */
        private String summary;
    }
    
    /**
     * 成长轨迹
     */
    @Data
    public static class GrowthTrajectory {
        /**
         * 关键里程碑
         */
        private List<Map<String, Object>> milestones;
        
        /**
         * 情绪变化曲线
         */
        private Map<String, Integer> moodDistribution;
        
        /**
         * 高频关注主题
         */
        private List<Map<String, Object>> topThemes;
        
        /**
         * 日记更新频率
         */
        private String diaryFrequency;
        
        /**
         * 总结
         */
        private String summary;
    }
}
