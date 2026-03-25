package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.archivemanagement.entity.RecordResearch;

import java.util.List;

public interface RecordResearchService extends IService<RecordResearch> {
    List<RecordResearch> getByStudentId(Long studentId);
}
