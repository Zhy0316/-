package com.example.archivemanagement.service;

import com.example.archivemanagement.entity.RecordAcademic;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RecordAcademicService {
    List<RecordAcademic> getByStudentId(Long studentId);
    RecordAcademic addRecord(RecordAcademic record);
    RecordAcademic updateRecord(RecordAcademic record);
    void deleteRecord(Long id);
    int importFromExcel(Long studentId, MultipartFile file) throws Exception;
}
