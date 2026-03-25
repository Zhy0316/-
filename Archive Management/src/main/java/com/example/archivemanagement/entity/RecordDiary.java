package com.example.archivemanagement.entity;

import lombok.Data;
import java.util.Date;

@Data
public class RecordDiary {
    private Long id;
    private Long studentId;
    private String title;
    private String content;
    private String mood;
    private String attachmentPath;
    private Date recordDate;
    private Date createTime;
    private Integer visibility;
}
