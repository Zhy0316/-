package com.example.archivemanagement.service;

import com.example.archivemanagement.entity.RecordAcademic;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface RecordAcademicService {
    List<RecordAcademic> getByStudentId(Long studentId);
    RecordAcademic addRecord(RecordAcademic record);
    RecordAcademic updateRecord(RecordAcademic record);
    void deleteRecord(Long id);

    /** Excel 批量导入成绩 */
    int importFromExcel(Long studentId, MultipartFile file) throws Exception;

    /** 导出成绩为 Excel 字节流 */
    byte[] exportToExcel(Long studentId) throws IOException;

    /** 按学年返回绩点趋势数据（供 ECharts 使用） */
    List<Map<String, Object>> getGpaTrend(Long studentId);
}
