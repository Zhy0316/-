package com.example.archivemanagement.controller;

import com.example.archivemanagement.entity.InfoTutor;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.mapper.InfoTutorMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class TutorController {

    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private InfoTutorMapper tutorMapper;

    @GetMapping("/tutors")
    public ResponseEntity<List<Map<String, Object>>> getTutors(
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String major) {
        try {
            // 查询所有教师用户（角色为tutor）
            List<SysUser> users = userMapper.selectTutors();
            
            List<Map<String, Object>> tutors = new ArrayList<>();
            
            for (SysUser user : users) {
                // 查询教师详细信息
                InfoTutor tutorInfo = tutorMapper.selectByUserId(user.getUserId());
                
                // 如果提供了学院和专业，按研究方向过滤
                if (college != null && major != null && tutorInfo != null) {
                    String researchField = tutorInfo.getResearchField();
                    // 简单的包含关系匹配，实际应用中可能需要更复杂的匹配逻辑
                    if (!researchField.contains(major)) {
                        continue;
                    }
                }
                
                Map<String, Object> tutor = Map.of(
                    "userId", user.getUserId(),
                    "realName", user.getRealName(),
                    "title", tutorInfo != null ? tutorInfo.getTitle() : "讲师",
                    "researchField", tutorInfo != null ? tutorInfo.getResearchField() : ""
                );
                
                tutors.add(tutor);
            }
            
            return ResponseEntity.ok(tutors);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
}
