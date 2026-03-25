package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.archivemanagement.entity.RecordAward;

import java.util.List;

public interface RecordAwardService extends IService<RecordAward> {
    List<RecordAward> getByStudentId(Long studentId);
}
