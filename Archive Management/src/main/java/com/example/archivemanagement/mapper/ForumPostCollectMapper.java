package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.ForumPostCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ForumPostCollectMapper extends BaseMapper<ForumPostCollect> {

    @Select("SELECT COUNT(*) > 0 FROM forum_post_collect WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean exists(@Param("postId") Long postId, @Param("userId") Long userId);

    void deleteCollect(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT p.*, u.real_name as authorName, u.avatar as authorAvatar " +
            "FROM forum_post_collect fc " +
            "LEFT JOIN forum_post p ON fc.post_id = p.id " +
            "LEFT JOIN sys_user u ON p.user_id = u.user_id " +
            "WHERE fc.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY fc.create_time DESC")
    List<Map<String, Object>> selectUserCollections(@Param("userId") Long userId);
}
