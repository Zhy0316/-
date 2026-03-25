package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.archivemanagement.dto.RegisterDTO;
import com.example.archivemanagement.entity.InfoEnterprise;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.mapper.InfoEnterpriseMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 企业信息 Service
 */
@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private final InfoEnterpriseMapper enterpriseMapper;
    private final SysUserMapper userMapper;
    private final UserService userService;

    /**
     * 企业注册：创建系统用户（hr角色）+ 企业信息记录
     */
    public boolean registerEnterprise(RegisterDTO dto) {
        dto.setRoleKey("hr");
        boolean userCreated = userService.register(dto);
        if (!userCreated) return false;

        // 查询刚创建的用户ID
        QueryWrapper<SysUser> q = new QueryWrapper<>();
        q.eq("username", dto.getUsername());
        SysUser user = userMapper.selectOne(q);
        if (user == null) return false;

        // 创建企业信息记录（初始待审核）
        InfoEnterprise enterprise = new InfoEnterprise();
        enterprise.setUserId(user.getUserId());
        enterprise.setCompanyName(dto.getCompanyName());
        enterprise.setCreditCode(dto.getCreditCode());
        enterprise.setIndustry(dto.getIndustry());
        enterprise.setAuditStatus(0);
        enterpriseMapper.insert(enterprise);
        return true;
    }

    /**
     * 根据用户ID获取企业信息
     */
    public InfoEnterprise getByUserId(Long userId) {
        return enterpriseMapper.selectById(userId);
    }

    /**
     * 更新企业信息（简介、行业等）
     */
    public boolean updateEnterprise(Long userId, InfoEnterprise enterprise) {
        enterprise.setUserId(userId);
        return enterpriseMapper.updateById(enterprise) > 0;
    }

    /**
     * 上传营业执照文件路径
     */
    public boolean updateLicenseFile(Long userId, String filePath) {
        InfoEnterprise enterprise = enterpriseMapper.selectById(userId);
        if (enterprise == null) return false;
        enterprise.setLicenseFile(filePath);
        return enterpriseMapper.updateById(enterprise) > 0;
    }

    /**
     * 管理员：获取所有企业列表
     */
    public List<InfoEnterprise> listAll() {
        return enterpriseMapper.selectList(null);
    }

    /**
     * 管理员：审核企业认证
     * @param userId 企业用户ID
     * @param auditStatus 1=通过，2=驳回
     */
    public boolean audit(Long userId, Integer auditStatus) {
        InfoEnterprise enterprise = enterpriseMapper.selectById(userId);
        if (enterprise == null) return false;
        enterprise.setAuditStatus(auditStatus);
        return enterpriseMapper.updateById(enterprise) > 0;
    }
}
