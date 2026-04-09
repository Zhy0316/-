package com.example.archivemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO（学生 / 导师 / 企业HR 三种角色通用）
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为 3~20 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度为 6~30 位")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String email;

    @NotBlank(message = "角色不能为空")
    private String roleKey;

    // ===== 学生专属 =====
    private String studentNo;
    private String college;
    private String major;
    private String className;
    private Integer enrollmentYear;

    // ===== 导师专属 =====
    private String tutorNo;
    private String title;

    // ===== 企业HR专属 =====
    private String companyName;
    private String creditCode;
    private String industry;
}
