package com.example.archivemanagement.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RecordPortfolio {
    private Long id;
    private Long studentId;
    private String title;
    private String workType;
    private String description;
    private String fileUrl;
    private String coverUrl;
    private String fileStructure;
    private Date uploadTime;
}
