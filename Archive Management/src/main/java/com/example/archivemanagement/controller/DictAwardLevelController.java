package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.DictAwardLevel;
import com.example.archivemanagement.service.DictAwardLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 获奖等级字典 Controller
 */
@RestController
@RequestMapping("/api/dict/award-level")
@RequiredArgsConstructor
public class DictAwardLevelController {
    
    private final DictAwardLevelService awardLevelService;
    
    /**
     * 获取所有获奖等级
     */
    @GetMapping("/list")
    public Result<List<DictAwardLevel>> list() {
        return Result.ok(awardLevelService.listAll());
    }
}
