package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.RecordAcademic;
import com.example.archivemanagement.service.RecordAcademicService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /** 查询成绩列表（支持按 studentId 路径参数，或从 Token 取当前用户） */
    @GetMapping("/{studentId}")
    public ResponseEntity<List<RecordAcademic>> getByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    /** Excel 批量导入成绩 */
    @PostMapping("/import/{studentId}")
    public ResponseEntity<?> importExcel(@PathVariable Long studentId,
                                         @RequestParam("file") MultipartFile file) {
        try {
            int count = service.importFromExcel(studentId, file);
            return ResponseEntity.ok(Map.of("message", "成绩导入成功，共导入 " + count + " 条", "count", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "导入失败: " + e.getMessage()));
        }
    }

    /** 兼容旧接口路径 /upload/{studentId} */
    @PostMapping("/upload/{studentId}")
    public ResponseEntity<?> uploadExcel(@PathVariable Long studentId,
                                         @RequestParam("file") MultipartFile file) {
        return importExcel(studentId, file);
    }

    /** 导出成绩 Excel */
    @GetMapping("/export/{studentId}")
    public void exportExcel(@PathVariable Long studentId, HttpServletResponse response) {
        try {
            byte[] data = service.exportToExcel(studentId);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=academic_" + studentId + ".xlsx");
            response.getOutputStream().write(data);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** 绩点趋势数据（按学年，供 ECharts 折线图使用） */
    @GetMapping("/gpa-trend/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> gpaTrend(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getGpaTrend(studentId));
    }

    /** 新增成绩记录 */
    @PostMapping
    public ResponseEntity<RecordAcademic> add(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(service.addRecord(mapToRecord(request)));
    }

    /** 更新成绩记录 */
    @PutMapping
    public ResponseEntity<RecordAcademic> update(@RequestBody Map<String, Object> request) {
        RecordAcademic record = mapToRecord(request);
        if (request.containsKey("id")) {
            record.setId(Long.parseLong(request.get("id").toString()));
        }
        return ResponseEntity.ok(service.updateRecord(record));
    }

    /** 删除成绩记录 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteRecord(id);
        return ResponseEntity.ok().build();
    }

    /** 将请求 Map 映射为实体 */
    private RecordAcademic mapToRecord(Map<String, Object> req) {
        RecordAcademic r = new RecordAcademic();
        if (req.containsKey("studentId")) r.setStudentId(Long.parseLong(req.get("studentId").toString()));
        if (req.containsKey("semester"))  r.setAcademicYear(req.get("semester").toString());
        if (req.containsKey("academicYear")) r.setAcademicYear(req.get("academicYear").toString());
        if (req.containsKey("courseType")) r.setCourseNature(req.get("courseType").toString());
        if (req.containsKey("courseNature")) r.setCourseNature(req.get("courseNature").toString());
        if (req.containsKey("courseName")) r.setCourseName(req.get("courseName").toString());
        if (req.containsKey("credit"))    r.setCredit(new BigDecimal(req.get("credit").toString()));
        if (req.containsKey("score"))     r.setScore(req.get("score").toString());
        if (req.containsKey("gpa"))       r.setGpa(new BigDecimal(req.get("gpa").toString()));
        if (req.containsKey("gpaPoint"))  r.setGpa(new BigDecimal(req.get("gpaPoint").toString()));
        if (req.containsKey("isRetake")) {
            int v = Integer.parseInt(req.get("isRetake").toString());
            r.setIsInvalidated(v == 1 ? "是" : "否");
        }
        if (req.containsKey("isInvalidated")) r.setIsInvalidated(req.get("isInvalidated").toString());
        if (r.getCredit() != null && r.getGpa() != null) {
            r.setCreditGpa(r.getCredit().multiply(r.getGpa()));
        }
        return r;
    }
}
