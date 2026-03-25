package com.example.archivemanagement.controller;

import com.example.archivemanagement.dto.LoginDTO;
import com.example.archivemanagement.dto.RegisterDTO;
import com.example.archivemanagement.dto.UserInfoDTO;
import com.example.archivemanagement.entity.SysRole;
import com.example.archivemanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 登录，返回含 token 的用户信息 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        UserInfoDTO userInfo = userService.login(loginDTO);
        if (userInfo != null) {
            return ResponseEntity.ok(userInfo);
        }
        return ResponseEntity.badRequest().body("用户名或密码错误");
    }

    /** 注册 */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO registerDTO) {
        if (userService.register(registerDTO)) {
            return ResponseEntity.ok("注册成功");
        }
        return ResponseEntity.badRequest().body("注册失败，用户名已存在");
    }

    /** 获取当前登录用户信息（需携带 Token） */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        UserInfoDTO userInfo = userService.getUserById(userId);
        if (userInfo == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }
        return ResponseEntity.ok(userInfo);
    }

    /** 修改密码 */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("参数错误，新密码不能少于6位");
        }
        if (userService.changePassword(userId, oldPassword, newPassword)) {
            return ResponseEntity.ok("密码修改成功");
        }
        return ResponseEntity.badRequest().body("旧密码错误");
    }

    /** 获取所有角色列表 */
    @GetMapping("/roles")
    public ResponseEntity<List<SysRole>> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }
}
