package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.ForumCommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForumCommentLikeMapper extends BaseMapper<ForumCommentLike> {

    boolean exists(@Param("commentId") Long commentId, @Param("userId") Long userId);

    void deleteLike(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
