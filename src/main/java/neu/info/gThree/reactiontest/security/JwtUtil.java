package neu.info.gThree.reactiontest.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // Token类型常量
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_EMAIL_VERIFY = "email_verify";
    public static final String TYPE_PASSWORD_RESET = "password_reset";

    // 过期时间
    private static final long EMAIL_VERIFY_EXPIRATION = 24 * 60 * 60 * 1000L;  // 24小时
    private static final long PASSWORD_RESET_EXPIRATION = 60 * 60 * 1000L;      // 1小时

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 生成登录Token
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails.getUsername());
    }

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", TYPE_ACCESS)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成邮箱验证Token
     */
    public String generateEmailVerifyToken(Long userId, String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", TYPE_EMAIL_VERIFY)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EMAIL_VERIFY_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成密码重置Token
     */
    public String generatePasswordResetToken(Long userId, String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", TYPE_PASSWORD_RESET)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取用户名/ID
     */
    public String getSubjectFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 获取Token类型
     */
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    /**
     * 获取Token中的邮箱
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /**
     * 验证Token并检查类型
     */
    public boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            String type = claims.get("type", String.class);
            return expectedType.equals(type);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 验证登录Token
     */
    public boolean validateToken(String token) {
        return validateToken(token, TYPE_ACCESS);
    }

    /**
     * 解析Token获取Claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 兼容旧方法
    public String getUsernameFromToken(String token) {
        return getSubjectFromToken(token);
    }
}