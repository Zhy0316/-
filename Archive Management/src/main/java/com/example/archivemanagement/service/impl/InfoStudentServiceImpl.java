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
            sysUser.setRealName(dto.getRealName());
            sysUser.setPhone(dto.getPhone());
            sysUser.setEmail(dto.getEmail());
            // username and avatar handled elsewhere or not editable here
            sysUserMapper.updateById(sysUser);
        }

        // Update student info
        InfoStudent infoStudent = infoStudentMapper.selectById(userId);
        if (infoStudent == null) {
            infoStudent = new InfoStudent();
            infoStudent.setUserId(userId);
        }
        
        // Use manual setting or BeanUtils with exclusions, here simple copy works if DTO aligns
        infoStudent.setStudentNo(dto.getStudentNo());
        infoStudent.setGender(dto.getGender());
        infoStudent.setCollege(dto.getCollege());
        infoStudent.setMajor(dto.getMajor());
        infoStudent.setClassName(dto.getClassName());
        infoStudent.setEnrollmentYear(dto.getEnrollmentYear());
        infoStudent.setPoliticalStatus(dto.getPoliticalStatus());
        infoStudent.setNation(dto.getNation());
        infoStudent.setNativePlace(dto.getNativePlace());
        infoStudent.setBirthDate(dto.getBirthDate());
        
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
