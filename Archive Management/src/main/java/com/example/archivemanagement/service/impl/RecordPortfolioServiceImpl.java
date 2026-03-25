package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.RecordPortfolio;
import com.example.archivemanagement.mapper.RecordPortfolioMapper;
import com.example.archivemanagement.service.RecordPortfolioService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordPortfolioServiceImpl extends ServiceImpl<RecordPortfolioMapper, RecordPortfolio> implements RecordPortfolioService {

    @Override
    public List<RecordPortfolio> getByStudentId(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }
}
