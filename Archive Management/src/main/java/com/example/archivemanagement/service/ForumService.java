package com.example.archivemanagement.service;

import com.example.archivemanagement.dto.CreatePostDTO;
import com.example.archivemanagement.dto.UpdatePostDTO;
import java.util.List;
import java.util.Map;

public interface ForumService {

    Map<String, Object> createPost(Long userId, String userRole, CreatePostDTO dto);

    boolean updatePost(Long postId, Long userId, UpdatePostDTO dto);

    boolean deletePost(Long postId, Long userId);

    Map<String, Object> getPostDetail(Long postId, Long userId);

    List<Map<String, Object>> getPostList();

    List<Map<String, Object>> getUserPosts(Long userId);

    List<Map<String, Object>> getUserCollections(Long userId);

    List<Map<String, Object>> getUserLikedPosts(Long userId);

    List<Map<String, Object>> searchPosts(String keyword);

    boolean likePost(Long postId, Long userId);

    boolean unlikePost(Long postId, Long userId);

    boolean collectPost(Long postId, Long userId);

    boolean uncollectPost(Long postId, Long userId);

    Map<String, Object> addComment(Long postId, Long userId, String content, Long parentId);

    boolean deleteComment(Long commentId, Long userId);

    boolean likeComment(Long commentId, Long userId);

    List<Map<String, Object>> getComments(Long postId);
}
