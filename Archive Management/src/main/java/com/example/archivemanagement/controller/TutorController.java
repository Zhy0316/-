package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.InfoTutor;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.mapper.InfoTutorMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class TutorController {

    private final SysUserMapper userMapper;
    private final InfoTutorMapper tutorMapper;

    @GetMapping("/tutors")
    public Result<List<Map<String, Object>>> getTutors(
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String major) {
        List<SysUser> users = userMapper.selectTutors();
        List<Map<String, Object>> tutors = new ArrayList<>();
        for (SysUser user : users) {
            InfoTutor tutorInfo = tutorMapper.selectByUserId(user.getUserId());
            if (college != null && major != null && tutorInfo != null) {
                String field = tutorInfo.getResearchField();
                if (field == null || !field.contains(major)) continue;
            }
            tutors.add(Map.of(
                "userId", user.getUserId(),
                "realName", user.getRealName() != null ? user.getRealName() : "",
                "title", tutorInfo != null && tutorInfo.getTitle() != null ? tutorInfo.getTitle() : "讲师",
                "researchField", tutorInfo != null && tutorInfo.getResearchField() != null ? tutorInfo.getResearchField() : ""
            ));
        }
        return Result.ok(tutors);
    }
}
