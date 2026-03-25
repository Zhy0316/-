package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.archivemanagement.dto.LoginDTO;
import com.example.archivemanagement.dto.RegisterDTO;
import com.example.archivemanagement.dto.UserInfoDTO;
import com.example.archivemanagement.entity.SysRole;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.entity.SysUserRole;
import com.example.archivemanagement.mapper.SysRoleMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import com.example.archivemanagement.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    
    public UserInfoDTO login(LoginDTO loginDTO) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDTO.getUsername())
                   .eq("password", DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes()));
        
        SysUser user = userMapper.selectOne(queryWrapper);
        if (user == null || user.getStatus() == 0) {
            return null;
        }
        
        return getUserInfo(user);
    }
    
    public boolean register(RegisterDTO registerDTO) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerDTO.getUsername());
        if (userMapper.selectOne(queryWrapper) != null) {
            return false;
        }
        
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        
        int result = userMapper.insert(user);
        if (result > 0) {
            QueryWrapper<SysRole> roleQuery = new QueryWrapper<>();
            roleQuery.eq("role_key", registerDTO.getRoleKey());
            SysRole role = roleMapper.selectOne(roleQuery);
            
            if (role != null) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(role.getRoleId());
                userRoleMapper.insert(userRole);
            }
            return true;
        }
        return false;
    }
    
    private UserInfoDTO getUserInfo(SysUser user) {
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setCreateTime(user.getCreateTime());
        
        QueryWrapper<SysUserRole> userRoleQuery = new QueryWrapper<>();
        userRoleQuery.eq("user_id", user.getUserId());
        List<SysUserRole> userRoles = userRoleMapper.selectList(userRoleQuery);
        
        if (!userRoles.isEmpty()) {
            SysRole role = roleMapper.selectById(userRoles.get(0).getRoleId());
            if (role != null) {
                userInfo.setRoleKey(role.getRoleKey());
                userInfo.setRoleName(role.getRoleName());
            }
        }
        
        return userInfo;
    }
    
    public List<SysRole> getAllRoles() {
        return roleMapper.selectList(null);
    }
}