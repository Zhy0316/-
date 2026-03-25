package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.RecordResearch;
import com.example.archivemanagement.mapper.RecordResearchMapper;
import com.example.archivemanagement.service.RecordResearchService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordResearchServiceImpl extends ServiceImpl<RecordResearchMapper, RecordResearch> implements RecordResearchService {

    @Override
    public List<RecordResearch> getByStudentId(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }
}
