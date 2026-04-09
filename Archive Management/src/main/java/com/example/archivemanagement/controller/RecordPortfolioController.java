package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.RecordPortfolio;
import com.example.archivemanagement.service.RecordPortfolioService;
import com.example.archivemanagement.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class RecordPortfolioController {

    private final RecordPortfolioService service;

    @GetMapping("/{studentId}")
    public ResponseEntity<List<RecordPortfolio>> getPortfolioList(@PathVariable Long studentId) {
        List<RecordPortfolio> portfolioList = service.getByStudentId(studentId);
        return ResponseEntity.ok(portfolioList);
    }

    @PostMapping
    public ResponseEntity<RecordPortfolio> uploadPortfolio(
            @RequestParam("studentId") Long studentId,
            @RequestParam("title") String title,
            @RequestParam("workType") String workType,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        try {
            RecordPortfolio portfolio = new RecordPortfolio();
            portfolio.setStudentId(studentId);
            portfolio.setTitle(title);
            portfolio.setWorkType(workType);
            portfolio.setDescription(description);
            portfolio.setUploadTime(new Date());

            // 上传作品文件
            if (file != null && !file.isEmpty()) {
                String fileUrl = FileUploadUtil.uploadFile(file, studentId, "portfolio");
                portfolio.setFileUrl(fileUrl);
                
                // 如果是压缩包文件，读取保存后的文件来解析结构
                String originalFilename = file.getOriginalFilename();
                if (originalFilename != null && (originalFilename.endsWith(".zip") || originalFilename.endsWith(".rar"))) {
                    // 获取保存后的文件路径
                    String savedFilePath = FileUploadUtil.getBaseUploadDir() + studentId + "/portfolio/" + 
                        fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                    File savedFile = new File(savedFilePath);
                    if (savedFile.exists()) {
                        // 读取保存后的文件来解析结构
                        try (InputStream inputStream = new FileInputStream(savedFile);
                             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                            Map<String, Object> root = new HashMap<>();
                            root.put("name", originalFilename.substring(0, originalFilename.lastIndexOf('.')));
                            root.put("type", "folder");
                            root.put("expanded", true);
                            List<Map<String, Object>> children = new ArrayList<>();
                            root.put("children", children);

                            ZipEntry entry;
                            while ((entry = zipInputStream.getNextEntry()) != null) {
                                if (!entry.isDirectory()) {
                                    String filePath = entry.getName();
                                    FileUploadUtil.addFileToTree(children, filePath);
                                }
                                zipInputStream.closeEntry();
                            }

                            String fileStructure = FileUploadUtil.buildTreeJson(root);
                            portfolio.setFileStructure(fileStructure);
                        }
                    }
                }
            }

            // 上传封面图片（转换为base64存储）
            if (cover != null && !cover.isEmpty()) {
                String coverBase64 = FileUploadUtil.fileToBase64(cover);
                portfolio.setCoverUrl(coverBase64);
            }

            boolean result = service.save(portfolio);
            if (result) {
                return ResponseEntity.ok(portfolio);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping
    public ResponseEntity<RecordPortfolio> updatePortfolio(
            @RequestParam("id") Long id,
            @RequestParam("title") String title,
            @RequestParam("workType") String workType,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        try {
            RecordPortfolio portfolio = new RecordPortfolio();
            portfolio.setId(id);
            portfolio.setTitle(title);
            portfolio.setWorkType(workType);
            portfolio.setDescription(description);

            // 获取学生ID
            RecordPortfolio existingPortfolio = service.getById(id);
            Long studentId = existingPortfolio != null ? existingPortfolio.getStudentId() : 0L;

            // 上传作品文件
            if (file != null && !file.isEmpty()) {
                String fileUrl = FileUploadUtil.uploadFile(file, studentId, "portfolio");
                portfolio.setFileUrl(fileUrl);
            }

            // 上传封面图片（转换为base64存储）
            if (cover != null && !cover.isEmpty()) {
                String coverBase64 = FileUploadUtil.fileToBase64(cover);
                portfolio.setCoverUrl(coverBase64);
            }

            boolean result = service.updateById(portfolio);
            if (result) {
                return ResponseEntity.ok(portfolio);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePortfolio(@PathVariable Long id,
                                             jakarta.servlet.http.HttpServletRequest request) {
        Long callerId = (Long) request.getAttribute("userId");
        RecordPortfolio portfolio = service.getById(id);
        if (portfolio == null) return ResponseEntity.status(404).body("作品不存在");
        if (!portfolio.getStudentId().equals(callerId)) return ResponseEntity.status(403).body("无权删除他人作品");
        service.removeById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadPortfolio(@PathVariable Long id) throws Exception {
        RecordPortfolio portfolio = service.getById(id);
        if (portfolio == null || portfolio.getFileUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 获取文件路径
        String fileUrl = portfolio.getFileUrl();
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        String filePath = FileUploadUtil.getBaseUploadDir() + portfolio.getStudentId() + "/portfolio/" + fileName;
        
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        // 设置响应头
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + portfolio.getTitle() + fileName.substring(fileName.lastIndexOf(".")) + "\"")
                .body(resource);
    }
}
