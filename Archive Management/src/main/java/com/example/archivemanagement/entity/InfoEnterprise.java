package com.example.archivemanagement.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 企业信息实体（对应 info_enterprise 表）
 */
@TableName("info_enterprise")
public class InfoEnterprise {

    /** 关联 sys_user.user_id，同时作为主键 */
    @TableId
    private Long userId;

    /** 企业名称 */
    private String companyName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 所属行业 */
    private String industry;

    /** 营业执照/认证材料文件路径 */
    private String licenseFile;

    /** 认证状态：0=待审核，1=已认证，2=驳回 */
    private Integer auditStatus;

    /** 企业简介 */
    private String description;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCreditCode() { return creditCode; }
    public void setCreditCode(String creditCode) { this.creditCode = creditCode; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLicenseFile() { return licenseFile; }
    public void setLicenseFile(String licenseFile) { this.licenseFile = licenseFile; }

    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
