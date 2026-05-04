package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.ForumPostResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ForumPostResourceMapper extends BaseMapper<ForumPostResource> {

    List<ForumPostResource> selectByPostId(@Param("postId") Long postId);

    void deleteByPostId(@Param("postId") Long postId);
}
