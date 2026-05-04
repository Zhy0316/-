package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.entity.InfoStudent;
import com.example.archivemanagement.entity.InfoTutor;
import com.example.archivemanagement.entity.SysUser;
import com.example.archivemanagement.mapper.InfoStudentMapper;
import com.example.archivemanagement.mapper.InfoTutorMapper;
import com.example.archivemanagement.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
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
    private final InfoStudentMapper studentMapper;

    /**
     * 学生查看导师列表（仅显示同学院导师）
     */
    @GetMapping("/tutors")
    public Result<List<Map<String, Object>>> getTutors(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String roleKey = (String) request.getAttribute("roleKey");
        
        // 获取当前学生的学院信息
        String studentCollege = null;
        if ("student".equals(roleKey)) {
            InfoStudent studentInfo = studentMapper.selectById(userId);
            if (studentInfo != null) {
                studentCollege = studentInfo.getCollege();
            }
        }
        
        List<SysUser> users = userMapper.selectTutors();
        List<Map<String, Object>> tutors = new ArrayList<>();
        
        for (SysUser user : users) {
            InfoTutor tutorInfo = tutorMapper.selectByUserId(user.getUserId());
            
            // 学生只能看到同学院的导师
            if ("student".equals(roleKey) && studentCollege != null && tutorInfo != null) {
                String tutorCollege = tutorInfo.getCollege();
                if (tutorCollege == null || !tutorCollege.equals(studentCollege)) {
                    continue;
                }
            }
            
            tutors.add(Map.of(
                "userId", user.getUserId(),
                "realName", user.getRealName() != null ? user.getRealName() : "",
                "title", tutorInfo != null && tutorInfo.getTitle() != null ? tutorInfo.getTitle() : "讲师",
                "researchField", tutorInfo != null && tutorInfo.getResearchField() != null ? tutorInfo.getResearchField() : "",
                "college", tutorInfo != null && tutorInfo.getCollege() != null ? tutorInfo.getCollege() : ""
            ));
        }
        return Result.ok(tutors);
    }
}
