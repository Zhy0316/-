package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.archivemanagement.entity.*;
import com.example.archivemanagement.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 数据统计 Service
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final InfoStudentMapper studentMapper;
    private final RecordAcademicMapper academicMapper;
    private final RecordAwardMapper awardMapper;
    private final RecordResearchMapper researchMapper;
    private final RecordPracticeMapper practiceMapper;
    private final EvalGrowthScoreMapper growthScoreMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    /**
     * 学院整体统计概览
     */
    public Map<String, Object> getCollegeOverview() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 学生总数
        long studentCount = studentMapper.selectCount(null);
        result.put("studentCount", studentCount);

        // 全院平均绩点
        List<Map<String, Object>> trendAll = academicMapper.selectGpaTrendByStudentId(null);
        // 用所有成绩计算总体加权平均绩点
        BigDecimal avgGpa = calcOverallAvgGpa();
        result.put("avgGpa", avgGpa);

        // 获奖总数（已审核通过）
        QueryWrapper<RecordAward> awardQw = new QueryWrapper<>();
        awardQw.eq("audit_status", 1);
        long awardCount = awardMapper.selectCount(awardQw);
        result.put("awardCount", awardCount);

        // 科研项目总数
        long researchCount = researchMapper.selectCount(null);
        result.put("researchCount", researchCount);

        // 实践记录总数
        long practiceCount = practiceMapper.selectCount(null);
        result.put("practiceCount", practiceCount);

        return result;
    }

    /**
     * 单个学生成长趋势数据（各学期综合分）
     */
    public List<Map<String, Object>> getStudentGrowthTrend(Long studentId) {
        List<EvalGrowthScore> scores = growthScoreMapper.selectByStudentId(studentId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (EvalGrowthScore s : scores) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("termYear", s.getTermYear());
            item.put("totalScore", s.getTotalScore());
            item.put("academicScore", s.getAcademicScore());
            item.put("awardScore", s.getAwardScore());
            item.put("researchScore", s.getResearchScore());
            item.put("practiceScore", s.getPracticeScore());
            result.add(item);
        }
        return result;
    }

    /**
     * 获奖级别分布（饼图数据）
     */
    public List<Map<String, Object>> getAwardDistribution() {
        List<RecordAward> awards = awardMapper.selectList(
                new QueryWrapper<RecordAward>().eq("audit_status", 1));
        Map<String, Integer> countMap = new LinkedHashMap<>();
        countMap.put("国家级", 0);
        countMap.put("省级", 0);
        countMap.put("校级", 0);
        countMap.put("其他", 0);
        for (RecordAward a : awards) {
            String level = a.getAwardLevel();
            if (level == null) { countMap.merge("其他", 1, Integer::sum); continue; }
            if (level.contains("国家")) countMap.merge("国家级", 1, Integer::sum);
            else if (level.contains("省")) countMap.merge("省级", 1, Integer::sum);
            else if (level.contains("校")) countMap.merge("校级", 1, Integer::sum);
            else countMap.merge("其他", 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        countMap.forEach((k, v) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", k);
            item.put("value", v);
            result.add(item);
        });
        return result;
    }

    /**
     * 绩点分布（柱状图数据）
     * 区间：[0,1) [1,2) [2,3) [3,3.5) [3.5,4.0]
     */
    public Map<String, Object> getGpaDistribution() {
        // 查询所有学生的加权平均绩点
        List<Map<String, Object>> studentGpas = academicMapper.selectAllStudentAvgGpa();
        int[] buckets = new int[5]; // 0-1, 1-2, 2-3, 3-3.5, 3.5-4
        String[] labels = {"0~1", "1~2", "2~3", "3~3.5", "3.5~4.0"};
        for (Map<String, Object> row : studentGpas) {
            Object gpaObj = row.get("avgGpa");
            if (gpaObj == null) continue;
            double gpa = Double.parseDouble(gpaObj.toString());
            if (gpa < 1) buckets[0]++;
            else if (gpa < 2) buckets[1]++;
            else if (gpa < 3) buckets[2]++;
            else if (gpa < 3.5) buckets[3]++;
            else buckets[4]++;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("data", buckets);
        return result;
    }

    /**
     * 管理员首页统计（含用户数、各角色数量）
     */
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(getCollegeOverview());

        // 各角色用户数
        List<SysRole> roles = roleMapper.selectList(null);
        Map<String, Long> roleCount = new LinkedHashMap<>();
        for (SysRole role : roles) {
            QueryWrapper<SysUserRole> qw = new QueryWrapper<>();
            qw.eq("role_id", role.getRoleId());
            roleCount.put(role.getRoleKey(), userRoleMapper.selectCount(qw));
        }
        result.put("roleCount", roleCount);
        return result;
    }

    // ==================== 私有方法 ====================

    private BigDecimal calcOverallAvgGpa() {
        try {
            List<Map<String, Object>> rows = academicMapper.selectAllStudentAvgGpa();
            if (rows == null || rows.isEmpty()) return BigDecimal.ZERO;
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            for (Map<String, Object> row : rows) {
                Object v = row.get("avgGpa");
                if (v != null) {
                    sum = sum.add(new BigDecimal(v.toString()));
                    count++;
                }
            }
            if (count == 0) return BigDecimal.ZERO;
            return sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
