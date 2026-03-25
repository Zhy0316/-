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
}
