package com.example.archivemanagement.service.impl;

import com.example.archivemanagement.entity.StudentTutorRelation;
import com.example.archivemanagement.mapper.StudentTutorRelationMapper;
import com.example.archivemanagement.service.StudentTutorRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentTutorRelationServiceImpl implements StudentTutorRelationService {

    @Autowired
    private StudentTutorRelationMapper mapper;

    @Override
    public boolean applyForTutor(Long studentId, Long tutorId) {
        // 检查是否已经存在绑定关系
        StudentTutorRelation existing = mapper.selectByStudentAndTutor(studentId, tutorId);
        if (existing != null) {
            return false; // 已经存在绑定关系
        }

        StudentTutorRelation relation = new StudentTutorRelation();
        relation.setStudentId(studentId);
        relation.setTutorId(tutorId);
        relation.setStatus(0); // 待审核
        relation.setApplyTime(LocalDateTime.now());

        return mapper.insert(relation) > 0;
    }

    @Override
    public boolean auditRelation(Long id, Integer status) {
        StudentTutorRelation relation = mapper.selectById(id);
        if (relation == null) {
            return false;
        }

        relation.setStatus(status);
        relation.setAuditTime(LocalDateTime.now());

        return mapper.update(relation) > 0;
    }

    @Override
    public StudentTutorRelation getRelation(Long studentId, Long tutorId) {
        return mapper.selectByStudentAndTutor(studentId, tutorId);
    }

    @Override
    public List<StudentTutorRelation> getStudentRelations(Long studentId) {
        return mapper.selectByStudentId(studentId);
    }

    @Override
    public List<StudentTutorRelation> getTutorRelations(Long tutorId) {
        return mapper.selectByTutorId(tutorId);
    }

    @Override
    public List<StudentTutorRelation> getTutorPendingRelations(Long tutorId) {
        return mapper.selectByTutorIdAndStatus(tutorId, 0); // 0表示待审核
    }

    @Override
    public List<StudentTutorRelation> getStudentPendingRelations(Long studentId) {
        return mapper.selectByStudentIdAndStatus(studentId, 0); // 0表示待审核
    }
}
