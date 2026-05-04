package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.dto.RegisterDTO;
import com.example.archivemanagement.entity.InfoEnterprise;
import com.example.archivemanagement.service.EnterpriseService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @PostMapping("/api/enterprise/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            throw new BusinessException("企业名称不能为空");
        }
        if (!enterpriseService.registerEnterprise(dto)) throw new BusinessException("注册失败，用户名已存在");
        return Result.ok("企业注册成功，请等待管理员审核");
    }

    @GetMapping("/api/enterprise/info")
    public Result<InfoEnterprise> getInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        InfoEnterprise enterprise = enterpriseService.getByUserId(userId);
        if (enterprise == null) throw BusinessException.notFound("企业信息不存在");
        return Result.ok(enterprise);
    }

    /** 获取认证状态（不需要认证即可访问，用于前端判断） */
    @GetMapping("/api/enterprise/audit-status")
    public Result<Integer> getAuditStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        InfoEnterprise enterprise = enterpriseService.getByUserId(userId);
        return Result.ok(enterprise != null ? enterprise.getAuditStatus() : -1);
    }

    @PutMapping("/api/enterprise/info")
    public Result<Void> updateInfo(@RequestBody InfoEnterprise enterprise,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (!enterpriseService.updateEnterprise(userId, enterprise)) throw new BusinessException("更新失败");
        return Result.ok("更新成功");
    }

    @PostMapping("/api/enterprise/license/upload")
    public Result<Map<String, String>> uploadLicense(@RequestParam("file") MultipartFile file,
                                                     HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        String filePath = FileUploadUtil.uploadFile(file, userId, "license");
        enterpriseService.updateLicenseFile(userId, filePath);
        
        // 上传执照后自动将状态设为待审核
        InfoEnterprise enterprise = enterpriseService.getByUserId(userId);
        if (enterprise != null && enterprise.getAuditStatus() != 1) {
            enterprise.setAuditStatus(0); // 设为待审核
            enterpriseService.updateEnterprise(userId, enterprise);
        }
        
        return Result.ok(Map.of("filePath", filePath));
    }

    @GetMapping("/api/admin/enterprise/list")
    public Result<List<InfoEnterprise>> listAll() {
        return Result.ok(enterpriseService.listAll());
    }

    @PutMapping("/api/admin/enterprise/audit")
    public Result<Void> audit(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Integer auditStatus = Integer.valueOf(body.get("auditStatus").toString());
        if (auditStatus != 1 && auditStatus != 2) {
            throw new BusinessException("审核状态值无效（1=通过，2=驳回）");
        }
        if (!enterpriseService.audit(userId, auditStatus)) throw BusinessException.notFound("企业不存在");
        return Result.ok(auditStatus == 1 ? "审核通过" : "已驳回");
    }
}
