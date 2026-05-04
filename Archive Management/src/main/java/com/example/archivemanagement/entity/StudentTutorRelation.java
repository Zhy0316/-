package com.example.archivemanagement.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentTutorRelation {
    private Long id;
    private Long studentId;
    private Long tutorId;
    private Integer status;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    /** 学生自荐信 */
    private String applyNote;
    /** 附件路径 */
    private String attachFile;
    
    /** 驳回备注 */
    private String rejectNote;
}
