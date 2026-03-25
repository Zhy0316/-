package com.example.archivemanagement.controller;

import com.alibaba.excel.EasyExcel;
import com.example.archivemanagement.entity.*;
import com.example.archivemanagement.mapper.*;
import com.example.archivemanagement.service.EvalGrowthScoreService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 报表导出接口
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final RecordAcademicMapper academicMapper;
    private final RecordAwardMapper awardMapper;
    private final RecordResearchMapper researchMapper;
    private final RecordPracticeMapper practiceMapper;
    private final InfoStudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final EvalGrowthScoreService growthScoreService;

    /**
     * 下载成绩导入 Excel 模板
     * GET /api/export/academic-template
     */
    @GetMapping("/academic-template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "成绩导入模板.xlsx");
        // 写入空表头模板
        EasyExcel.write(response.getOutputStream(), RecordAcademic.class)
                .sheet("成绩模板")
                .doWrite(List.of());
    }

    /**
     * 导出学生个人成长档案（Excel 格式，含成绩/获奖/科研/实践/成长分）
     * GET /api/export/student-report/{studentId}
     */
    @GetMapping("/student-report/{studentId}")
    public void exportStudentReport(@PathVariable Long studentId,
                                    HttpServletResponse response) throws IOException {
        SysUser user = userMapper.selectById(studentId);
        String name = user != null ? user.getRealName() : String.valueOf(studentId);
        setExcelResponse(response, name + "_成长档案.xlsx");

        try (var out = response.getOutputStream()) {
            var excelWriter = EasyExcel.write(out).build();

            // Sheet1：学业成绩
            List<RecordAcademic> academics = academicMapper.selectByStudentId(studentId);
            var sheet1 = EasyExcel.writerSheet(0, "学业成绩")
                    .head(RecordAcademic.class).build();
            excelWriter.write(academics, sheet1);

            // Sheet2：获奖记录
            List<RecordAward> awards = awardMapper.selectByStudentId(studentId);
            var sheet2 = EasyExcel.writerSheet(1, "获奖记录")
                    .head(RecordAward.class).build();
            excelWriter.write(awards, sheet2);

            // Sheet3：科研项目
            List<RecordResearch> researches = researchMapper.selectByStudentId(studentId);
            var sheet3 = EasyExcel.writerSheet(2, "科研项目")
                    .head(RecordResearch.class).build();
            excelWriter.write(researches, sheet3);

            // Sheet4：实践记录
            List<RecordPractice> practices = practiceMapper.selectByStudentId(studentId);
            var sheet4 = EasyExcel.writerSheet(3, "实践记录")
                    .head(RecordPractice.class).build();
            excelWriter.write(practices, sheet4);

            // Sheet5：成长评分
            List<EvalGrowthScore> scores = growthScoreService.listByStudent(studentId);
            var sheet5 = EasyExcel.writerSheet(4, "成长评分")
                    .head(EvalGrowthScore.class).build();
            excelWriter.write(scores, sheet5);

            excelWriter.finish();
        }
    }

    /**
     * 导出学院综合统计报表（Excel）
     * GET /api/export/college-report
     */
    @GetMapping("/college-report")
    public void exportCollegeReport(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "学院综合统计报表.xlsx");

        try (var out = response.getOutputStream()) {
            var excelWriter = EasyExcel.write(out).build();

            // Sheet1：学生基本信息
            List<InfoStudent> students = studentMapper.selectList(null);
            var sheet1 = EasyExcel.writerSheet(0, "学生信息")
                    .head(InfoStudent.class).build();
            excelWriter.write(students, sheet1);

            // Sheet2：成长分排行
            List<java.util.Map<String, Object>> ranking = growthScoreService.getRanking();
            // 转为简单 List 写入
            var sheet2 = EasyExcel.writerSheet(1, "成长分排行").build();
            excelWriter.write(ranking, sheet2);

            excelWriter.finish();
        }
    }

    // ==================== 私有方法 ====================

    private void setExcelResponse(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded);
        response.setCharacterEncoding("UTF-8");
    }
}
