package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.RecordAward;
import com.example.archivemanagement.service.RecordAwardService;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@RestController
@RequestMapping("/api/award")
@RequiredArgsConstructor
public class RecordAwardController {

    private final RecordAwardService service;

    @GetMapping("/{studentId}")
    public Result<List<RecordAward>> getAwardList(@PathVariable Long studentId) {
        return Result.ok(service.getByStudentId(studentId));
    }

    @PostMapping
    public Result<RecordAward> uploadAward(
            @RequestParam("studentId") Long studentId,
            @RequestParam("awardName") String awardName,
            @RequestParam("awardLevel") String awardLevel,
            @RequestParam("issuingAuthority") String issuingAuthority,
            @RequestParam("awardDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date awardDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        RecordAward award = new RecordAward();
        award.setStudentId(studentId);
        award.setAwardName(awardName);
        award.setAwardLevel(awardLevel);
        award.setIssuingAuthority(issuingAuthority);
        award.setAwardDate(awardDate);
        award.setDescription(description);
        award.setAuditStatus(0);
        if (file != null && !file.isEmpty()) {
            award.setCertFile(FileUploadUtil.uploadFile(file, studentId, "award"));
        }
        if (!service.save(award)) throw new BusinessException("保存失败");
        return Result.ok(award);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAward(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok("删除成功");
    }

    @PutMapping("/audit/{id}")
    public Result<Void> auditAward(@PathVariable Long id,
                                   @RequestBody Map<String, Object> body) {
        RecordAward award = service.getById(id);
        if (award == null) throw BusinessException.notFound("记录不存在");
        Integer auditStatus = Integer.valueOf(body.get("auditStatus").toString());
        award.setAuditStatus(auditStatus);
        service.updateById(award);
        return Result.ok(auditStatus == 1 ? "审核通过" : "已驳回");
    }

    @PutMapping
    public Result<RecordAward> updateAward(
            @RequestParam("id") Long id,
            @RequestParam("awardName") String awardName,
            @RequestParam("awardLevel") String awardLevel,
            @RequestParam("issuingAuthority") String issuingAuthority,
            @RequestParam("awardDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date awardDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        RecordAward award = new RecordAward();
        award.setId(id);
        award.setAwardName(awardName);
        award.setAwardLevel(awardLevel);
        award.setIssuingAuthority(issuingAuthority);
        award.setAwardDate(awardDate);
        award.setDescription(description);
        if (file != null && !file.isEmpty()) {
            RecordAward existing = service.getById(id);
            Long studentId = existing != null ? existing.getStudentId() : 0L;
            award.setCertFile(FileUploadUtil.uploadFile(file, studentId, "award"));
        }
        if (!service.updateById(award)) throw new BusinessException("更新失败");
        return Result.ok(award);
    }
}
