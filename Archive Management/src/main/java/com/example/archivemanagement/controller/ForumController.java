package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.dto.CreatePostDTO;
import com.example.archivemanagement.dto.UpdatePostDTO;
import com.example.archivemanagement.service.ForumService;
import com.example.archivemanagement.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @PostMapping("/post")
    public Result<Map<String, Object>> createPost(@RequestBody CreatePostDTO dto,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("roleKey");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        Map<String, Object> result = forumService.createPost(userId, userRole, dto);
        return Result.success(result, "发布成功");
    }

    @PutMapping("/post/{postId}")
    public Result<Void> updatePost(@PathVariable Long postId,
                                   @RequestBody UpdatePostDTO dto,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (!forumService.updatePost(postId, userId, dto)) {
            throw new BusinessException("修改失败，无权限");
        }
        return Result.success(null, "修改成功");
    }

    @DeleteMapping("/post/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (!forumService.deletePost(postId, userId)) {
            throw new BusinessException("删除失败，无权限");
        }
        return Result.success(null, "删除成功");
    }

    @GetMapping("/post/{postId}")
    public Result<Map<String, Object>> getPostDetail(@PathVariable Long postId,
                                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> post = forumService.getPostDetail(postId, userId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        return Result.success(post);
    }

    @GetMapping("/posts")
    public Result<List<Map<String, Object>>> getPostList() {
        return Result.success(forumService.getPostList());
    }

    @GetMapping("/user/posts")
    public Result<List<Map<String, Object>>> getUserPosts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return Result.success(forumService.getUserPosts(userId));
    }

    @GetMapping("/user/collections")
    public Result<List<Map<String, Object>>> getUserCollections(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return Result.success(forumService.getUserCollections(userId));
    }

    @GetMapping("/user/liked")
    public Result<List<Map<String, Object>>> getUserLikedPosts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return Result.success(forumService.getUserLikedPosts(userId));
    }

    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchPosts(@RequestParam String keyword) {
        return Result.success(forumService.searchPosts(keyword));
    }

    @PostMapping("/like/{postId}")
    public Result<Void> likePost(@PathVariable Long postId,
                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (!forumService.likePost(postId, userId)) {
            throw new BusinessException("已点赞");
        }
        return Result.success(null, "点赞成功");
    }

    @DeleteMapping("/like/{postId}")
    public Result<Void> unlikePost(@PathVariable Long postId,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        forumService.unlikePost(postId, userId);
        return Result.success(null, "取消点赞成功");
    }

    @PostMapping("/collect/{postId}")
    public Result<Void> collectPost(@PathVariable Long postId,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (!forumService.collectPost(postId, userId)) {
            throw new BusinessException("已收藏");
        }
        return Result.success(null, "收藏成功");
    }

    @DeleteMapping("/collect/{postId}")
    public Result<Void> uncollectPost(@PathVariable Long postId,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        forumService.uncollectPost(postId, userId);
        return Result.success(null, "取消收藏成功");
    }

    @PostMapping("/comment/{postId}")
    public Result<Map<String, Object>> addComment(@PathVariable Long postId,
                                                  @RequestBody Map<String, Object> body,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        String content = (String) body.get("content");
        Long parentId = body.containsKey("parentId") && body.get("parentId") != null
                ? Long.parseLong(body.get("parentId").toString()) : null;
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        Map<String, Object> result = forumService.addComment(postId, userId, content, parentId);
        return Result.success(result, "评论成功");
    }

    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (!forumService.deleteComment(commentId, userId)) {
            throw new BusinessException("删除失败，无权限");
        }
        return Result.success(null, "删除成功");
    }

    @PostMapping("/comment/{commentId}/like")
    public Result<Void> likeComment(@PathVariable Long commentId,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (!forumService.likeComment(commentId, userId)) {
            throw new BusinessException("已点赞");
        }
        return Result.success(null, "点赞成功");
    }

    @GetMapping("/comment/{postId}")
    public Result<List<Map<String, Object>>> getComments(@PathVariable Long postId) {
        return Result.success(forumService.getComments(postId));
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        String filePath = FileUploadUtil.uploadFile(file, userId, "forum");
        return Result.success(Map.of("filePath", filePath, "fileName", file.getOriginalFilename()));
    }
}
