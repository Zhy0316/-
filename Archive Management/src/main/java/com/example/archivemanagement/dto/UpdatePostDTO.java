package com.example.archivemanagement.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdatePostDTO {
    private String title;
    private String content;
    private String coverImage;
    private String contentType;
    private String category;
    private String tags;
    private List<PostResourceDTO> resources;
    private Integer status;
}
