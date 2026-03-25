package com.example.archivemanagement.service;

import com.example.archivemanagement.entity.StudentTutorRelation;
import java.util.List;

public interface StudentTutorRelationService {
    boolean applyForTutor(Long studentId, Long tutorId);
    
    boolean auditRelation(Long id, Integer status);
    
    StudentTutorRelation getRelation(Long studentId, Long tutorId);
    
    List<StudentTutorRelation> getStudentRelations(Long studentId);
    
    List<StudentTutorRelation> getTutorRelations(Long tutorId);
    
    List<StudentTutorRelation> getTutorPendingRelations(Long tutorId);
    
    List<StudentTutorRelation> getStudentPendingRelations(Long studentId);
}
