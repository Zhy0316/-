package com.example.archivemanagement.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RecordAward {
    private Long id;
    private Long studentId;
    private String awardName;
    private String awardLevel;
    private String issuingAuthority;
    private Date awardDate;
    private String description;
    private String certFile;
    private Integer auditStatus;
}
