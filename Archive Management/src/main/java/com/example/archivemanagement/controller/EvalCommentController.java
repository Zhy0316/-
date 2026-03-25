package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.EvalComment;
import com.example.archivemanagement.service.EvalCommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 导师评语接口
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class EvalCommentController {

    private final EvalCommentService evalCommentService;

    /**
     * 导师添加评语/批注
     * POST /api/comment/save
     * body: { studentId, targetId(可选), targetType(可选), content }
     */
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Map<String, Object> body,
                                   HttpServletRequest request) {
        Long tutorId = (Long) request.getAttribute("userId");
        Long studentId = Long.valueOf(body.get("studentId").toString());
        Long targetId = body.containsKey("targetId") && body.get("targetId") != null
                ? Long.valueOf(body.get("targetId").toString()) : null;
        String targetType = body.containsKey("targetType") ? body.get("targetType").toString() : "GENERAL";
        String content = body.get("content").toString();

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body("评语内容不能为空");
        }
        EvalComment comment = evalCommentService.addComment(tutorId, studentId, targetId, targetType, content);
        return ResponseEntity.ok(comment);
    }

    /**
     * 查询某学生的评语列表
     * GET /api/comment/list?studentId=xxx
     */
    @GetMapping("/list")
    public ResponseEntity<List<EvalComment>> list(@RequestParam("studentId") Long studentId) {
        return ResponseEntity.ok(evalCommentService.listByStudent(studentId));
    }

    /**
     * 查询当前导师对某学生的评语
     * GET /api/comment/my?studentId=xxx
     */
    @GetMapping("/my")
    public ResponseEntity<List<EvalComment>> myComments(@RequestParam("studentId") Long studentId,
                                                         HttpServletRequest request) {
        Long tutorId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(evalCommentService.listByTutorAndStudent(tutorId, studentId));
    }

    /**
     * 删除评语（仅评语作者可删）
     * DELETE /api/comment/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long tutorId = (Long) request.getAttribute("userId");
        if (evalCommentService.deleteComment(id, tutorId)) {
            return ResponseEntity.ok("删除成功");
        }
        return ResponseEntity.status(403).body("无权删除或评语不存在");
    }
}
