package com.example.archivemanagement.controller;

import com.example.archivemanagement.dto.StudentProfileDTO;
import com.example.archivemanagement.service.InfoStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final InfoStudentService infoStudentService;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload-resume")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            File dest = new File(dir, filename);
            file.transferTo(dest);
            return ResponseEntity.ok("/uploads/" + filename);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("上传失败");
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<StudentProfileDTO> getProfile(@PathVariable Long userId) {
        StudentProfileDTO profile = infoStudentService.getStudentProfile(userId);
        if (profile != null) {
            return ResponseEntity.ok(profile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/profile/{userId}")
    public ResponseEntity<String> updateProfile(@PathVariable Long userId, @RequestBody StudentProfileDTO profileDTO) {
        if (infoStudentService.updateStudentProfile(userId, profileDTO)) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.badRequest().body("更新失败");
        }
    }
}
