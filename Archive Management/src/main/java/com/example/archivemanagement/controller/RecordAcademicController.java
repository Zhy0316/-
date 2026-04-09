package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.RecordAcademic;
import com.example.archivemanagement.service.RecordAcademicService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class RecordAcademicController {

    private final RecordAcademicService service;

    @GetMapping("/{studentId}")
    public Result<List<RecordAcademic>> getByStudentId(@PathVariable Long studentId) {
        return Result.ok(service.getByStudentId(studentId));
    }

    @PostMapping("/import/{studentId}")
    public Result<Map<String, Object>> importExcel(@PathVariable Long studentId,
                                                   @RequestParam("file") MultipartFile file) throws Exception {
        int count = service.importFromExcel(studentId, file);
        return Result.ok(Map.of("message", "成绩导入成功，共导入 " + count + " 条", "count", count));
    }

    /** 兼容旧路径 */
    @PostMapping("/upload/{studentId}")
    public Result<Map<String, Object>> uploadExcel(@PathVariable Long studentId,
                                                   @RequestParam("file") MultipartFile file) throws Exception {
        return importExcel(studentId, file);
    }

    /** 导出成绩 Excel（保留 void 直接写流） */
    @GetMapping("/export/{studentId}")
    public void exportExcel(@PathVariable Long studentId, HttpServletResponse response) throws Exception {
        byte[] data = service.exportToExcel(studentId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=academic_" + studentId + ".xlsx");
        response.getOutputStream().write(data);
    }

    @GetMapping("/gpa-trend/{studentId}")
    public Result<List<Map<String, Object>>> gpaTrend(@PathVariable Long studentId) {
        return Result.ok(service.getGpaTrend(studentId));
    }

    @PostMapping
    public Result<RecordAcademic> add(@RequestBody Map<String, Object> request) {
        return Result.ok(service.addRecord(mapToRecord(request)));
    }

    @PutMapping
    public Result<RecordAcademic> update(@RequestBody Map<String, Object> request) {
        RecordAcademic record = mapToRecord(request);
        if (request.containsKey("id")) record.setId(Long.parseLong(request.get("id").toString()));
        return Result.ok(service.updateRecord(record));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.deleteRecord(id);
        return Result.ok("删除成功");
    }

    private RecordAcademic mapToRecord(Map<String, Object> req) {
        RecordAcademic r = new RecordAcademic();
        if (req.containsKey("studentId"))    r.setStudentId(Long.parseLong(req.get("studentId").toString()));
        if (req.containsKey("semester"))     r.setAcademicYear(req.get("semester").toString());
        if (req.containsKey("academicYear")) r.setAcademicYear(req.get("academicYear").toString());
        if (req.containsKey("courseType"))   r.setCourseNature(req.get("courseType").toString());
        if (req.containsKey("courseNature")) r.setCourseNature(req.get("courseNature").toString());
        if (req.containsKey("courseName"))   r.setCourseName(req.get("courseName").toString());
        if (req.containsKey("credit"))       r.setCredit(new BigDecimal(req.get("credit").toString()));
        if (req.containsKey("score"))        r.setScore(req.get("score").toString());
        if (req.containsKey("gpa"))          r.setGpa(new BigDecimal(req.get("gpa").toString()));
        if (req.containsKey("gpaPoint"))     r.setGpa(new BigDecimal(req.get("gpaPoint").toString()));
        if (req.containsKey("isRetake")) {
            r.setIsInvalidated(Integer.parseInt(req.get("isRetake").toString()) == 1 ? "是" : "否");
        }
        if (req.containsKey("isInvalidated")) r.setIsInvalidated(req.get("isInvalidated").toString());
        if (r.getCredit() != null && r.getGpa() != null) r.setCreditGpa(r.getCredit().multiply(r.getGpa()));
        return r;
    }
}
