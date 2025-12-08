package neu.info.gfour.reactiontest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsServiceImpl userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {
    try {
      // 跳过公开端点
      String path = request.getRequestURI();
      if (path.startsWith("/api/auth/")) {
        filterChain.doFilter(request, response);
        return;
      }
      String jwt = parseJwt(request);

      if (jwt != null && jwtUtil.validateToken(jwt)) {
        String username = jwtUtil.getUsernameFromToken(jwt);

        UserDetails userDetails =
            userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      logger.error("无法设置用户认证: {}", e);
    }

    filterChain.doFilter(request, response);
  }

  /**
   * 从请求头中提取JWT Token
   */
  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7); // 移除 "Bearer " 前缀
    }

    return null;
  }
}