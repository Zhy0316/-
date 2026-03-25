package com.example.archivemanagement.controller;

import com.example.archivemanagement.dto.StudentProfileDTO;
import com.example.archivemanagement.service.InfoStudentService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final InfoStudentService infoStudentService;

    /**
     * 获取当前登录学生的完整档案（从 Token 取 userId）
     * GET /api/student/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentProfileDTO profile = infoStudentService.getStudentProfile(userId);
        if (profile == null) return ResponseEntity.status(404).body("档案不存在");
        return ResponseEntity.ok(profile);
    }

    /**
     * 按 userId 路径参数获取学生档案（导师/管理员使用）
     * GET /api/student/profile/{userId}
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        StudentProfileDTO profile = infoStudentService.getStudentProfile(userId);
        if (profile == null) return ResponseEntity.status(404).body("档案不存在");
        return ResponseEntity.ok(profile);
    }

    /**
     * 更新当前登录学生的个人信息（从 Token 取 userId）
     * PUT /api/student/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile(@RequestBody StudentProfileDTO dto,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (infoStudentService.updateStudentProfile(userId, dto)) {
            return ResponseEntity.ok("更新成功");
        }
        return ResponseEntity.badRequest().body("更新失败");
    }

    /**
     * 兼容旧接口：POST /api/student/profile/{userId}
     */
    @PostMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId,
                                           @RequestBody StudentProfileDTO dto) {
        if (infoStudentService.updateStudentProfile(userId, dto)) {
            return ResponseEntity.ok("更新成功");
        }
        return ResponseEntity.badRequest().body("更新失败");
    }

    /**
     * 上传个人简历
     * POST /api/student/resume/upload
     */
    @PostMapping("/resume/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            String filePath = FileUploadUtil.uploadFile(file, userId, "resume");
            // 更新数据库中的简历路径
            StudentProfileDTO dto = new StudentProfileDTO();
            dto.setResumeFile(filePath);
            infoStudentService.updateStudentProfile(userId, dto);
            return ResponseEntity.ok(Map.of("filePath", filePath));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传头像
     * POST /api/student/avatar/upload
     */
    @PostMapping("/avatar/upload")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            String filePath = FileUploadUtil.uploadFile(file, userId, "avatar");
            return ResponseEntity.ok(Map.of("filePath", filePath));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("上传失败：" + e.getMessage());
        }
    }
}
