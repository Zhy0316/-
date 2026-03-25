package com.example.archivemanagement.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RecordResearch {
    private Long id;
    private Long studentId;
    private String projectName;
    private String role;
    private Date startDate;
    private Date endDate;
    private String description;
    private String resultFile;
}
