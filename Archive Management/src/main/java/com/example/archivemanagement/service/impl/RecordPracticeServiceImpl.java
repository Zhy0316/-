package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.RecordPractice;
import com.example.archivemanagement.mapper.RecordPracticeMapper;
import com.example.archivemanagement.service.RecordPracticeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordPracticeServiceImpl extends ServiceImpl<RecordPracticeMapper, RecordPractice> implements RecordPracticeService {

    @Override
    public List<RecordPractice> getByStudentId(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }
}
