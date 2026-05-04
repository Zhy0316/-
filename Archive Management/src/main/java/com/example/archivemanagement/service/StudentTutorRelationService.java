package com.example.archivemanagement.service;

import com.example.archivemanagement.entity.StudentTutorRelation;
import java.util.List;

public interface StudentTutorRelationService {
    boolean applyForTutor(Long studentId, Long tutorId);

    /** 申请绑定（含自荐信和附件） */
    boolean applyForTutorWithNote(Long studentId, Long tutorId, String applyNote, String attachFile);
    
    boolean auditRelation(Long id, Integer status);
    
    boolean auditRelation(Long id, Integer status, String rejectNote);
    
    boolean resubmitRelation(Long id, String applyNote, String attachFile);
    
    StudentTutorRelation getRelation(Long studentId, Long tutorId);
    
    List<StudentTutorRelation> getStudentRelations(Long studentId);
    
    List<StudentTutorRelation> getTutorRelations(Long tutorId);
    
    List<StudentTutorRelation> getTutorPendingRelations(Long tutorId);

    /** 查询待审核申请，含学生基本信息 */
    List<java.util.Map<String, Object>> getPendingWithStudentInfo(Long tutorId);
    
    List<StudentTutorRelation> getStudentPendingRelations(Long studentId);
}
