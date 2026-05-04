package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("record_award")
public class RecordAward {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String awardName;
    private String awardLevel;
    private String issuingAuthority;
    private Date awardDate;
    private String description;
    private String certFile;
    private Integer auditStatus;
    
    /** 驳回备注 */
    private String rejectNote;
    
    /** 获奖等级ID(关联dict_award_level) */
    private Long awardLevelId;
    
    /** 获奖分数 */
    private Integer awardScore;
    
    /** 学生姓名（非数据库字段，用于查询返回） */
    private String studentName;
    
    /** 学生学号（非数据库字段，用于查询返回） */
    private String studentNo;
}
