package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.archivemanagement.entity.DictAwardLevel;
import com.example.archivemanagement.mapper.DictAwardLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获奖等级字典 Service
 */
@Service
@RequiredArgsConstructor
public class DictAwardLevelService {
    
    private final DictAwardLevelMapper awardLevelMapper;
    
    /**
     * 获取所有获奖等级(按排序)
     */
    public List<DictAwardLevel> listAll() {
        QueryWrapper<DictAwardLevel> qw = new QueryWrapper<>();
        qw.orderByAsc("sort_order");
        return awardLevelMapper.selectList(qw);
    }
    
    /**
     * 根据ID获取等级
     */
    public DictAwardLevel getById(Long id) {
        return awardLevelMapper.selectById(id);
    }
    
    /**
     * 根据等级代码获取等级
     */
    public DictAwardLevel getByCode(String levelCode) {
        QueryWrapper<DictAwardLevel> qw = new QueryWrapper<>();
        qw.eq("level_code", levelCode);
        return awardLevelMapper.selectOne(qw);
    }
}
