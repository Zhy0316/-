package com.example.archivemanagement.dto;

import lombok.Data;

@Data
public class PostResourceDTO {
    private String resourceType;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileExt;
    private String thumbnailUrl;
    private Integer duration;
    private Integer sortOrder;
}
