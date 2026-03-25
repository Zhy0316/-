package com.example.archivemanagement.controller;

import com.example.archivemanagement.dto.LoginDTO;
import com.example.archivemanagement.dto.RegisterDTO;
import com.example.archivemanagement.dto.UserInfoDTO;
import com.example.archivemanagement.entity.SysRole;
import com.example.archivemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        UserInfoDTO userInfo = userService.login(loginDTO);
        if (userInfo != null) {
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO registerDTO) {
        if (userService.register(registerDTO)) {
            return ResponseEntity.ok("注册成功");
        } else {
            return ResponseEntity.badRequest().body("注册失败，用户名已存在");
        }
    }
    
    @GetMapping("/roles")
    public ResponseEntity<List<SysRole>> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }
}