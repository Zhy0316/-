package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.dto.StudentProfileDTO;
import com.example.archivemanagement.service.InfoStudentService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final InfoStudentService infoStudentService;

    @GetMapping("/profile")
    public Result<StudentProfileDTO> getMyProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentProfileDTO profile = infoStudentService.getStudentProfile(userId);
        if (profile == null) throw BusinessException.notFound("档案不存在");
        return Result.ok(profile);
    }

    @GetMapping("/profile/{userId}")
    public Result<StudentProfileDTO> getProfile(@PathVariable Long userId,
                                                HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        String roleKey = (String) request.getAttribute("roleKey");
        if ("student".equals(roleKey) && !userId.equals(callerId)) {
            throw BusinessException.forbidden("无权查看其他学生的档案");
        }
        StudentProfileDTO profile = infoStudentService.getStudentProfile(userId);
        if (profile == null) throw BusinessException.notFound("档案不存在");
        return Result.ok(profile);
    }

    @PutMapping("/profile")
    public Result<Void> updateMyProfile(@Valid @RequestBody StudentProfileDTO dto,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (!infoStudentService.updateStudentProfile(userId, dto)) throw new BusinessException("更新失败");
        return Result.ok("更新成功");
    }

    @PostMapping("/profile/{userId}")
    public Result<Void> updateProfile(@PathVariable Long userId,
                                      @Valid @RequestBody StudentProfileDTO dto,
                                      HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        String roleKey = (String) request.getAttribute("roleKey");
        if ("student".equals(roleKey) && !userId.equals(callerId)) {
            throw BusinessException.forbidden("无权修改他人档案");
        }
        if (!infoStudentService.updateStudentProfile(userId, dto)) throw new BusinessException("更新失败");
        return Result.ok("更新成功");
    }

    @PostMapping("/resume/upload")
    public Result<Map<String, String>> uploadResume(@RequestParam("file") MultipartFile file,
                                                    HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        String filePath = FileUploadUtil.uploadFile(file, userId, "resume");
        StudentProfileDTO dto = new StudentProfileDTO();
        dto.setResumeFile(filePath);
        infoStudentService.updateStudentProfile(userId, dto);
        return Result.ok(Map.of("filePath", filePath));
    }

    @PostMapping("/avatar/upload")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                    HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        String filePath = FileUploadUtil.uploadFile(file, userId, "avatar");
        return Result.ok(Map.of("filePath", filePath));
    }
}
