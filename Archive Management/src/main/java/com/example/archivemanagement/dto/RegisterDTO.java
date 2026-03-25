package com.example.archivemanagement.dto;

/**
 * 注册请求 DTO
 * 支持学生、导师、企业HR三种角色注册
 */
public class RegisterDTO {
    // ===== 通用字段 =====
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    /** 角色标识：student / tutor / hr */
    private String roleKey;

    // ===== 学生专属字段 =====
    private String studentNo;   // 学号
    private String college;     // 学院
    private String major;       // 专业
    private String className;   // 班级
    private Integer enrollmentYear; // 入学年份

    // ===== 导师专属字段 =====
    private String tutorNo;     // 工号
    private String title;       // 职称

    // ===== 企业HR专属字段 =====
    private String companyName; // 企业名称
    private String creditCode;  // 统一社会信用代码
    private String industry;    // 所属行业

    // ===== Getters & Setters =====
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getEnrollmentYear() { return enrollmentYear; }
    public void setEnrollmentYear(Integer enrollmentYear) { this.enrollmentYear = enrollmentYear; }

    public String getTutorNo() { return tutorNo; }
    public void setTutorNo(String tutorNo) { this.tutorNo = tutorNo; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCreditCode() { return creditCode; }
    public void setCreditCode(String creditCode) { this.creditCode = creditCode; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
}
