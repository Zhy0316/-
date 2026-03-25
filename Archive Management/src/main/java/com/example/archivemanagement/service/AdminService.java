package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.archivemanagement.entity.SysRole;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.entity.SysUserRole;
import com.example.archivemanagement.mapper.SysRoleMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import com.example.archivemanagement.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 管理员 Service
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 分页查询用户列表，支持按角色筛选
     * @param page     页码（从1开始）
     * @param size     每页条数
     * @param roleKey  角色标识（null=全部）
     * @param keyword  姓名/用户名关键字（null=不过滤）
     */
    public Map<String, Object> listUsers(int page, int size, String roleKey, String keyword) {
        // 如果按角色筛选，先查出该角色的 userId 集合
        Set<Long> roleUserIds = null;
        if (roleKey != null && !roleKey.isBlank()) {
            QueryWrapper<SysRole> rq = new QueryWrapper<>();
            rq.eq("role_key", roleKey);
            SysRole role = roleMapper.selectOne(rq);
            if (role != null) {
                QueryWrapper<SysUserRole> urq = new QueryWrapper<>();
                urq.eq("role_id", role.getRoleId());
                List<SysUserRole> urs = userRoleMapper.selectList(urq);
                roleUserIds = new HashSet<>();
                for (SysUserRole ur : urs) roleUserIds.add(ur.getUserId());
            } else {
                // 角色不存在，返回空
                return Map.of("total", 0, "list", List.of());
            }
        }

        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like("real_name", keyword).or().like("username", keyword);
        }
        if (roleUserIds != null) {
            if (roleUserIds.isEmpty()) return Map.of("total", 0, "list", List.of());
            qw.in("user_id", roleUserIds);
        }
        qw.orderByDesc("create_time");

        Page<SysUser> pageObj = new Page<>(page, size);
        Page<SysUser> result = userMapper.selectPage(pageObj, qw);

        // 为每个用户附加角色信息
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysUser u : result.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", u.getUserId());
            item.put("username", u.getUsername());
            item.put("realName", u.getRealName());
            item.put("phone", u.getPhone());
            item.put("email", u.getEmail());
            item.put("status", u.getStatus());
            item.put("createTime", u.getCreateTime());
            item.put("roleKey", getUserRoleKey(u.getUserId()));
            list.add(item);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("total", result.getTotal());
        resp.put("list", list);
        return resp;
    }

    /**
     * 启用/禁用用户
     * @param userId 目标用户ID
     * @param status 1=启用，0=禁用
     */
    public boolean updateUserStatus(Long userId, Integer status) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return false;
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    /**
     * 获取用户角色标识
     */
    private String getUserRoleKey(Long userId) {
        QueryWrapper<SysUserRole> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        List<SysUserRole> urs = userRoleMapper.selectList(qw);
        if (urs.isEmpty()) return null;
        SysRole role = roleMapper.selectById(urs.get(0).getRoleId());
        return role != null ? role.getRoleKey() : null;
    }
}
