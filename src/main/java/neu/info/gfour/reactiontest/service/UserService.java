package neu.info.gfour.reactiontest.service;

import neu.info.gfour.reactiontest.entity.User;
import neu.info.gfour.reactiontest.repository.UserRepository;
import neu.info.gfour.reactiontest.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @Transactional
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(User.UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        // 生成JWT验证Token并发送邮件
        String token = jwtUtil.generateEmailVerifyToken(savedUser.getId(), email);
        emailService.sendVerificationEmail(email, username, token);

        return savedUser;
    }

    /**
     * 验证邮箱 - 从JWT中解析用户信息
     */
    @Transactional
    public void verifyEmail(String token) {
        // 验证Token类型和有效性
        if (!jwtUtil.validateToken(token, JwtUtil.TYPE_EMAIL_VERIFY)) {
            throw new RuntimeException("无效或已过期的验证链接");
        }

        Long userId = Long.parseLong(jwtUtil.getSubjectFromToken(token));
        String email = jwtUtil.getEmailFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 确保邮箱匹配（防止token被滥用）
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("验证链接无效");
        }

        if (user.getEmailVerified()) {
            throw new RuntimeException("邮箱已验证，无需重复操作");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * 重新发送验证邮件
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getEmailVerified()) {
            throw new RuntimeException("邮箱已验证，无需重复操作");
        }

        String token = jwtUtil.generateEmailVerifyToken(user.getId(), email);
        emailService.sendVerificationEmail(email, user.getUsername(), token);
    }

    /**
     * 请求密码重置
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("该邮箱未注册"));

        String token = jwtUtil.generatePasswordResetToken(user.getId(), email);
        emailService.sendPasswordResetEmail(email, user.getUsername(), token);
    }

    /**
     * 重置密码 - 从JWT中解析用户信息
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!jwtUtil.validateToken(token, JwtUtil.TYPE_PASSWORD_RESET)) {
            throw new RuntimeException("无效或已过期的重置链接");
        }

        Long userId = Long.parseLong(jwtUtil.getSubjectFromToken(token));
        String email = jwtUtil.getEmailFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 确保邮箱匹配
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("重置链接无效");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordChangedNotification(email, user.getUsername());
    }

    /**
     * 验证重置Token是否有效
     */
    public boolean isResetTokenValid(String token) {
        return jwtUtil.validateToken(token, JwtUtil.TYPE_PASSWORD_RESET);
    }

    /**
     * 修改密码（已登录用户）
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordChangedNotification(user.getEmail(), user.getUsername());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}