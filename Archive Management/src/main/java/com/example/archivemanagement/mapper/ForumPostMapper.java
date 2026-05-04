package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    @Select("SELECT p.*, u.real_name as authorName, u.avatar as authorAvatar " +
            "FROM forum_post p " +
            "LEFT JOIN sys_user u ON p.user_id = u.user_id " +
            "WHERE p.status = 1 " +
            "ORDER BY p.is_top DESC, p.create_time DESC")
    List<Map<String, Object>> selectPostList();

    @Select("SELECT p.*, u.real_name as authorName, u.avatar as authorAvatar " +
            "FROM forum_post p " +
            "LEFT JOIN sys_user u ON p.user_id = u.user_id " +
            "WHERE p.id = #{postId}")
    Map<String, Object> selectPostDetail(@Param("postId") Long postId);

    @Select("SELECT p.*, u.real_name as authorName, u.avatar as authorAvatar " +
            "FROM forum_post p " +
            "LEFT JOIN sys_user u ON p.user_id = u.user_id " +
            "WHERE p.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY p.create_time DESC")
    List<Map<String, Object>> selectPostsByUserId(@Param("userId") Long userId);

    @Select("SELECT p.*, u.real_name as authorName, u.avatar as authorAvatar " +
            "FROM forum_post p " +
            "LEFT JOIN sys_user u ON p.user_id = u.user_id " +
            "WHERE p.status = 1 AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY p.create_time DESC")
    List<Map<String, Object>> searchPosts(@Param("keyword") String keyword);
}
