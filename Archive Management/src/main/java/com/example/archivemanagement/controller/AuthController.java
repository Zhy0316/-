package com.example.archivemanagement.controller;

import com.example.archivemanagement.common.BusinessException;
import com.example.archivemanagement.common.Result;
import com.example.archivemanagement.dto.LoginDTO;
import com.example.archivemanagement.dto.RegisterDTO;
import com.example.archivemanagement.dto.UserInfoDTO;
import com.example.archivemanagement.entity.SysRole;
import com.example.archivemanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<UserInfoDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserInfoDTO userInfo = userService.login(loginDTO);
        if (userInfo == null) throw new BusinessException("用户名或密码错误");
        return Result.ok(userInfo);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        if (!userService.register(registerDTO)) throw new BusinessException("注册失败，用户名已存在");
        return Result.ok("注册成功");
    }

    @GetMapping("/me")
    public Result<UserInfoDTO> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw BusinessException.unauthorized("未登录");
        UserInfoDTO userInfo = userService.getUserById(userId);
        if (userInfo == null) throw BusinessException.notFound("用户不存在");
        return Result.ok(userInfo);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw BusinessException.unauthorized("未登录");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("参数错误，新密码不能少于6位");
        }
        if (!userService.changePassword(userId, oldPassword, newPassword)) {
            throw new BusinessException("旧密码错误");
        }
        return Result.ok("密码修改成功");
    }

    @GetMapping("/roles")
    public Result<List<SysRole>> getAllRoles() {
        return Result.ok(userService.getAllRoles());
    }
}
