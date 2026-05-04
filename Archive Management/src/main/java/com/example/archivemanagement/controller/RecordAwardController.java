package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.RecordAward;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.entity.StudentTutorRelation;
import com.example.archivemanagement.mapper.SysUserMapper;
import com.example.archivemanagement.mapper.StudentTutorRelationMapper;
import com.example.archivemanagement.service.RecordAwardService;
import com.example.archivemanagement.service.TodoMessageService;
import com.example.archivemanagement.service.impl.RecordAwardServiceImpl;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@RestController
@RequestMapping("/api/award")
@RequiredArgsConstructor
public class RecordAwardController {

    private final RecordAwardService service;
    private final RecordAwardServiceImpl serviceImpl;
    private final TodoMessageService todoMessageService;
    private final SysUserMapper userMapper;
    private final StudentTutorRelationMapper relationMapper;

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
            @RequestParam(value = "awardLevelId", required = false) Long awardLevelId,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        RecordAward award = new RecordAward();
        award.setStudentId(studentId);
        award.setAwardName(awardName);
        award.setAwardLevel(awardLevel);
        award.setIssuingAuthority(issuingAuthority);
        award.setAwardDate(awardDate);
        award.setDescription(description);
        award.setAuditStatus(0);
        award.setAwardLevelId(awardLevelId);
        if (file != null && !file.isEmpty()) {
            award.setCertFile(FileUploadUtil.uploadFile(file, studentId, "award"));
        }
        if (!service.save(award)) throw new BusinessException("保存失败");
        
        // 发送消息通知导师
        SysUser student = userMapper.selectById(studentId);
        List<StudentTutorRelation> relations = relationMapper.selectByStudentIdAndStatus(studentId, 1);
        if (relations != null && !relations.isEmpty()) {
            Long tutorId = relations.get(0).getTutorId();
            todoMessageService.sendAwardApplyNotice(tutorId, student.getRealName(), awardName, award.getId());
        }
        
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
        String rejectNote = body.containsKey("rejectNote") ? body.get("rejectNote").toString() : null;
        award.setAuditStatus(auditStatus);
        award.setRejectNote(rejectNote);
        service.updateById(award);
        
        // 发送消息通知学生
        todoMessageService.sendAwardResultNotice(award.getStudentId(), award.getAwardName(), auditStatus);
        
        return Result.ok(auditStatus == 1 ? "审核通过" : "已驳回");
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
    public Result<RecordAward> updateAward(
            @RequestParam("id") Long id,
            @RequestParam("awardName") String awardName,
            @RequestParam("awardLevel") String awardLevel,
            @RequestParam("issuingAuthority") String issuingAuthority,
            @RequestParam("awardDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date awardDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "awardLevelId", required = false) Long awardLevelId,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        RecordAward award = new RecordAward();
        award.setId(id);
        award.setAwardName(awardName);
        award.setAwardLevel(awardLevel);
        award.setIssuingAuthority(issuingAuthority);
        award.setAwardDate(awardDate);
        award.setDescription(description);
        award.setAwardLevelId(awardLevelId);
        
        // 获取当前记录，用于获取学生ID和之前状态
        RecordAward existingAward = service.getById(id);
        Integer previousStatus = existingAward != null ? existingAward.getAuditStatus() : null;
        
        if (file != null && !file.isEmpty()) {
            Long studentId = existingAward != null ? existingAward.getStudentId() : 0L;
            award.setCertFile(FileUploadUtil.uploadFile(file, studentId, "award"));
        }
        
        // 如果之前被驳回，重新提交时重置状态为待审核，并清除驳回备注
        if (previousStatus != null && previousStatus == 2) {
            award.setAuditStatus(0);
            award.setRejectNote(null);
        }
        
        if (!service.updateById(award)) throw new BusinessException("更新失败");
        
        // 如果之前被驳回，重新提交后发送新消息通知导师
        if (previousStatus != null && previousStatus == 2 && existingAward != null) {
            SysUser student = userMapper.selectById(existingAward.getStudentId());
            List<StudentTutorRelation> relations = relationMapper.selectByStudentIdAndStatus(existingAward.getStudentId(), 1);
            if (relations != null && !relations.isEmpty()) {
                Long tutorId = relations.get(0).getTutorId();
                todoMessageService.sendAwardApplyNotice(tutorId, student.getRealName(), awardName, id);
            }
        }
        
        return Result.ok(award);
    }

    /**
     * 获取学生获奖总分
     */
    @GetMapping("/total-score/{studentId}")
    public Result<Integer> getTotalScore(@PathVariable Long studentId) {
        int totalScore = serviceImpl.calculateTotalScore(studentId);
        return Result.ok(totalScore);
    }

    /**
     * 获取导师待审核的获奖记录列表
     * GET /api/award/tutor-pending/{tutorId}
     */
    @GetMapping("/tutor-pending/{tutorId}")
    public Result<List<RecordAward>> getPendingByTutorId(@PathVariable Long tutorId) {
        return Result.ok(service.getPendingByTutorId(tutorId));
    }

    /**
     * 获取导师待审核的获奖记录数量
     * GET /api/award/tutor-pending-count/{tutorId}
     */
    @GetMapping("/tutor-pending-count/{tutorId}")
    public Result<Integer> countPendingByTutorId(@PathVariable Long tutorId) {
        return Result.ok(service.countPendingByTutorId(tutorId));
    }

    /**
     * 在线预览证书文件（图片直接显示，PDF 内嵌，Word 触发下载）
     * GET /api/award/preview/{id}
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<FileSystemResource> previewCert(@PathVariable Long id) {
        RecordAward award = service.getById(id);
        if (award == null || award.getCertFile() == null) return ResponseEntity.notFound().build();

        // certFile 存的是 /uploads/{userId}/award/{filename}
        String relativePath = award.getCertFile();
        String absolutePath = FileUploadUtil.getBaseUploadDir()
                + relativePath.replaceFirst("^/uploads/", "");
        File file = new File(absolutePath);
        if (!file.exists()) return ResponseEntity.notFound().build();

        FileSystemResource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        String name = file.getName().toLowerCase();

        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");
        } else if (name.endsWith(".png")) {
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");
        } else if (name.endsWith(".pdf")) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");
        } else {
            // Word 等无法内嵌，触发下载
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
        }
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
