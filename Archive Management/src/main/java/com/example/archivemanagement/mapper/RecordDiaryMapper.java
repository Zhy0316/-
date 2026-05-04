package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordDiary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface RecordDiaryMapper extends BaseMapper<RecordDiary> {

    /**
     * 根据学生ID查询所有日记
     */
    List<RecordDiary> selectByStudentId(@Param("studentId") Long studentId);

    List<RecordDiary> selectByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /** 带全部筛选条件查询 */
    List<RecordDiary> selectWithFilter(
            @Param("studentId") Long studentId,
            @Param("starred") Boolean starred,
            @Param("category") String category,
            @Param("tag") String tag,
            @Param("keyword") String keyword,
            @Param("visibility") Integer visibility);

    /** 查询该学生所有分组名称 */
    List<String> selectCategories(@Param("studentId") Long studentId);

    /** 查询该学生所有 tags 原始字符串（前端解析去重） */
    List<String> selectAllTags(@Param("studentId") Long studentId);

    /** 切换星标 */
    int toggleStar(@Param("id") Long id, @Param("isStarred") Integer isStarred);
}
