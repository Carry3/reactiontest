package neu.info.gfour.reactiontest.service;

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

    @Value("${app.name:认知反应测试系统}")
    private String appName;

    /**
     * 发送邮箱验证邮件
     */
    @Async
    public void sendVerificationEmail(String to, String username, String token) {
        String subject = "🎉 欢迎加入！验证您的邮箱";
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;

        String content = buildVerificationEmailContent(username, verifyUrl);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送密码重置邮件
     */
    @Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        String subject = "🔑 重置您的密码";
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String content = buildPasswordResetEmailContent(username, resetUrl);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送密码修改成功通知
     */
    @Async
    public void sendPasswordChangedNotification(String to, String username) {
        String subject = "✅ 密码修改成功！";
        String content = buildPasswordChangedContent(username);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送系统启动通知邮件
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
            log.info("邮件发送成功: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("邮件发送失败: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
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
                        <h2 style="color: #3C3C3C; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;">嗨，%s！👋</h2>
                        <p style="color: #777777; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;">
                            太好了！您离开始认知测试之旅只差一步了。点击下方按钮验证您的邮箱，让我们开始吧！
                        </p>

                        <!-- CTA Button -->
                        <div style="text-align: center; margin: 35px 0;">
                            <a href="%s" style="display: inline-block; background: #58CC02; color: white; padding: 16px 48px;
                               text-decoration: none; border-radius: 12px; font-weight: 700; font-size: 16px;
                               box-shadow: 0 4px 0 #46A302; transition: all 0.2s;">
                               立即验证邮箱
                            </a>
                        </div>

                        <p style="color: #AFAFAF; font-size: 14px; line-height: 1.5; margin: 30px 0 0 0; text-align: center;">
                            按钮无法点击？复制这个链接：<br>
                            <a href="%s" style="color: #58CC02; word-break: break-all;">%s</a>
                        </p>
                    </div>

                    <!-- Footer -->
                    <div style="padding: 30px; background: #F7F7F7; border-radius: 0 0 16px 16px; text-align: center;">
                        <p style="color: #AFAFAF; font-size: 12px; margin: 0; line-height: 1.5;">
                            链接24小时后失效 ⏰<br>
                            如果这不是您的操作，请忽略此邮件
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
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">重置密码</h1>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px 30px; background: #ffffff;">
                        <h2 style="color: #3C3C3C; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;">嗨，%s！👋</h2>
                        <p style="color: #777777; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;">
                            别担心，忘记密码很常见！点击下方按钮，我们帮您重新设置一个新密码。
                        </p>

                        <!-- CTA Button -->
                        <div style="text-align: center; margin: 35px 0;">
                            <a href="%s" style="display: inline-block; background: #FF9600; color: white; padding: 16px 48px;
                               text-decoration: none; border-radius: 12px; font-weight: 700; font-size: 16px;
                               box-shadow: 0 4px 0 #CC7700; transition: all 0.2s;">
                               重置我的密码
                            </a>
                        </div>

                        <p style="color: #AFAFAF; font-size: 14px; line-height: 1.5; margin: 30px 0 0 0; text-align: center;">
                            按钮无法点击？复制这个链接：<br>
                            <a href="%s" style="color: #FF9600; word-break: break-all;">%s</a>
                        </p>

                        <div style="background: #FFF4E5; border-left: 4px solid #FF9600; padding: 15px; margin-top: 30px; border-radius: 4px;">
                            <p style="color: #3C3C3C; font-size: 14px; margin: 0; line-height: 1.5;">
                                ⏰ <strong>提示：</strong>此链接将在 1 小时后失效
                            </p>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div style="padding: 30px; background: #F7F7F7; border-radius: 0 0 16px 16px; text-align: center;">
                        <p style="color: #AFAFAF; font-size: 12px; margin: 0; line-height: 1.5;">
                            没有请求重置密码？您的账号很安全 🔒<br>
                            请忽略此邮件即可
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
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">密码修改成功！</h1>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px 30px; background: #ffffff;">
                        <h2 style="color: #3C3C3C; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;">嗨，%s！👋</h2>
                        <p style="color: #777777; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                            太棒了！您的密码已成功修改。现在您可以使用新密码登录了。
                        </p>

                        <!-- Success Box -->
                        <div style="background: #E8F9E8; border: 2px solid #58CC02; padding: 20px; border-radius: 12px; margin: 30px 0; text-align: center;">
                            <div style="font-size: 48px; margin-bottom: 10px;">✅</div>
                            <p style="color: #3C3C3C; font-size: 16px; font-weight: 700; margin: 0;">
                                您的账号安全得到保障
                            </p>
                        </div>

                        <div style="background: #FFF4E5; border-left: 4px solid #FF9600; padding: 15px; margin-top: 20px; border-radius: 4px;">
                            <p style="color: #3C3C3C; font-size: 14px; margin: 0; line-height: 1.5;">
                                ⚠️ <strong>这不是您的操作？</strong><br>
                                如果您没有修改密码，请立即联系我们的支持团队。
                            </p>
                        </div>

                        <p style="color: #AFAFAF; font-size: 12px; margin: 30px 0 0 0; text-align: center;">
                            修改时间：%s
                        </p>
                    </div>

                    <!-- Footer -->
                    <div style="padding: 30px; background: #F7F7F7; border-radius: 0 0 16px 16px; text-align: center;">
                        <p style="color: #AFAFAF; font-size: 12px; margin: 0;">
                            继续加油！💪 保持您的学习连续性
                        </p>
                    </div>
                </div>
                """
                .formatted(username, java.time.LocalDateTime.now().toString());
    }
}