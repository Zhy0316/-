package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.DictDiaryTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日记标签字典Mapper
 */
@Mapper
public interface DictDiaryTagMapper extends BaseMapper<DictDiaryTag> {
    
    /**
     * 根据标签类型查询标签列表
     */
    List<DictDiaryTag> selectByType(@Param("tagType") String tagType);
    
    /**
     * 查询所有系统标签
     */
    List<DictDiaryTag> selectSystemTags();
}
