package neu.info.gfour.reactiontest.controller;

import neu.info.gfour.reactiontest.dto.request.*;
import neu.info.gfour.reactiontest.dto.response.AuthResponse;
import neu.info.gfour.reactiontest.dto.response.MessageResponse;
import neu.info.gfour.reactiontest.entity.User;
import neu.info.gfour.reactiontest.security.JwtUtil;
import neu.info.gfour.reactiontest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword());
            return ResponseEntity.ok(new MessageResponse(
                    "注册成功！验证邮件已发送至 " + user.getEmail() + "，请查收并完成验证。"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("注册失败: " + e.getMessage()));
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息（根据输入判断是邮箱还是用户名）
            User user;
            if (request.getUsernameOrEmail().contains("@")) {
                user = userService.findByEmail(request.getUsernameOrEmail());
            } else {
                user = userService.findByUsername(request.getUsernameOrEmail());
            }

            // 检查邮箱是否已验证（可选：根据需求决定是否强制验证）
            if (!user.getEmailVerified()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("请先验证您的邮箱后再登录"));
            }

            // 生成JWT Token
            String jwt = jwtUtil.generateToken(authentication);

            // 更新最后登录时间
            userService.updateLastLogin(user.getId());

            // 返回认证信息
            AuthResponse response = new AuthResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("登录失败: 用户名/邮箱或密码错误"));
        }
    }

    /**
     * 验证邮箱
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(new MessageResponse("邮箱验证成功！您现在可以登录了。"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("验证失败: " + e.getMessage()));
        }
    }

    /**
     * 重新发送验证邮件
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            userService.resendVerificationEmail(request.getEmail());
            return ResponseEntity.ok(new MessageResponse("验证邮件已重新发送，请查收。"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("发送失败: " + e.getMessage()));
        }
    }

    /**
     * 忘记密码 - 发送重置邮件
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(new MessageResponse(
                    "密码重置邮件已发送至 " + request.getEmail() + "，请在1小时内完成重置。"));
        } catch (RuntimeException e) {
            // 为安全起见，不透露邮箱是否存在
            return ResponseEntity.ok(new MessageResponse(
                    "如果该邮箱已注册，重置邮件将发送至该地址。"));
        }
    }

    /**
     * 验证重置Token是否有效
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean valid = userService.isResetTokenValid(token);
        if (valid) {
            return ResponseEntity.ok(new MessageResponse("Token有效"));
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Token无效或已过期"));
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // 验证两次密码输入一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("两次输入的密码不一致"));
        }

        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("密码重置成功！请使用新密码登录。"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("重置失败: " + e.getMessage()));
        }
    }

    /**
     * 修改密码（需要登录）
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("请先登录"));
        }

        // 验证两次密码输入一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("两次输入的密码不一致"));
        }

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("密码修改成功！"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("修改失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("未登录"));
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        return ResponseEntity.ok(user);
    }
}