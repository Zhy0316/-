package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class InfoTutor {
    @TableId
    private Long userId;
    private String tutorNo;
    private String title;
    private String researchField;
    private String college;
    private String avatar;
    private String realName;
}
