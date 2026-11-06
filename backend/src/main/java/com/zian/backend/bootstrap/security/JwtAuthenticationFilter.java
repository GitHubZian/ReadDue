package com.zian.backend.bootstrap.security;

import com.zian.backend.iam.application.AuthService;
import com.zian.backend.shared.security.JwtService;
import com.zian.backend.shared.security.LoginUser;
import com.zian.backend.shared.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 认证过滤器：解析 {@code Authorization: Bearer <token>}，写入 SecurityContext 与 UserContext。
 *
 * <p>token 只用来证明「是谁」；角色与权限每请求从数据库重新装载（{@link AuthService#principalOf}），
 * 因此伪造 claim 提权无效，拉黑 / 改角色 / 改权限也能立即生效。
 *
 * <p>解析失败不在此处返回 401，而是保持匿名继续走过滤器链，
 * 由 {@link RestAuthenticationEntryPoint} 对需要认证的接口统一输出 401 JSON。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            try {
                LoginUser claimed = jwtService.parse(header.substring(BEARER.length()).trim());
                LoginUser user = authService.principalOf(claimed.id());
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.role()));
                user.permissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                UserContext.set(user);
            } catch (RuntimeException e) {
                SecurityContextHolder.clearContext();
                UserContext.clear();
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
