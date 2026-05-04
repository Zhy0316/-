package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.archivemanagement.entity.StudentTutorRelation;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.mapper.StudentTutorRelationMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import com.example.archivemanagement.service.StudentTutorRelationService;
import com.example.archivemanagement.service.TodoMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentTutorRelationServiceImpl implements StudentTutorRelationService {

    @Autowired
    private StudentTutorRelationMapper mapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private TodoMessageService todoMessageService;

    @Override
    public boolean applyForTutor(Long studentId, Long tutorId) {
        return applyForTutorWithNote(studentId, tutorId, null, null);
    }

    @Override
    public boolean applyForTutorWithNote(Long studentId, Long tutorId, String applyNote, String attachFile) {
        StudentTutorRelation existing = mapper.selectByStudentAndTutor(studentId, tutorId);
        if (existing != null) return false;

        StudentTutorRelation relation = new StudentTutorRelation();
        relation.setStudentId(studentId);
        relation.setTutorId(tutorId);
        relation.setStatus(0);
        relation.setApplyTime(LocalDateTime.now());
        relation.setApplyNote(applyNote);
        relation.setAttachFile(attachFile);
        
        boolean success = mapper.insert(relation) > 0;
        if (success) {
            // 发送消息给导师
            SysUser student = userMapper.selectById(studentId);
            todoMessageService.sendRelationApplyNotice(tutorId, student.getRealName(), relation.getId());
        }
        return success;
    }

    @Override
    public boolean auditRelation(Long id, Integer status) {
        return auditRelation(id, status, null);
    }

    @Override
    public boolean auditRelation(Long id, Integer status, String rejectNote) {
        StudentTutorRelation relation = mapper.selectById(id);
        if (relation == null) {
            return false;
        }

        relation.setStatus(status);
        relation.setAuditTime(LocalDateTime.now());
        if (status == 2) {
            relation.setRejectNote(rejectNote);
        }

        boolean success = mapper.update(relation) > 0;
        if (success) {
            // 发送消息给学生
            SysUser tutor = userMapper.selectById(relation.getTutorId());
            todoMessageService.sendRelationResultNotice(relation.getStudentId(), tutor.getRealName(), status);
        }
        return success;
    }
    
    @Override
    public boolean resubmitRelation(Long id, String applyNote, String attachFile) {
        StudentTutorRelation relation = mapper.selectById(id);
        if (relation == null || relation.getStatus() != 2) {
            return false;
        }
        
        relation.setStatus(0);
        relation.setApplyTime(LocalDateTime.now());
        relation.setApplyNote(applyNote);
        if (attachFile != null) {
            relation.setAttachFile(attachFile);
        }
        relation.setRejectNote(null);
        
        boolean success = mapper.update(relation) > 0;
        if (success) {
            // 发送消息给导师
            SysUser student = userMapper.selectById(relation.getStudentId());
            todoMessageService.sendRelationApplyNotice(relation.getTutorId(), student.getRealName(), relation.getId());
        }
        return success;
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
        return mapper.selectByTutorIdAndStatus(tutorId, 0);
    }

    @Override
    public List<java.util.Map<String, Object>> getPendingWithStudentInfo(Long tutorId) {
        return mapper.selectPendingWithStudentInfo(tutorId);
    }

    @Override
    public List<StudentTutorRelation> getStudentPendingRelations(Long studentId) {
        return mapper.selectByStudentIdAndStatus(studentId, 0); // 0表示待审核
    }
}
