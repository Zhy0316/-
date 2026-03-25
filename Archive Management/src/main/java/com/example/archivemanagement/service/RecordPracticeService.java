package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.archivemanagement.entity.RecordPractice;

import java.util.List;

public interface RecordPracticeService extends IService<RecordPractice> {
    List<RecordPractice> getByStudentId(Long studentId);
}
