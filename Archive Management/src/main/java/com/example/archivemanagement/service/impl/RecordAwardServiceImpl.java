package com.example.archivemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.archivemanagement.entity.DictAwardLevel;
import com.example.archivemanagement.entity.RecordAward;
import com.example.archivemanagement.mapper.RecordAwardMapper;
import com.example.archivemanagement.service.DictAwardLevelService;
import com.example.archivemanagement.service.RecordAwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordAwardServiceImpl extends ServiceImpl<RecordAwardMapper, RecordAward> implements RecordAwardService {

    private final DictAwardLevelService awardLevelService;

    @Override
    public List<RecordAward> getByStudentId(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }

    @Override
    public List<RecordAward> getPendingByTutorId(Long tutorId) {
        return baseMapper.selectPendingByTutorId(tutorId);
    }

    @Override
    public int countPendingByTutorId(Long tutorId) {
        return baseMapper.countPendingByTutorId(tutorId);
    }
    
    /**
     * 保存或更新获奖记录,自动计算分数
     */
    @Override
    public boolean saveOrUpdate(RecordAward award) {
        // 如果设置了等级ID,自动计算分数
        if (award.getAwardLevelId() != null) {
            DictAwardLevel level = awardLevelService.getById(award.getAwardLevelId());
            if (level != null) {
                award.setAwardScore(level.getScore());
                award.setAwardLevel(level.getLevelName());
            }
        }
        return super.saveOrUpdate(award);
    }
    
    /**
     * 计算学生获奖总分
     */
    public int calculateTotalScore(Long studentId) {
        List<RecordAward> awards = getByStudentId(studentId);
        return awards.stream()
                .filter(a -> a.getAuditStatus() == 1) // 只统计已审核通过的
                .mapToInt(a -> a.getAwardScore() != null ? a.getAwardScore() : 0)
                .sum();
    }
}
