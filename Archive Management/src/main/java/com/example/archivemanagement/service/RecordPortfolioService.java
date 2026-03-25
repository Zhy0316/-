package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.archivemanagement.entity.RecordPortfolio;

import java.util.List;

public interface RecordPortfolioService extends IService<RecordPortfolio> {
    List<RecordPortfolio> getByStudentId(Long studentId);
}
