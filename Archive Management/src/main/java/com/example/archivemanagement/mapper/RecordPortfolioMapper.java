package com.example.archivemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.archivemanagement.entity.RecordPortfolio;

import java.util.List;

public interface RecordPortfolioMapper extends BaseMapper<RecordPortfolio> {
    List<RecordPortfolio> selectByStudentId(Long studentId);
    RecordPortfolio selectById(Long id);
}
