package com.example.archivemanagement.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentProfileDTO {

    private Long userId;
    private String username;
    private String realName;

    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号格式不正确")
    private String phone;

    private String email;
    private String avatar;
    private String studentNo;
    private String gender;
    private String college;
    private String major;
    private String className;
    private Integer enrollmentYear;
    private String resumeFile;
    private String politicalStatus;
    private String nation;
    private String nativePlace;
    private LocalDate birthDate;
}
