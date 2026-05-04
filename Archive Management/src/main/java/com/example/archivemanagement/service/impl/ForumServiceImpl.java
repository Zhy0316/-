package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.dto.CreatePostDTO;
import com.example.archivemanagement.dto.PostResourceDTO;
import com.example.archivemanagement.dto.UpdatePostDTO;
import com.example.archivemanagement.entity.*;
import com.example.archivemanagement.mapper.*;
import com.example.archivemanagement.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {

    private final ForumPostMapper postMapper;
    private final ForumPostResourceMapper resourceMapper;
    private final ForumPostLikeMapper likeMapper;
    private final ForumPostCollectMapper collectMapper;
    private final ForumPostCommentMapper commentMapper;
    private final ForumCommentLikeMapper commentLikeMapper;
    private final ForumPostViewMapper viewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPost(Long userId, String userRole, CreatePostDTO dto) {
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setUserRole(userRole);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCoverImage(dto.getCoverImage());
        post.setContentType(dto.getContentType() != null ? dto.getContentType() : "article");
        post.setCategory(dto.getCategory() != null ? dto.getCategory() : "学习分享");
        post.setTags(dto.getTags());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCollectCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(1);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());

        postMapper.insert(post);

        if (dto.getResources() != null && !dto.getResources().isEmpty()) {
            for (PostResourceDTO resourceDTO : dto.getResources()) {
                ForumPostResource resource = new ForumPostResource();
                resource.setPostId(post.getId());
                resource.setResourceType(resourceDTO.getResourceType());
                resource.setFileName(resourceDTO.getFileName());
                resource.setFileUrl(resourceDTO.getFileUrl());
                resource.setFileSize(resourceDTO.getFileSize());
                resource.setFileExt(resourceDTO.getFileExt());
                resource.setThumbnailUrl(resourceDTO.getThumbnailUrl());
                resource.setDuration(resourceDTO.getDuration());
                resource.setSortOrder(resourceDTO.getSortOrder() != null ? resourceDTO.getSortOrder() : 0);
                resource.setCreateTime(LocalDateTime.now());
                resourceMapper.insert(resource);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", post.getId());
        result.put("title", post.getTitle());
        result.put("createTime", post.getCreateTime());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePost(Long postId, Long userId, UpdatePostDTO dto) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
            return false;
        }

        if (dto.getTitle() != null) post.setTitle(dto.getTitle());
        if (dto.getContent() != null) post.setContent(dto.getContent());
        if (dto.getCoverImage() != null) post.setCoverImage(dto.getCoverImage());
        if (dto.getContentType() != null) post.setContentType(dto.getContentType());
        if (dto.getCategory() != null) post.setCategory(dto.getCategory());
        if (dto.getTags() != null) post.setTags(dto.getTags());
        if (dto.getStatus() != null) post.setStatus(dto.getStatus());
        post.setUpdateTime(LocalDateTime.now());

        postMapper.updateById(post);

        if (dto.getResources() != null) {
            resourceMapper.deleteByPostId(postId);
            for (PostResourceDTO resourceDTO : dto.getResources()) {
                ForumPostResource resource = new ForumPostResource();
                resource.setPostId(postId);
                resource.setResourceType(resourceDTO.getResourceType());
                resource.setFileName(resourceDTO.getFileName());
                resource.setFileUrl(resourceDTO.getFileUrl());
                resource.setFileSize(resourceDTO.getFileSize());
                resource.setFileExt(resourceDTO.getFileExt());
                resource.setThumbnailUrl(resourceDTO.getThumbnailUrl());
                resource.setDuration(resourceDTO.getDuration());
                resource.setSortOrder(resourceDTO.getSortOrder() != null ? resourceDTO.getSortOrder() : 0);
                resource.setCreateTime(LocalDateTime.now());
                resourceMapper.insert(resource);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long postId, Long userId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
            return false;
        }
        post.setStatus(2);
        postMapper.updateById(post);
        return true;
    }

    @Override
    public Map<String, Object> getPostDetail(Long postId, Long userId) {
        Map<String, Object> post = postMapper.selectPostDetail(postId);
        if (post == null) {
            return null;
        }

        List<ForumPostResource> resources = resourceMapper.selectByPostId(postId);
        post.put("resources", resources);

        if (userId != null) {
            boolean hasLiked = likeMapper.exists(postId, userId);
            boolean hasCollected = collectMapper.exists(postId, userId);
            post.put("hasLiked", hasLiked);
            post.put("hasCollected", hasCollected);
        }

        return post;
    }

    @Override
    public List<Map<String, Object>> getPostList() {
        List<Map<String, Object>> posts = postMapper.selectPostList();
        return posts;
    }

    @Override
    public List<Map<String, Object>> getUserPosts(Long userId) {
        return postMapper.selectPostsByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getUserCollections(Long userId) {
        return collectMapper.selectUserCollections(userId);
    }

    @Override
    public List<Map<String, Object>> getUserLikedPosts(Long userId) {
        return likeMapper.selectUserLikedPosts(userId);
    }

    @Override
    public List<Map<String, Object>> searchPosts(String keyword) {
        return postMapper.searchPosts(keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likePost(Long postId, Long userId) {
        if (likeMapper.exists(postId, userId)) {
            return false;
        }

        ForumPostLike like = new ForumPostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        likeMapper.insert(like);

        ForumPost post = postMapper.selectById(postId);
        post.setLikeCount(post.getLikeCount() + 1);
        postMapper.updateById(post);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikePost(Long postId, Long userId) {
        likeMapper.deleteLike(postId, userId);

        ForumPost post = postMapper.selectById(postId);
        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postMapper.updateById(post);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectPost(Long postId, Long userId) {
        if (collectMapper.exists(postId, userId)) {
            return false;
        }

        ForumPostCollect collect = new ForumPostCollect();
        collect.setPostId(postId);
        collect.setUserId(userId);
        collect.setCreateTime(LocalDateTime.now());
        collectMapper.insert(collect);

        ForumPost post = postMapper.selectById(postId);
        post.setCollectCount(post.getCollectCount() + 1);
        postMapper.updateById(post);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uncollectPost(Long postId, Long userId) {
        collectMapper.deleteCollect(postId, userId);

        ForumPost post = postMapper.selectById(postId);
        if (post.getCollectCount() > 0) {
            post.setCollectCount(post.getCollectCount() - 1);
            postMapper.updateById(post);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addComment(Long postId, Long userId, String content, Long parentId) {
        ForumPostComment comment = new ForumPostComment();
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        ForumPost post = postMapper.selectById(postId);
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);

        Map<String, Object> result = new HashMap<>();
        result.put("id", comment.getId());
        result.put("content", comment.getContent());
        result.put("createTime", comment.getCreateTime());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId, Long userId) {
        ForumPostComment comment = commentMapper.selectById(commentId);
        if (comment == null || !comment.getUserId().equals(userId)) {
            return false;
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);

        ForumPost post = postMapper.selectById(comment.getPostId());
        if (post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postMapper.updateById(post);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId, Long userId) {
        if (commentLikeMapper.exists(commentId, userId)) {
            return false;
        }

        ForumCommentLike like = new ForumCommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        commentLikeMapper.insert(like);

        ForumPostComment comment = commentMapper.selectById(commentId);
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentMapper.updateById(comment);

        return true;
    }

    @Override
    public List<Map<String, Object>> getComments(Long postId) {
        return commentMapper.selectByPostId(postId);
    }
}
