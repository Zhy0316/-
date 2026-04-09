package com.example.archivemanagement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 工具类：生成、解析、校验 Token
 */
@Component
public class JwtUtil {

    // 密钥（生产环境应从配置文件读取）
    private static final String SECRET = "bysj-archive-management-secret-key-2024-very-long";
    // Token 有效期：24小时
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param username 用户名
     * @param roleKey 角色标识
     */
    public String generateToken(Long userId, String username, String roleKey) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roleKey", roleKey)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 Token，获取 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    /**
     * 从 Token 中获取角色
     */
    public String getRoleKey(String token) {
        return parseToken(token).get("roleKey", String.class);
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
