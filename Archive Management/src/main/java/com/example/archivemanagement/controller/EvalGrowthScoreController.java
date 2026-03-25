package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.EvalGrowthScore;
import com.example.archivemanagement.service.EvalGrowthScoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 成长评分接口
 */
@RestController
@RequestMapping("/api/growth-score")
@RequiredArgsConstructor
public class EvalGrowthScoreController {

    private final EvalGrowthScoreService growthScoreService;

    /**
     * 获取某学生各学期成长分列表
     * GET /api/growth-score/{studentId}
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<List<EvalGrowthScore>> list(@PathVariable Long studentId) {
        return ResponseEntity.ok(growthScoreService.listByStudent(studentId));
    }

    /**
     * 获取当前登录学生自己的成长分
     * GET /api/growth-score/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<EvalGrowthScore>> myScores(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(growthScoreService.listByStudent(userId));
    }

    /**
     * 触发重新计算综合成长分
     * POST /api/growth-score/calculate/{studentId}
     */
    @PostMapping("/calculate/{studentId}")
    public ResponseEntity<EvalGrowthScore> calculate(@PathVariable Long studentId) {
        return ResponseEntity.ok(growthScoreService.calculate(studentId));
    }

    /**
     * 触发计算当前登录学生的成长分
     * POST /api/growth-score/calculate/my
     */
    @PostMapping("/calculate/my")
    public ResponseEntity<EvalGrowthScore> calculateMy(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(growthScoreService.calculate(userId));
    }

    /**
     * 获取雷达图五维数据
     * GET /api/growth-score/radar/{studentId}
     */
    @GetMapping("/radar/{studentId}")
    public ResponseEntity<Map<String, Object>> radar(@PathVariable Long studentId) {
        return ResponseEntity.ok(growthScoreService.getRadarData(studentId));
    }

    /**
     * 全院成长分排行榜（管理员/导师使用）
     * GET /api/growth-score/ranking
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<Map<String, Object>>> ranking() {
        return ResponseEntity.ok(growthScoreService.getRanking());
    }
}
