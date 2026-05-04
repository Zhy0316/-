package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.archivemanagement.entity.LearnResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LearnResourceMapper extends BaseMapper<LearnResource> {

    /** 分页查询（含上传者姓名），支持分类和关键词过滤 */
    @Select("<script>" +
            "SELECT r.*, u.real_name AS uploaderName " +
            "FROM learn_resource r LEFT JOIN sys_user u ON u.user_id = r.uploader_id " +
            "<where>" +
            "  <if test='category != null and category != \"\"'>AND r.category = #{category}</if>" +
            "  <if test='keyword != null and keyword != \"\"'>AND r.title LIKE CONCAT('%',#{keyword},'%')</if>" +
            "</where>" +
            "ORDER BY r.create_time DESC" +
            "</script>")
    IPage<java.util.Map<String, Object>> selectPageWithUploader(
            Page<?> page,
            @Param("category") String category,
            @Param("keyword") String keyword);

    /** 点赞 +1 */
    @Update("UPDATE learn_resource SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLike(@Param("id") Long id);

    /** 浏览 +1 */
    @Update("UPDATE learn_resource SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementView(@Param("id") Long id);
}
