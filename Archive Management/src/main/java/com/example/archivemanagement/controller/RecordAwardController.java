package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.RecordAward;
import com.example.archivemanagement.service.RecordAwardService;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

import java.util.List;

@RestController
@RequestMapping("/api/award")
@RequiredArgsConstructor
public class RecordAwardController {

    private final RecordAwardService service;

    @GetMapping("/{studentId}")
    public ResponseEntity<List<RecordAward>> getAwardList(@PathVariable Long studentId) {
        List<RecordAward> awardList = service.getByStudentId(studentId);
        return ResponseEntity.ok(awardList);
    }

    @PostMapping
    public ResponseEntity<RecordAward> uploadAward(
            @RequestParam("studentId") Long studentId,
            @RequestParam("awardName") String awardName,
            @RequestParam("awardLevel") String awardLevel,
            @RequestParam("issuingAuthority") String issuingAuthority,
            @RequestParam("awardDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date awardDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            RecordAward award = new RecordAward();
            award.setStudentId(studentId);
            award.setAwardName(awardName);
            award.setAwardLevel(awardLevel);
            award.setIssuingAuthority(issuingAuthority);
            award.setAwardDate(awardDate);
            award.setDescription(description);
            award.setAuditStatus(0); // 默认待审核

            // 上传证书文件
            if (file != null && !file.isEmpty()) {
                String certFile = FileUploadUtil.uploadFile(file, studentId, "award");
                award.setCertFile(certFile);
            }

            boolean result = service.save(award);
            if (result) {
                return ResponseEntity.ok(award);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAward(@PathVariable Long id) {
        service.removeById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 导师审核获奖记录
     * PUT /api/award/audit/{id}
     * body: { "auditStatus": 1 }  1=通过，0=驳回
     */
    @PutMapping("/audit/{id}")
    public ResponseEntity<?> auditAward(@PathVariable Long id,
                                        @RequestBody java.util.Map<String, Object> body) {
        RecordAward award = service.getById(id);
        if (award == null) return ResponseEntity.status(404).body("记录不存在");
        Integer auditStatus = Integer.valueOf(body.get("auditStatus").toString());
        award.setAuditStatus(auditStatus);
        service.updateById(award);
        return ResponseEntity.ok(auditStatus == 1 ? "审核通过" : "已驳回");
    }

    @PutMapping
    public ResponseEntity<RecordAward> updateAward(
            @RequestParam("id") Long id,
            @RequestParam("awardName") String awardName,
            @RequestParam("awardLevel") String awardLevel,
            @RequestParam("issuingAuthority") String issuingAuthority,
            @RequestParam("awardDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date awardDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            RecordAward award = new RecordAward();
            award.setId(id);
            award.setAwardName(awardName);
            award.setAwardLevel(awardLevel);
            award.setIssuingAuthority(issuingAuthority);
            award.setAwardDate(awardDate);
            award.setDescription(description);

            // 上传证书文件
            if (file != null && !file.isEmpty()) {
                // 获取学生ID
                RecordAward existingAward = service.getById(id);
                Long studentId = existingAward != null ? existingAward.getStudentId() : 0L;
                
                String certFile = FileUploadUtil.uploadFile(file, studentId, "award");
                award.setCertFile(certFile);
            }

            boolean result = service.updateById(award);
            if (result) {
                return ResponseEntity.ok(award);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
