package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.EvalComment;
import com.example.archivemanagement.mapper.EvalCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导师评语 Service
 */
@Service
@RequiredArgsConstructor
public class EvalCommentService extends ServiceImpl<EvalCommentMapper, EvalComment> {

    /**
     * 导师添加评语
     */
    public EvalComment addComment(Long tutorId, Long studentId, Long targetId,
                                   String targetType, String content) {
        EvalComment comment = new EvalComment();
        comment.setTutorId(tutorId);
        comment.setStudentId(studentId);
        comment.setTargetId(targetId);
        comment.setTargetType(targetType != null ? targetType : "GENERAL");
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        save(comment);
        return comment;
    }

    /**
     * 查询某学生的所有评语
     */
    public List<EvalComment> listByStudent(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }

    /**
     * 查询某导师对某学生的评语
     */
    public List<EvalComment> listByTutorAndStudent(Long tutorId, Long studentId) {
        return baseMapper.selectByTutorAndStudent(tutorId, studentId);
    }

    /**
     * 删除评语（仅评语作者可删）
     */
    public boolean deleteComment(Long id, Long tutorId) {
        EvalComment comment = getById(id);
        if (comment == null || !comment.getTutorId().equals(tutorId)) {
            return false;
        }
        return removeById(id);
    }

    /**
     * 编辑评语内容（仅评语作者可改）
     */
    public boolean updateComment(Long id, Long tutorId, String content) {
        EvalComment comment = getById(id);
        if (comment == null || !comment.getTutorId().equals(tutorId)) {
            return false;
        }
        comment.setContent(content);
        return updateById(comment);
    }
}
