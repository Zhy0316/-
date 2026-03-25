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
            createRole("student", "学生", "学生用户");
            createRole("tutor", "导师", "导师用户");
            createRole("hr", "企业HR", "企业人力资源用户");
            createRole("admin", "管理员", "系统管理员");
            System.out.println("角色数据初始化完成");
        }
    }

    private void createRole(String key, String name, String desc) {
        SysRole role = new SysRole();
        role.setRoleKey(key);
        role.setRoleName(name);
        role.setDescription(desc);
        roleMapper.insert(role);
    }
    
    private void initTestData() {
        // 初始化管理员账号
        initAdminUser();
        // 初始化测试导师数据
        initTutorData();
    }

    private void initAdminUser() {
        QueryWrapper<SysUser> adminQuery = new QueryWrapper<>();
        adminQuery.eq("username", "admin");
        if (userMapper.selectOne(adminQuery) != null) return;

        QueryWrapper<SysRole> roleQuery = new QueryWrapper<>();
        roleQuery.eq("role_key", "admin");
        SysRole adminRole = roleMapper.selectOne(roleQuery);
        if (adminRole == null) return;

        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(encryptPassword("admin123"));
        admin.setRealName("系统管理员");
        admin.setStatus(1);
        userMapper.insert(admin);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(admin.getUserId());
        ur.setRoleId(adminRole.getRoleId());
        userRoleMapper.insert(ur);
        System.out.println("管理员账号初始化完成，账号：admin / 密码：admin123");
    }

    private void initTutorData() {
        // 检查是否已有导师用户
        QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
        userQuery.eq("username", "tutor1");
        if (userMapper.selectOne(userQuery) != null) return;

        QueryWrapper<SysRole> roleQuery = new QueryWrapper<>();
        roleQuery.eq("role_key", "tutor");
        SysRole tutorRole = roleMapper.selectOne(roleQuery);
        if (tutorRole == null) return;

        createTutor("tutor1", "张教授", "13800138001", "tutor1@example.com",
                "T001", "教授", "计算机科学与技术", tutorRole.getRoleId());
        createTutor("tutor2", "李副教授", "13800138002", "tutor2@example.com",
                "T002", "副教授", "软件工程", tutorRole.getRoleId());
        createTutor("tutor3", "王讲师", "13800138003", "tutor3@example.com",
                "T003", "讲师", "数据科学与大数据技术", tutorRole.getRoleId());
        System.out.println("测试导师数据初始化完成");
    }

    private void createTutor(String username, String realName, String phone, String email,
                              String tutorNo, String title, String field, Long roleId) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(encryptPassword("123456"));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(1);
        userMapper.insert(user);

        InfoTutor info = new InfoTutor();
        info.setUserId(user.getUserId());
        info.setTutorNo(tutorNo);
        info.setTitle(title);
        info.setResearchField(field);
        tutorMapper.insert(info);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getUserId());
        ur.setRoleId(roleId);
        userRoleMapper.insert(ur);
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