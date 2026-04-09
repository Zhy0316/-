package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.EvalGrowthScore;
import com.example.archivemanagement.service.EvalGrowthScoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/growth-score")
@RequiredArgsConstructor
public class EvalGrowthScoreController {

    private final EvalGrowthScoreService growthScoreService;

    @GetMapping("/{studentId}")
    public Result<List<EvalGrowthScore>> list(@PathVariable Long studentId) {
        return Result.ok(growthScoreService.listByStudent(studentId));
    }

    @GetMapping("/my")
    public Result<List<EvalGrowthScore>> myScores(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(growthScoreService.listByStudent(userId));
    }

    @PostMapping("/calculate/{studentId}")
    public Result<EvalGrowthScore> calculate(@PathVariable Long studentId) {
        return Result.ok(growthScoreService.calculate(studentId));
    }

    @PostMapping("/calculate/my")
    public Result<EvalGrowthScore> calculateMy(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(growthScoreService.calculate(userId));
    }

    @GetMapping("/radar/{studentId}")
    public Result<Map<String, Object>> radar(@PathVariable Long studentId) {
        return Result.ok(growthScoreService.getRadarData(studentId));
    }

    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> ranking() {
        return Result.ok(growthScoreService.getRanking());
    }
}
