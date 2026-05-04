package com.example.archivemanagement.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 学业健康度报告 DTO
 */
@Data
public class AcademicHealthDTO {
    
    /** 当前学期GPA */
    private BigDecimal currentGPA;
    
    /** 历史平均GPA */
    private BigDecimal avgGPA;
    
    /** 最高GPA */
    private BigDecimal maxGPA;
    
    /** 最低GPA */
    private BigDecimal minGPA;
    
    /** 学分进度 */
    private CreditProgress creditProgress;
    
    /** 课程类型分布 */
    private List<CourseTypeStats> courseTypeStats;
    
    /** GPA趋势(按学期) */
    private List<GPATrend> gpaTrends;
    
    /** 风险课程列表(GPA < 2.0) */
    private List<RiskCourse> riskCourses;
    
    /** 学业建议 */
    private List<String> suggestions;
    
    /**
     * 学分进度
     */
    @Data
    public static class CreditProgress {
        /** 已修学分 */
        private BigDecimal completed;
        /** 毕业要求学分 */
        private BigDecimal required;
        /** 完成百分比 */
        private BigDecimal percentage;
    }
    
    /**
     * 课程类型统计
     */
    @Data
    public static class CourseTypeStats {
        /** 课程类型 */
        private String courseType;
        /** 课程数量 */
        private Integer count;
        /** 平均GPA */
        private BigDecimal avgGPA;
        /** 总学分 */
        private BigDecimal totalCredit;
    }
    
    /**
     * GPA趋势
     */
    @Data
    public static class GPATrend {
        /** 学年学期 */
        private String term;
        /** GPA */
        private BigDecimal gpa;
        /** 学分 */
        private BigDecimal credit;
    }
    
    /**
     * 风险课程
     */
    @Data
    public static class RiskCourse {
        /** 课程名称 */
        private String courseName;
        /** 学年 */
        private String academicYear;
        /** GPA */
        private BigDecimal gpa;
        /** 成绩 */
        private String score;
    }
}
