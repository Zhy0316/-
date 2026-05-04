package com.example.archivemanagement.config;

import com.example.archivemanagement.entity.InfoEnterprise;
import com.example.archivemanagement.mapper.InfoEnterpriseMapper;
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
    private final InfoEnterpriseMapper enterpriseMapper;

    /** 需要企业认证才能访问的接口前缀 */
    private static final String[] ENTERPRISE_CERTIFIED_URIS = {
        "/api/admin/users",       // 学生列表（HR通过admin接口访问）
        "/api/student/profile/",  // 学生档案详情
        "/api/growth-score/",     // 成长评分
        "/api/ai/student-recommend" // AI学生推荐
    };

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String uri = request.getRequestURI();

        if (isPublicUri(uri)) return true;

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            write401(response);
            return false;
        }

        // 管理员接口权限校验
        if (uri.startsWith("/api/admin/")) {
            String roleKey = (String) request.getAttribute("roleKey");
            if (!"admin".equals(roleKey)) {
                write403(response, "无权限访问");
                return false;
            }
        }

        // 企业认证校验：hr 角色访问敏感接口时必须已认证
        String roleKey = (String) request.getAttribute("roleKey");
        if ("hr".equals(roleKey) && requiresEnterpriseCert(uri)) {
            InfoEnterprise enterprise = enterpriseMapper.selectById(userId);
            if (enterprise == null || enterprise.getAuditStatus() == null
                    || enterprise.getAuditStatus() != 1) {
                write403(response, "企业尚未通过认证，无法访问该功能");
                return false;
            }
        }

        return true;
    }

    private boolean requiresEnterpriseCert(String uri) {
        for (String prefix : ENTERPRISE_CERTIFIED_URIS) {
            if (uri.startsWith(prefix)) return true;
        }
        return false;
    }

    private void write401(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\",\"data\":null}");
    }

    private void write403(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"" + msg + "\",\"data\":null}");
    }

    private boolean isPublicUri(String uri) {
        return uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/auth/register")
                || uri.startsWith("/api/auth/roles")
                || uri.startsWith("/api/enterprise/register")
                || uri.startsWith("/uploads/");
    }
}
