package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.RecordPractice;
import com.example.archivemanagement.service.RecordPracticeService;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class RecordPracticeController {

    private final RecordPracticeService recordPracticeService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPractice(
            @RequestParam("studentId") Long studentId,
            @RequestParam("activityName") String activityName,
            @RequestParam("organization") String organization,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("content") String content,
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = FileUploadUtil.uploadFile(file, studentId, "practice");
            RecordPractice practice = new RecordPractice();
            practice.setStudentId(studentId);
            practice.setActivityName(activityName);
            practice.setOrganization(organization);
            practice.setStartDate(java.sql.Date.valueOf(startDate));
            practice.setEndDate(java.sql.Date.valueOf(endDate));
            practice.setContent(content);
            practice.setProofFile(filePath);
            boolean result = recordPracticeService.save(practice);
            if (result) {
                return ResponseEntity.ok("社会实践上传成功");
            } else {
                return ResponseEntity.badRequest().body("社会实践上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/list/{studentId}")
    public ResponseEntity<List<RecordPractice>> getPracticeList(@PathVariable Long studentId) {
        List<RecordPractice> practiceList = recordPracticeService.getByStudentId(studentId);
        return ResponseEntity.ok(practiceList);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePractice(@PathVariable Long id) {
        boolean result = recordPracticeService.removeById(id);
        if (result) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updatePractice(@RequestBody RecordPractice practice) {
        boolean result = recordPracticeService.updateById(practice);
        if (result) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.badRequest().body("更新失败");
        }
    }

    /**
     * 在线查看实习材料文件
     * @param id 实习材料ID
     * @return 文件内容
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<FileSystemResource> previewFile(@PathVariable Long id) {
        try {
            RecordPractice practice = recordPracticeService.getById(id);
            if (practice == null) {
                return ResponseEntity.notFound().build();
            }

            String filePath = practice.getProofFile();
            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 将相对路径转换为绝对路径
            String absolutePath = FileUploadUtil.getBaseUploadDir() + filePath.substring(9);
            
            File file = new File(absolutePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            FileSystemResource resource = new FileSystemResource(file);

            // 设置响应头，支持在线查看
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");

            // 根据文件扩展名设置不同的媒体类型
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".pdf")) {
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                // Word文档需要通过浏览器插件或在线服务查看
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.badRequest().body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
