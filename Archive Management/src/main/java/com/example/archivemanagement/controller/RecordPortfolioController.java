package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.RecordPortfolio;
import com.example.archivemanagement.service.RecordPortfolioService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
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
    public Result<List<RecordPortfolio>> getPortfolioList(@PathVariable Long studentId) {
        return Result.ok(service.getByStudentId(studentId));
    }

    @PostMapping("/meta")
    public ResponseEntity<RecordPortfolio> savePortfolioMeta(
            @RequestParam("studentId") Long studentId,
            @RequestParam("title") String title,
            @RequestParam("workType") String workType,
            @RequestParam("description") String description,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        try {
            RecordPortfolio portfolio = new RecordPortfolio();
            portfolio.setStudentId(studentId);
            portfolio.setTitle(title);
            portfolio.setWorkType(workType);
            portfolio.setDescription(description);
            portfolio.setUploadTime(new Date());

            if (cover != null && !cover.isEmpty()) {
                String coverBase64 = FileUploadUtil.fileToBase64(cover);
                portfolio.setCoverUrl(coverBase64);
            }

            service.save(portfolio);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{id}/file")
    public ResponseEntity<RecordPortfolio> uploadPortfolioFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            RecordPortfolio existing = service.getById(id);
            if (existing == null) return ResponseEntity.notFound().build();

            String fileUrl = FileUploadUtil.uploadFile(file, existing.getStudentId(), "portfolio");

            RecordPortfolio update = new RecordPortfolio();
            update.setId(id);
            update.setFileUrl(fileUrl);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && (originalFilename.endsWith(".zip") || originalFilename.endsWith(".rar"))) {
                String savedFilePath = FileUploadUtil.getBaseUploadDir() + existing.getStudentId() + "/portfolio/" +
                        fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                File savedFile = new File(savedFilePath);
                if (savedFile.exists()) {
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
                            if (!entry.isDirectory()) FileUploadUtil.addFileToTree(children, entry.getName());
                            zipInputStream.closeEntry();
                        }
                        update.setFileStructure(FileUploadUtil.buildTreeJson(root));
                    }
                }
            }

            service.updateById(update);
            return ResponseEntity.ok(service.getById(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
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

            if (file != null && !file.isEmpty()) {
                String fileUrl = FileUploadUtil.uploadFile(file, studentId, "portfolio");
                portfolio.setFileUrl(fileUrl);

                String originalFilename = file.getOriginalFilename();
                if (originalFilename != null && (originalFilename.endsWith(".zip") || originalFilename.endsWith(".rar"))) {
                    String savedFilePath = FileUploadUtil.getBaseUploadDir() + studentId + "/portfolio/" +
                            fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                    File savedFile = new File(savedFilePath);
                    if (savedFile.exists()) {
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
                                if (!entry.isDirectory()) FileUploadUtil.addFileToTree(children, entry.getName());
                                zipInputStream.closeEntry();
                            }
                            portfolio.setFileStructure(FileUploadUtil.buildTreeJson(root));
                        }
                    }
                }
            }

            if (cover != null && !cover.isEmpty()) {
                portfolio.setCoverUrl(FileUploadUtil.fileToBase64(cover));
            }

            service.save(portfolio);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadPortfolio(@PathVariable Long id) throws Exception {
        RecordPortfolio portfolio = service.getById(id);
        if (portfolio == null || portfolio.getFileUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        String fileUrl = portfolio.getFileUrl();
        Path path;
        if (fileUrl.startsWith("/uploads/") || fileUrl.startsWith("\\uploads\\")) {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            path = Paths.get(FileUploadUtil.getBaseUploadDir() + portfolio.getStudentId() + "/portfolio/" + fileName);
        } else {
            path = Paths.get(fileUrl);
        }

        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = "application/octet-stream";

        String fileName = path.getFileName().toString();
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + portfolio.getTitle() + ext + "\"")
                .body(resource);
    }

    @PostMapping("/path")
    public ResponseEntity<RecordPortfolio> saveByLocalPath(
            @RequestParam("studentId") Long studentId,
            @RequestParam("title") String title,
            @RequestParam("workType") String workType,
            @RequestParam("description") String description,
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {
        try {
            RecordPortfolio portfolio = new RecordPortfolio();
            portfolio.setStudentId(studentId);
            portfolio.setTitle(title);
            portfolio.setWorkType(workType);
            portfolio.setDescription(description);
            portfolio.setUploadTime(new Date());
            portfolio.setFileUrl(filePath);

            if (cover != null && !cover.isEmpty()) {
                portfolio.setCoverUrl(FileUploadUtil.fileToBase64(cover));
            }

            service.save(portfolio);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
