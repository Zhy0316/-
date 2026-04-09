package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.EvalComment;
import com.example.archivemanagement.service.EvalCommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class EvalCommentController {

    private final EvalCommentService evalCommentService;

    @PostMapping("/save")
    public Result<EvalComment> save(@RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        Long tutorId = (Long) request.getAttribute("userId");
        Long studentId = Long.valueOf(body.get("studentId").toString());
        Long targetId = body.containsKey("targetId") && body.get("targetId") != null
                ? Long.valueOf(body.get("targetId").toString()) : null;
        String targetType = body.containsKey("targetType") ? body.get("targetType").toString() : "GENERAL";
        String content = body.get("content").toString();
        if (content == null || content.isBlank()) throw new BusinessException("评语内容不能为空");
        return Result.ok(evalCommentService.addComment(tutorId, studentId, targetId, targetType, content));
    }

    @GetMapping("/list")
    public Result<List<EvalComment>> list(@RequestParam("studentId") Long studentId) {
        return Result.ok(evalCommentService.listByStudent(studentId));
    }

    @GetMapping("/my")
    public Result<List<EvalComment>> myComments(@RequestParam("studentId") Long studentId,
                                                HttpServletRequest request) {
        Long tutorId = (Long) request.getAttribute("userId");
        return Result.ok(evalCommentService.listByTutorAndStudent(tutorId, studentId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long tutorId = (Long) request.getAttribute("userId");
        if (!evalCommentService.deleteComment(id, tutorId)) {
            throw BusinessException.forbidden("无权删除或评语不存在");
        }
        return Result.ok("删除成功");
    }

    /** 编辑评语内容（仅作者可改） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody Map<String, Object> body,
                               HttpServletRequest request) {
        Long tutorId = (Long) request.getAttribute("userId");
        String content = body.get("content").toString();
        if (content == null || content.isBlank()) throw new BusinessException("评语内容不能为空");
        if (!evalCommentService.updateComment(id, tutorId, content)) {
            throw BusinessException.forbidden("无权修改或评语不存在");
        }
        return Result.ok("修改成功");
    }
}
