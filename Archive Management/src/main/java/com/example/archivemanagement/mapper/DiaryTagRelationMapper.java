package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.DiaryTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日记标签关联Mapper
 */
@Mapper
public interface DiaryTagRelationMapper extends BaseMapper<DiaryTagRelation> {
    
    /**
     * 根据日记ID查询关联的标签ID列表
     */
    List<Long> selectTagIdsByDiaryId(@Param("diaryId") Long diaryId);
    
    /**
     * 根据日记ID删除所有标签关联
     */
    int deleteByDiaryId(@Param("diaryId") Long diaryId);
    
    /**
     * 批量插入标签关联
     */
    int batchInsert(@Param("relations") List<DiaryTagRelation> relations);
}
