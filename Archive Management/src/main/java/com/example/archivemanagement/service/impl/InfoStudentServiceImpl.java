package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.dto.StudentProfileDTO;
import com.example.archivemanagement.entity.InfoStudent;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.mapper.InfoStudentMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import com.example.archivemanagement.service.InfoStudentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InfoStudentServiceImpl extends ServiceImpl<InfoStudentMapper, InfoStudent> implements InfoStudentService {

    @Autowired
    private InfoStudentMapper infoStudentMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public StudentProfileDTO getStudentProfile(Long userId) {
        // Get user info
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            return null;
        }

        // Get student info
        InfoStudent infoStudent = infoStudentMapper.selectById(userId);
        
        StudentProfileDTO dto = new StudentProfileDTO();
        
        // Copy SysUser properties
        dto.setUserId(sysUser.getUserId());
        dto.setUsername(sysUser.getUsername());
        dto.setRealName(sysUser.getRealName());
        dto.setPhone(sysUser.getPhone());
        dto.setEmail(sysUser.getEmail());
        dto.setAvatar(sysUser.getAvatar());
        
        // Copy InfoStudent properties if exists
        if (infoStudent != null) {
            BeanUtils.copyProperties(infoStudent, dto);
        }
        
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudentProfile(Long userId, StudentProfileDTO dto) {
        // Update user info
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser != null) {
            // Real name is not editable - do NOT update realName
            if (dto.getPhone() != null) sysUser.setPhone(dto.getPhone());
            if (dto.getEmail() != null) sysUser.setEmail(dto.getEmail());
            if (dto.getAvatar() != null) sysUser.setAvatar(dto.getAvatar());
            sysUserMapper.updateById(sysUser);
        }

        // Update student info
        InfoStudent infoStudent = infoStudentMapper.selectById(userId);
        if (infoStudent == null) {
            infoStudent = new InfoStudent();
            infoStudent.setUserId(userId);
            // If we are creating a new record, copy real name from sys_user
            infoStudent.setRealName(sysUser != null ? sysUser.getRealName() : null);
        }

        // Use manual setting or BeanUtils with exclusions, here simple copy works if DTO aligns
        if (dto.getStudentNo() != null) infoStudent.setStudentNo(dto.getStudentNo());
        if (dto.getGender() != null) infoStudent.setGender(dto.getGender());
        if (dto.getCollege() != null) infoStudent.setCollege(dto.getCollege());
        if (dto.getMajor() != null) infoStudent.setMajor(dto.getMajor());
        if (dto.getClassName() != null) infoStudent.setClassName(dto.getClassName());
        if (dto.getEnrollmentYear() != null) infoStudent.setEnrollmentYear(dto.getEnrollmentYear());
        if (dto.getPoliticalStatus() != null) infoStudent.setPoliticalStatus(dto.getPoliticalStatus());
        if (dto.getNation() != null) infoStudent.setNation(dto.getNation());
        if (dto.getNativePlace() != null) infoStudent.setNativePlace(dto.getNativePlace());
        if (dto.getBirthDate() != null) infoStudent.setBirthDate(dto.getBirthDate());
        if (dto.getAvatar() != null) infoStudent.setAvatar(dto.getAvatar());

        // If resume file is uploaded, it's typically handled separately, but can be updated here
        if (dto.getResumeFile() != null) {
             infoStudent.setResumeFile(dto.getResumeFile());
        }

        return saveOrUpdate(infoStudent);
    }

    @Override
    public void createDefaultStudentInfo(Long userId, String studentNo) {
        InfoStudent infoStudent = new InfoStudent();
        infoStudent.setUserId(userId);
        infoStudent.setStudentNo(studentNo);
        // Default other fields can be null
        save(infoStudent);
    }
}
