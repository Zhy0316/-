package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.RecordDiary;
import com.example.archivemanagement.mapper.RecordDiaryMapper;
import com.example.archivemanagement.service.RecordDiaryService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RecordDiaryServiceImpl extends ServiceImpl<RecordDiaryMapper, RecordDiary> implements RecordDiaryService {

    @Override
    public List<RecordDiary> getRecords(Long studentId, Date startDate, Date endDate) {
        return baseMapper.selectByStudentIdAndDateRange(studentId, startDate, endDate);
    }

    @Override
    public boolean save(RecordDiary record) {
        if (record.getRecordDate() == null) {
            record.setRecordDate(new Date());
        }
        if (record.getVisibility() == null) {
            record.setVisibility(1);
        }
        return super.save(record);
    }
}
