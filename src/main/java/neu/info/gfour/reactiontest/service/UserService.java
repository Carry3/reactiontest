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
     * User registration
     */
    @Transactional
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(User.UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        // Generate JWT verification token and send email
        String token = jwtUtil.generateEmailVerifyToken(savedUser.getId(), email);
        emailService.sendVerificationEmail(email, username, token);

        return savedUser;
    }

    /**
     * Verify email - parse user information from JWT
     */
    @Transactional
    public void verifyEmail(String token) {
        // Verify token type and validity
        if (!jwtUtil.validateToken(token, JwtUtil.TYPE_EMAIL_VERIFY)) {
            throw new RuntimeException("Invalid or expired verification link");
        }

        Long userId = Long.parseLong(jwtUtil.getSubjectFromToken(token));
        String email = jwtUtil.getEmailFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        // Ensure email matches (prevent token abuse)
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("Verification link is invalid");
        }

        if (user.getEmailVerified()) {
            throw new RuntimeException("Email already verified, no need to repeat");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        if (user.getEmailVerified()) {
            throw new RuntimeException("Email already verified, no need to repeat");
        }

        String token = jwtUtil.generateEmailVerifyToken(user.getId(), email);
        emailService.sendVerificationEmail(email, user.getUsername(), token);
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("This email is not registered"));

        String token = jwtUtil.generatePasswordResetToken(user.getId(), email);
        emailService.sendPasswordResetEmail(email, user.getUsername(), token);
    }

    /**
     * Reset password - parse user information from JWT
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!jwtUtil.validateToken(token, JwtUtil.TYPE_PASSWORD_RESET)) {
            throw new RuntimeException("Invalid or expired reset link");
        }

        Long userId = Long.parseLong(jwtUtil.getSubjectFromToken(token));
        String email = jwtUtil.getEmailFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        // Ensure email matches
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("Reset link is invalid");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordChangedNotification(email, user.getUsername());
    }

    /**
     * Verify if reset token is valid
     */
    public boolean isResetTokenValid(String token) {
        return jwtUtil.validateToken(token, JwtUtil.TYPE_PASSWORD_RESET);
    }

    /**
     * Change password (logged in user)
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Incorrect old password");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as old password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordChangedNotification(user.getEmail(), user.getUsername());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}