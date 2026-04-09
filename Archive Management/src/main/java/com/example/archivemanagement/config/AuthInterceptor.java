package com.example.archivemanagement.config;

import com.example.archivemanagement.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限拦截器：校验接口访问权限（按角色区分）
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // 公开接口放行
        if (isPublicUri(uri)) {
            return true;
        }

        // 校验 Token
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\",\"data\":null}");
            return false;
        }

        // 管理员接口权限校验
        if (uri.startsWith("/api/admin/")) {
            String roleKey = (String) request.getAttribute("roleKey");
            if (!"admin".equals(roleKey)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\",\"data\":null}");
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否为公开接口（无需登录）
     */
    private boolean isPublicUri(String uri) {
        return uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/auth/register")
                || uri.startsWith("/api/auth/roles")
                || uri.startsWith("/api/enterprise/register")
                || uri.startsWith("/uploads/");
    }
}
