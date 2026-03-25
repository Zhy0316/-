package com.example.archivemanagement.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.archivemanagement.entity.InfoTutor;
import com.example.archivemanagement.entity.SysRole;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.entity.SysUserRole;
import com.example.archivemanagement.mapper.InfoTutorMapper;
import com.example.archivemanagement.mapper.SysRoleMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import com.example.archivemanagement.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final InfoTutorMapper tutorMapper;
    
    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initTestData();
    }
    
    private void initRoles() {
        // 检查角色是否已存在
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        long count = roleMapper.selectCount(queryWrapper);
        
        if (count == 0) {
            // 初始化角色数据
            SysRole studentRole = new SysRole();
            studentRole.setRoleKey("student");
            studentRole.setRoleName("学生");
            studentRole.setDescription("学生用户");
            roleMapper.insert(studentRole);
            
            SysRole tutorRole = new SysRole();
            tutorRole.setRoleKey("tutor");
            tutorRole.setRoleName("导师");
            tutorRole.setDescription("导师用户");
            roleMapper.insert(tutorRole);
            
            SysRole hrRole = new SysRole();
            hrRole.setRoleKey("hr");
            hrRole.setRoleName("企业HR");
            hrRole.setDescription("企业人力资源用户");
            roleMapper.insert(hrRole);
            
            System.out.println("角色数据初始化完成");
        }
    }
    
    private void initTestData() {
        // 检查是否已有教师用户
        QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
        userQuery.eq("username", "tutor1");
        SysUser existingUser = userMapper.selectOne(userQuery);
        
        if (existingUser == null) {
            // 获取导师角色ID
            QueryWrapper<SysRole> roleQuery = new QueryWrapper<>();
            roleQuery.eq("role_key", "tutor");
            SysRole tutorRole = roleMapper.selectOne(roleQuery);
            
            if (tutorRole != null) {
                // 创建教师用户1
                SysUser tutor1 = new SysUser();
                tutor1.setUsername("tutor1");
                tutor1.setPassword(encryptPassword("123456"));
                tutor1.setRealName("张教授");
                tutor1.setPhone("13800138001");
                tutor1.setEmail("tutor1@example.com");
                tutor1.setStatus(1);
                userMapper.insert(tutor1);
                
                // 创建教师信息1
                InfoTutor tutorInfo1 = new InfoTutor();
                tutorInfo1.setUserId(tutor1.getUserId());
                tutorInfo1.setTutorNo("T001");
                tutorInfo1.setTitle("教授");
                tutorInfo1.setResearchField("计算机科学与技术");
                tutorMapper.insert(tutorInfo1);
                
                // 创建用户角色关联1
                SysUserRole userRole1 = new SysUserRole();
                userRole1.setUserId(tutor1.getUserId());
                userRole1.setRoleId(tutorRole.getRoleId());
                userRoleMapper.insert(userRole1);
                
                // 创建教师用户2
                SysUser tutor2 = new SysUser();
                tutor2.setUsername("tutor2");
                tutor2.setPassword(encryptPassword("123456"));
                tutor2.setRealName("李副教授");
                tutor2.setPhone("13800138002");
                tutor2.setEmail("tutor2@example.com");
                tutor2.setStatus(1);
                userMapper.insert(tutor2);
                
                // 创建教师信息2
                InfoTutor tutorInfo2 = new InfoTutor();
                tutorInfo2.setUserId(tutor2.getUserId());
                tutorInfo2.setTutorNo("T002");
                tutorInfo2.setTitle("副教授");
                tutorInfo2.setResearchField("软件工程");
                tutorMapper.insert(tutorInfo2);
                
                // 创建用户角色关联2
                SysUserRole userRole2 = new SysUserRole();
                userRole2.setUserId(tutor2.getUserId());
                userRole2.setRoleId(tutorRole.getRoleId());
                userRoleMapper.insert(userRole2);
                
                // 创建教师用户3
                SysUser tutor3 = new SysUser();
                tutor3.setUsername("tutor3");
                tutor3.setPassword(encryptPassword("123456"));
                tutor3.setRealName("王讲师");
                tutor3.setPhone("13800138003");
                tutor3.setEmail("tutor3@example.com");
                tutor3.setStatus(1);
                userMapper.insert(tutor3);
                
                // 创建教师信息3
                InfoTutor tutorInfo3 = new InfoTutor();
                tutorInfo3.setUserId(tutor3.getUserId());
                tutorInfo3.setTutorNo("T003");
                tutorInfo3.setTitle("讲师");
                tutorInfo3.setResearchField("数据科学与大数据技术");
                tutorMapper.insert(tutorInfo3);
                
                // 创建用户角色关联3
                SysUserRole userRole3 = new SysUserRole();
                userRole3.setUserId(tutor3.getUserId());
                userRole3.setRoleId(tutorRole.getRoleId());
                userRoleMapper.insert(userRole3);
                
                System.out.println("测试教师数据初始化完成");
            }
        }
    }
    
    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }
}