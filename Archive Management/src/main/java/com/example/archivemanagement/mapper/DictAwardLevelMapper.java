package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.DictAwardLevel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 获奖等级字典 Mapper
 */
@Mapper
public interface DictAwardLevelMapper extends BaseMapper<DictAwardLevel> {
}
