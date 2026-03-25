package com.example.archivemanagement.controller;

import com.example.archivemanagement.dto.RegisterDTO;
import com.example.archivemanagement.entity.InfoEnterprise;
import com.example.archivemanagement.service.EnterpriseService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 企业信息接口
 */
@RestController
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    // ==================== 企业端接口 ====================

    /**
     * 企业注册（公开接口，无需登录）
     * POST /api/enterprise/register
     */
    @PostMapping("/api/enterprise/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            return ResponseEntity.badRequest().body("企业名称不能为空");
        }
        if (enterpriseService.registerEnterprise(dto)) {
            return ResponseEntity.ok("企业注册成功，请等待管理员审核");
        }
        return ResponseEntity.badRequest().body("注册失败，用户名已存在");
    }

    /**
     * 获取当前企业信息
     * GET /api/enterprise/info
     */
    @GetMapping("/api/enterprise/info")
    public ResponseEntity<?> getInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        InfoEnterprise enterprise = enterpriseService.getByUserId(userId);
        if (enterprise == null) {
            return ResponseEntity.status(404).body("企业信息不存在");
        }
        return ResponseEntity.ok(enterprise);
    }

    /**
     * 更新企业信息（简介、行业等）
     * PUT /api/enterprise/info
     */
    @PutMapping("/api/enterprise/info")
    public ResponseEntity<?> updateInfo(@RequestBody InfoEnterprise enterprise,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (enterpriseService.updateEnterprise(userId, enterprise)) {
            return ResponseEntity.ok("更新成功");
        }
        return ResponseEntity.badRequest().body("更新失败");
    }

    /**
     * 上传营业执照
     * POST /api/enterprise/license/upload
     */
    @PostMapping("/api/enterprise/license/upload")
    public ResponseEntity<?> uploadLicense(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            String filePath = FileUploadUtil.uploadFile(file, userId, "license");
            enterpriseService.updateLicenseFile(userId, filePath);
            return ResponseEntity.ok(Map.of("filePath", filePath));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("上传失败：" + e.getMessage());
        }
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员：获取所有企业列表
     * GET /api/admin/enterprise/list
     */
    @GetMapping("/api/admin/enterprise/list")
    public ResponseEntity<List<InfoEnterprise>> listAll() {
        return ResponseEntity.ok(enterpriseService.listAll());
    }

    /**
     * 管理员：审核企业认证
     * PUT /api/admin/enterprise/audit
     * body: { "userId": 1, "auditStatus": 1 }
     */
    @PutMapping("/api/admin/enterprise/audit")
    public ResponseEntity<?> audit(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Integer auditStatus = Integer.valueOf(body.get("auditStatus").toString());
        if (auditStatus != 1 && auditStatus != 2) {
            return ResponseEntity.badRequest().body("审核状态值无效（1=通过，2=驳回）");
        }
        if (enterpriseService.audit(userId, auditStatus)) {
            return ResponseEntity.ok(auditStatus == 1 ? "审核通过" : "已驳回");
        }
        return ResponseEntity.badRequest().body("操作失败，企业不存在");
    }
}
