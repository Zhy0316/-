package com.example.archivemanagement.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecordAcademic {
    private Long id;
    private Long studentId;
    
    @ExcelProperty("学年")
    private String academicYear;
    
    @ExcelProperty("课程性质")
    private String courseNature;
    
    @ExcelProperty("课程名称")
    private String courseName;
    
    @ExcelProperty("学分")
    private BigDecimal credit;
    
    @ExcelProperty("绩点")
    private BigDecimal gpa;
    
    @ExcelProperty("成绩")
    private String score;
    
    @ExcelProperty("是否成绩作废")
    private String isInvalidated;
    
    @ExcelProperty("学分绩点")
    private BigDecimal creditGpa;
}
