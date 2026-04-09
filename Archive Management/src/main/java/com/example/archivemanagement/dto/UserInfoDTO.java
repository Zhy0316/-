package com.example.archivemanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfoDTO {
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String roleKey;
    private String roleName;
    private LocalDateTime createTime;
    /** 登录成功后返回的 JWT Token */
    private String token;
}
