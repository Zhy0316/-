package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.archivemanagement.entity.RecordDiary;

import java.util.Date;
import java.util.List;

public interface RecordDiaryService extends IService<RecordDiary> {

    List<RecordDiary> getRecords(Long studentId, Date startDate, Date endDate);

    /** 带筛选条件查询 */
    List<RecordDiary> getRecordsWithFilter(Long studentId, Boolean starred,
                                           String category, String tag,
                                           String keyword, Integer visibility);

    /** 获取该学生所有分组 */
    List<String> getCategories(Long studentId);

    /** 获取该学生所有标签（已去重） */
    List<String> getTags(Long studentId);

    /** 切换星标状态 */
    boolean toggleStar(Long id, boolean star);
}
