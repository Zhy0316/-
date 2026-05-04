package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.ForumPostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ForumPostCommentMapper extends BaseMapper<ForumPostComment> {

    @Select("SELECT c.*, u.real_name as authorName, u.avatar as authorAvatar " +
            "FROM forum_post_comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.user_id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 " +
            "ORDER BY c.create_time ASC")
    List<Map<String, Object>> selectByPostId(@Param("postId") Long postId);

    List<Map<String, Object>> selectUserCollections(@Param("userId") Long userId);

    List<Map<String, Object>> selectUserLikedPosts(@Param("userId") Long userId);
}
