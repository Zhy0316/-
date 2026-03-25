package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.archivemanagement.entity.RecordDiary;
import java.util.Date;
import java.util.List;

public interface RecordDiaryService extends IService<RecordDiary> {
    List<RecordDiary> getRecords(Long studentId, Date startDate, Date endDate);
}
