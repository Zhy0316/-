package com.example.archivemanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.LearnResource;
import com.example.archivemanagement.service.LearnResourceService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/learn")
@RequiredArgsConstructor
public class LearnResourceController {

    private final LearnResourceService learnResourceService;

    /**
     * 分页查询资源列表
     * GET /api/learn/list?page=1&size=12&category=&keyword=
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false)    String category,
            @RequestParam(required = false)    String keyword) {
        return Result.ok(learnResourceService.listPage(page, size, category, keyword));
    }

    /**
     * 发布资源（含可选文件上传）
     * POST /api/learn/publish  multipart/form-data
     */
    @PostMapping("/publish")
    public Result<LearnResource> publish(
            @RequestParam String title,
            @RequestParam(defaultValue = "其他") String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(required = false) MultipartFile file,
            HttpServletRequest request) throws Exception {

        Long userId   = (Long) request.getAttribute("userId");
        String roleKey = (String) request.getAttribute("roleKey");

        LearnResource resource = new LearnResource();
        resource.setTitle(title);
        resource.setCategory(category);
        resource.setDescription(description);
        resource.setLinkUrl(linkUrl);

        if (file != null && !file.isEmpty()) {
            String path = FileUploadUtil.uploadFile(file, userId, "learn");
            resource.setFileUrl(path);
        }

        return Result.ok(learnResourceService.publish(userId, roleKey, resource));
    }

    /**
     * 点赞
     * PUT /api/learn/like/{id}
     */
    @PutMapping("/like/{id}")
    public Result<Void> like(@PathVariable Long id) {
        learnResourceService.like(id);
        return Result.ok("点赞成功");
    }

    /**
     * 删除（本人或管理员）
     * DELETE /api/learn/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId   = (Long) request.getAttribute("userId");
        String roleKey = (String) request.getAttribute("roleKey");
        if (!learnResourceService.canDelete(id, userId, roleKey)) {
            throw BusinessException.forbidden("无权删除该资源");
        }
        learnResourceService.removeById(id);
        return Result.ok("删除成功");
    }
}
