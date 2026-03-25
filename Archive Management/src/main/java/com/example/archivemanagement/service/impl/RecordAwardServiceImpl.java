package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.RecordAward;
import com.example.archivemanagement.mapper.RecordAwardMapper;
import com.example.archivemanagement.service.RecordAwardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordAwardServiceImpl extends ServiceImpl<RecordAwardMapper, RecordAward> implements RecordAwardService {

    @Override
    public List<RecordAward> getByStudentId(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }
}
