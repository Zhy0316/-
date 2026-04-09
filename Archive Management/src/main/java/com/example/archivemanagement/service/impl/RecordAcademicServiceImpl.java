package com.example.archivemanagement.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.example.archivemanagement.entity.RecordAcademic;
import com.example.archivemanagement.mapper.RecordAcademicMapper;
import com.example.archivemanagement.service.RecordAcademicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class RecordAcademicServiceImpl implements RecordAcademicService {

    @Autowired
    private RecordAcademicMapper mapper;

    @Override
    public List<RecordAcademic> getByStudentId(Long studentId) {
        return mapper.selectByStudentId(studentId);
    }

    @Override
    public RecordAcademic addRecord(RecordAcademic record) {
        mapper.insert(record);
        return record;
    }

    @Override
    public RecordAcademic updateRecord(RecordAcademic record) {
        mapper.update(record);
        return record;
    }

    @Override
    public void deleteRecord(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public int importFromExcel(Long studentId, MultipartFile file) throws Exception {
        int[] importedCount = {0};
        EasyExcel.read(file.getInputStream(), RecordAcademic.class, new ReadListener<RecordAcademic>() {
            private static final int BATCH_COUNT = 100;
            private List<RecordAcademic> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

            @Override
            public void invokeHead(Map<Integer, com.alibaba.excel.metadata.data.ReadCellData<?>> headMap,
                                   AnalysisContext context) {
                System.out.println("解析到表头: " + headMap);
            }

            @Override
            public void invoke(RecordAcademic data, AnalysisContext context) {
                // 全空行跳过
                if (data.getAcademicYear() == null && data.getCourseName() == null && data.getScore() == null) {
                    return;
                }
                // 必填字段默认值
                if (data.getCourseName() == null) data.setCourseName("未命名课程");
                if (data.getAcademicYear() == null) data.setAcademicYear("未知学年");
                if (data.getCourseNature() == null) data.setCourseNature("必修");
                if (data.getCredit() == null) data.setCredit(BigDecimal.ZERO);
                // 自动计算绩点
                if (data.getGpa() == null && data.getScore() != null) {
                    try {
                        double score = Double.parseDouble(data.getScore());
                        data.setGpa(new BigDecimal(calculateGpa(score)));
                    } catch (NumberFormatException e) {
                        data.setGpa(BigDecimal.ZERO);
                    }
                }
                if (data.getIsInvalidated() == null) data.setIsInvalidated("否");
                // 计算学分绩点
                if (data.getCredit() != null && data.getGpa() != null) {
                    data.setCreditGpa(data.getCredit().multiply(data.getGpa()));
                }
                data.setStudentId(studentId);
                cachedDataList.add(data);
                if (cachedDataList.size() >= BATCH_COUNT) {
                    saveData();
                    cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                saveData();
            }

            private void saveData() {
                for (RecordAcademic record : cachedDataList) {
                    try {
                        mapper.insert(record);
                        importedCount[0]++;
                    } catch (Exception e) {
                        System.err.println("插入失败: " + e.getMessage());
                    }
                }
                cachedDataList.clear();
            }
        }).sheet().doRead();
        return importedCount[0];
    }

    @Override
    public byte[] exportToExcel(Long studentId) throws IOException {
        List<RecordAcademic> list = mapper.selectByStudentId(studentId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, RecordAcademic.class).sheet("成绩单").doWrite(list);
        return out.toByteArray();
    }

    @Override
    public List<Map<String, Object>> getGpaTrend(Long studentId) {
        return mapper.selectGpaTrendByStudentId(studentId);
    }

    private double calculateGpa(double score) {
        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 82) return 3.3;
        if (score >= 78) return 3.0;
        if (score >= 75) return 2.7;
        if (score >= 72) return 2.3;
        if (score >= 68) return 2.0;
        if (score >= 64) return 1.5;
        if (score >= 60) return 1.0;
        return 0.0;
    }
}
