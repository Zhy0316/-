package com.example.archivemanagement.mapper;

import com.example.archivemanagement.entity.StudentTutorRelation;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StudentTutorRelationMapper {
    int insert(StudentTutorRelation relation);
    
    int update(StudentTutorRelation relation);
    
    int deleteById(Long id);
    
    StudentTutorRelation selectById(Long id);
    
    StudentTutorRelation selectByStudentAndTutor(Long studentId, Long tutorId);
    
    List<StudentTutorRelation> selectByStudentId(Long studentId);
    
    List<StudentTutorRelation> selectByTutorId(Long tutorId);
    
    List<StudentTutorRelation> selectByTutorIdAndStatus(Long tutorId, Integer status);
    
    List<StudentTutorRelation> selectByStudentIdAndStatus(Long studentId, Integer status);
}
