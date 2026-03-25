package com.example.archivemanagement.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RecordPractice {
    private Long id;
    private Long studentId;
    private String activityName;
    private String organization;
    private Date startDate;
    private Date endDate;
    private String content;
    private String proofFile;
}
