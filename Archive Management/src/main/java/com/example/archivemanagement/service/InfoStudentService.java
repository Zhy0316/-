package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.archivemanagement.dto.StudentProfileDTO;
import com.example.archivemanagement.entity.InfoStudent;

public interface InfoStudentService extends IService<InfoStudent> {
    
    /**
     * Get student profile by user ID
     */
    StudentProfileDTO getStudentProfile(Long userId);
    
    /**
     * Update student profile
     */
    boolean updateStudentProfile(Long userId, StudentProfileDTO profileDTO);
    
    /**
     * Create default student info when a student registers
     */
    void createDefaultStudentInfo(Long userId, String studentNo);
}
