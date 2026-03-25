package com.example.archivemanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.EvalGrowthScore;
import com.example.archivemanagement.entity.RecordAcademic;
import com.example.archivemanagement.entity.RecordAward;
import com.example.archivemanagement.entity.RecordPractice;
import com.example.archivemanagement.entity.RecordResearch;
import com.example.archivemanagement.mapper.EvalGrowthScoreMapper;
import com.example.archivemanagement.mapper.RecordAcademicMapper;
import com.example.archivemanagement.mapper.RecordAwardMapper;
import com.example.archivemanagement.mapper.RecordPracticeMapper;
import com.example.archivemanagement.mapper.RecordResearchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成长评分 Service
 * 加权规则：学业40% + 获奖20% + 科研20% + 实践20%
 */
@Service
@RequiredArgsConstructor
public class EvalGrowthScoreService extends ServiceImpl<EvalGrowthScoreMapper, EvalGrowthScore> {

    private final RecordAcademicMapper academicMapper;
    private final RecordAwardMapper awardMapper;
    private final RecordPracticeMapper practiceMapper;
    private final RecordResearchMapper researchMapper;

    /**
     * 查询某学生所有学期成长分
     */
    public List<EvalGrowthScore> listByStudent(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }

    /**
     * 触发重新计算某学生当前学期的综合成长分
     */
    public EvalGrowthScore calculate(Long studentId) {
        String termYear = getCurrentTerm();

        // 1. 学业成绩分：用加权平均绩点换算（满分4.0 → 100分）
        List<RecordAcademic> academics = academicMapper.selectByStudentId(studentId);
        BigDecimal academicScore = calcAcademicScore(academics);

        // 2. 获奖加分：国家级20分/项，省级10分/项，校级5分/项，上限100
        List<RecordAward> awards = awardMapper.selectByStudentId(studentId);
        BigDecimal awardScore = calcAwardScore(awards);

        // 3. 科研能力分：每个项目20分，上限100
        List<RecordResearch> researches = researchMapper.selectByStudentId(studentId);
        BigDecimal researchScore = calcResearchScore(researches);

        // 4. 实践表现分：每段实践15分，上限100
        List<RecordPractice> practices = practiceMapper.selectByStudentId(studentId);
        BigDecimal practiceScore = calcPracticeScore(practices);

        // 5. 综合成长分（加权）
        BigDecimal total = academicScore.multiply(new BigDecimal("0.4"))
                .add(awardScore.multiply(new BigDecimal("0.2")))
                .add(researchScore.multiply(new BigDecimal("0.2")))
                .add(practiceScore.multiply(new BigDecimal("0.2")))
                .setScale(2, RoundingMode.HALF_UP);

        // 查找是否已有当期记录
        QueryWrapper<EvalGrowthScore> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId).eq("term_year", termYear);
        EvalGrowthScore score = getOne(qw);
        if (score == null) {
            score = new EvalGrowthScore();
            score.setStudentId(studentId);
            score.setTermYear(termYear);
        }
        score.setAcademicScore(academicScore);
        score.setAwardScore(awardScore);
        score.setResearchScore(researchScore);
        score.setPracticeScore(practiceScore);
        score.setTotalScore(total);
        score.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(score);
        return score;
    }

    /**
     * 返回雷达图五维数据
     */
    public Map<String, Object> getRadarData(Long studentId) {
        EvalGrowthScore score = calculate(studentId);
        Map<String, Object> radar = new HashMap<>();
        radar.put("academic", score.getAcademicScore());
        radar.put("award", score.getAwardScore());
        radar.put("research", score.getResearchScore());
        radar.put("practice", score.getPracticeScore());
        radar.put("total", score.getTotalScore());
        radar.put("termYear", score.getTermYear());
        return radar;
    }

    /**
     * 全院成长分排行
     */
    public List<Map<String, Object>> getRanking() {
        return baseMapper.selectRanking();
    }

    // ==================== 私有计算方法 ====================

    private BigDecimal calcAcademicScore(List<RecordAcademic> list) {
        if (list == null || list.isEmpty()) return BigDecimal.ZERO;
        BigDecimal totalCreditGpa = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (RecordAcademic r : list) {
            if ("否".equals(r.getIsInvalidated()) && r.getCredit() != null && r.getGpa() != null) {
                totalCreditGpa = totalCreditGpa.add(r.getCredit().multiply(r.getGpa()));
                totalCredit = totalCredit.add(r.getCredit());
            }
        }
        if (totalCredit.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        // 加权平均绩点 / 4.0 * 100
        BigDecimal avgGpa = totalCreditGpa.divide(totalCredit, 4, RoundingMode.HALF_UP);
        return avgGpa.divide(new BigDecimal("4.0"), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .min(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcAwardScore(List<RecordAward> list) {
        if (list == null || list.isEmpty()) return BigDecimal.ZERO;
        int total = 0;
        for (RecordAward a : list) {
            if (a.getAuditStatus() != null && a.getAuditStatus() == 1) {
                String level = a.getAwardLevel();
                if (level != null) {
                    if (level.contains("国家")) total += 20;
                    else if (level.contains("省")) total += 10;
                    else total += 5;
                }
            }
        }
        return new BigDecimal(Math.min(total, 100));
    }

    private BigDecimal calcResearchScore(List<RecordResearch> list) {
        if (list == null || list.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(Math.min(list.size() * 20, 100));
    }

    private BigDecimal calcPracticeScore(List<RecordPractice> list) {
        if (list == null || list.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(Math.min(list.size() * 15, 100));
    }

    private String getCurrentTerm() {
        int year = Year.now().getValue();
        int month = java.time.LocalDate.now().getMonthValue();
        // 9月~次年1月为第一学期，2月~7月为第二学期
        int term = (month >= 9 || month <= 1) ? 1 : 2;
        int startYear = (month >= 9) ? year : year - 1;
        return startYear + "-" + (startYear + 1) + "-" + term;
    }
}
