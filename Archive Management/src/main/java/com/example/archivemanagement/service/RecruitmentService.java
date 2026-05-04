package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.BusJobApplication;
import com.example.archivemanagement.entity.BusRecruitment;
import com.example.archivemanagement.mapper.BusJobApplicationMapper;
import com.example.archivemanagement.mapper.BusRecruitmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 招聘与求职 Service
 */
@Service
@RequiredArgsConstructor
public class RecruitmentService extends ServiceImpl<BusRecruitmentMapper, BusRecruitment> {

    private final BusJobApplicationMapper applicationMapper;

    // ==================== 招聘管理 ====================

    /** 企业发布招聘 */
    public BusRecruitment publish(Long enterpriseId, BusRecruitment recruitment) {
        recruitment.setEnterpriseId(enterpriseId);
        recruitment.setStatus(1);
        recruitment.setCreateTime(LocalDateTime.now());
        save(recruitment);
        return recruitment;
    }

    /** 获取招聘中的职位列表（学生浏览） */
    public List<BusRecruitment> listActive() {
        QueryWrapper<BusRecruitment> qw = new QueryWrapper<>();
        qw.eq("status", 1).orderByDesc("create_time");
        return list(qw);
    }

    /** 获取某企业发布的所有招聘 */
    public List<BusRecruitment> listByEnterprise(Long enterpriseId) {
        QueryWrapper<BusRecruitment> qw = new QueryWrapper<>();
        qw.eq("enterprise_id", enterpriseId).orderByDesc("create_time");
        return list(qw);
    }

    /** 关闭招聘 */
    public boolean close(Long id, Long enterpriseId) {
        BusRecruitment r = getById(id);
        if (r == null || !r.getEnterpriseId().equals(enterpriseId)) return false;
        r.setStatus(0);
        return updateById(r);
    }

    // ==================== 求职申请 ====================

    /** 学生投递简历 */
    public BusJobApplication apply(Long studentId, Long recruitmentId, String message) {
        // 防止重复投递
        QueryWrapper<BusJobApplication> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId).eq("recruitment_id", recruitmentId);
        if (applicationMapper.selectOne(qw) != null) return null;

        BusJobApplication app = new BusJobApplication();
        app.setStudentId(studentId);
        app.setRecruitmentId(recruitmentId);
        app.setMessage(message);
        app.setStatus(0);
        app.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(app);
        return app;
    }

    /** 学生查看自己的投递记录 */
    public List<Map<String, Object>> myApplications(Long studentId) {
        return applicationMapper.selectByStudentWithDetail(studentId);
    }

    /** 企业查看某职位的投递列表 */
    public List<Map<String, Object>> applicationsByRecruitment(Long recruitmentId) {
        return applicationMapper.selectByRecruitmentWithDetail(recruitmentId);
    }

    /** 企业更新投递状态（1=有意向，2=不匹配） */
    public boolean updateApplicationStatus(Long applicationId, Integer status) {
        BusJobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) return false;
        app.setStatus(status);
        return applicationMapper.updateById(app) > 0;
    }

    /** 企业更新投递流程状态（细化版） */
    public boolean updateFinalStatus(Long applicationId, Integer finalStatus,
                                     java.time.LocalDateTime interviewTime, String interviewNote) {
        BusJobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) return false;
        app.setFinalStatus(finalStatus);
        if (interviewTime != null) app.setInterviewTime(interviewTime);
        if (interviewNote != null) app.setInterviewNote(interviewNote);
        // 同步旧 status 字段（兼容）
        if (finalStatus == 3) app.setStatus(1);      // 录用 → 有意向
        else if (finalStatus == 4) app.setStatus(2); // 淘汰 → 不匹配
        return applicationMapper.updateById(app) > 0;
    }
}
