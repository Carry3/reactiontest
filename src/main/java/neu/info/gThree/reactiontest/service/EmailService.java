package neu.info.gThree.reactiontest.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Cognitive Reaction Test System}")
    private String appName;

    /**
     * Send email verification email
     */
    @Async
    public void sendVerificationEmail(String to, String username, String token) {
        String subject = "🎉 Welcome! Verify Your Email";
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;

        String content = buildVerificationEmailContent(username, verifyUrl);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        String subject = "🔑 Reset Your Password";
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String content = buildPasswordResetEmailContent(username, resetUrl);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * Send password change success notification
     */
    @Async
    public void sendPasswordChangedNotification(String to, String username) {
        String subject = "✅ Password Changed Successfully!";
        String content = buildPasswordChangedContent(username);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * Send system startup notification email
     */
    @Async
    public void sendStartupNotification(String to, String subject, String htmlContent) {
        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("Email send failed: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("Email send failed", e);
        }
    }

    private String buildVerificationEmailContent(String username, String verifyUrl) {
        return """
                <div style="max-width: 600px; margin: 0 auto; font-family: 'DIN Round', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #ffffff;">
                    <!-- Header -->
                    <div style="background: #58CC02; padding: 40px 30px; text-align: center; border-radius: 16px 16px 0 0;">
                        <div style="font-size: 64px; margin-bottom: 10px;">🧠</div>
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">%s</h1>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px 30px; background: #ffffff;">
                        <h2 style="color: #3C3C3C; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;">Hi, %s! 👋</h2>
                        <p style="color: #777777; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;">
                            Great! You're just one step away from starting your cognitive testing journey. Click the button below to verify your email and let's get started!
                        </p>

                        <!-- CTA Button -->
                        <div style="text-align: center; margin: 35px 0;">
                            <a href="%s" style="display: inline-block; background: #58CC02; color: white; padding: 16px 48px;
                               text-decoration: none; border-radius: 12px; font-weight: 700; font-size: 16px;
                               box-shadow: 0 4px 0 #46A302; transition: all 0.2s;">
                               Verify Email Now
                            </a>
                        </div>

                        <p style="color: #AFAFAF; font-size: 14px; line-height: 1.5; margin: 30px 0 0 0; text-align: center;">
                            Button not working? Copy this link:<br>
                            <a href="%s" style="color: #58CC02; word-break: break-all;">%s</a>
                        </p>
                    </div>

                    <!-- Footer -->
                    <div style="padding: 30px; background: #F7F7F7; border-radius: 0 0 16px 16px; text-align: center;">
                        <p style="color: #AFAFAF; font-size: 12px; margin: 0; line-height: 1.5;">
                            Link expires in 24 hours ⏰<br>
                            If this wasn't you, please ignore this email
                        </p>
                    </div>
                </div>
                """
                .formatted(appName, username, verifyUrl, verifyUrl, verifyUrl);
    }

    private String buildPasswordResetEmailContent(String username, String resetUrl) {
        return """
                <div style="max-width: 600px; margin: 0 auto; font-family: 'DIN Round', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #ffffff;">
                    <!-- Header -->
                    <div style="background: #FF9600; padding: 40px 30px; text-align: center; border-radius: 16px 16px 0 0;">
                        <div style="font-size: 64px; margin-bottom: 10px;">🔑</div>
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">Reset Password</h1>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px 30px; background: #ffffff;">
                        <h2 style="color: #3C3C3C; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;">Hi, %s! 👋</h2>
                        <p style="color: #777777; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;">
                            No worries, forgetting passwords is common! Click the button below and we'll help you set a new password.
                        </p>

                        <!-- CTA Button -->
                        <div style="text-align: center; margin: 35px 0;">
                            <a href="%s" style="display: inline-block; background: #FF9600; color: white; padding: 16px 48px;
                               text-decoration: none; border-radius: 12px; font-weight: 700; font-size: 16px;
                               box-shadow: 0 4px 0 #CC7700; transition: all 0.2s;">
                               Reset My Password
                            </a>
                        </div>

                        <p style="color: #AFAFAF; font-size: 14px; line-height: 1.5; margin: 30px 0 0 0; text-align: center;">
                            Button not working? Copy this link:<br>
                            <a href="%s" style="color: #FF9600; word-break: break-all;">%s</a>
                        </p>

                        <div style="background: #FFF4E5; border-left: 4px solid #FF9600; padding: 15px; margin-top: 30px; border-radius: 4px;">
                            <p style="color: #3C3C3C; font-size: 14px; margin: 0; line-height: 1.5;">
                                ⏰ <strong>Note:</strong> This link will expire in 1 hour
                            </p>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div style="padding: 30px; background: #F7F7F7; border-radius: 0 0 16px 16px; text-align: center;">
                        <p style="color: #AFAFAF; font-size: 12px; margin: 0; line-height: 1.5;">
                            Didn't request a password reset? Your account is safe 🔒<br>
                            Please ignore this email
                        </p>
                    </div>
                </div>
                """
                .formatted(username, resetUrl, resetUrl, resetUrl);
    }

    private String buildPasswordChangedContent(String username) {
        return """
                <div style="max-width: 600px; margin: 0 auto; font-family: 'DIN Round', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #ffffff;">
                    <!-- Header -->
                    <div style="background: #58CC02; padding: 40px 30px; text-align: center; border-radius: 16px 16px 0 0;">
                        <div style="font-size: 64px; margin-bottom: 10px;">🎉</div>
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">Password Changed Successfully!</h1>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px 30px; background: #ffffff;">
                        <h2 style="color: #3C3C3C; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;">Hi, %s! 👋</h2>
                        <p style="color: #777777; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                            Awesome! Your password has been successfully changed. You can now log in with your new password.
                        </p>

                        <!-- Success Box -->
                        <div style="background: #E8F9E8; border: 2px solid #58CC02; padding: 20px; border-radius: 12px; margin: 30px 0; text-align: center;">
                            <div style="font-size: 48px; margin-bottom: 10px;">✅</div>
                            <p style="color: #3C3C3C; font-size: 16px; font-weight: 700; margin: 0;">
                                Your account is secure
                            </p>
                        </div>

                        <div style="background: #FFF4E5; border-left: 4px solid #FF9600; padding: 15px; margin-top: 20px; border-radius: 4px;">
                            <p style="color: #3C3C3C; font-size: 14px; margin: 0; line-height: 1.5;">
                                ⚠️ <strong>This wasn't you?</strong><br>
                                If you didn't change your password, please contact our support team immediately.
                            </p>
                        </div>

                        <p style="color: #AFAFAF; font-size: 12px; margin: 30px 0 0 0; text-align: center;">
                            Changed at: %s
                        </p>
                    </div>

                    <!-- Footer -->
                    <div style="padding: 30px; background: #F7F7F7; border-radius: 0 0 16px 16px; text-align: center;">
                        <p style="color: #AFAFAF; font-size: 12px; margin: 0;">
                            Keep it up! 💪 Maintain your learning streak
                        </p>
                    </div>
                </div>
                """
                .formatted(username, java.time.LocalDateTime.now().toString());
    }
}