package neu.info.gfour.reactiontest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupEmailService {

    private final EmailService emailService;

    @Value("${app.name:认知反应测试系统}")
    private String appName;

    @Value("${app.startup-notification.enabled:true}")
    private boolean startupNotificationEnabled;

    @Value("${app.startup-notification.recipient:chen121666@gmail.com}")
    private String recipientEmail;

    /**
     * 应用启动完成时发送邮件通知
     */
    @EventListener(ApplicationReadyEvent.class)
    public void sendStartupNotification() {
        if (!startupNotificationEnabled) {
            log.info("启动邮件通知已禁用");
            return;
        }

        try {
            log.info("准备发送启动通知邮件到: {}", recipientEmail);

            String subject = "🚀 系统上线啦！";
            String content = buildStartupEmailContent();

            emailService.sendStartupNotification(recipientEmail, subject, content);

            log.info("启动通知邮件已发送到: {}", recipientEmail);
        } catch (Exception e) {
            log.error("发送启动通知邮件失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响应用启动
        }
    }

    private String buildStartupEmailContent() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = now.format(formatter);

        return """
                <div style="max-width: 600px; margin: 0 auto; font-family: 'DIN Round', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #ffffff;">
                    <!-- Header -->
                    <div style="background: #58CC02; padding: 40px 30px; text-align: center; border-radius: 16px 16px 0 0;">
                        <div style="font-size: 64px; margin-bottom: 10px;">🚀</div>
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">系统上线啦！</h1>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px 30px; background: #ffffff;">
                        <h2 style="color: #3C3C3C; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;">太棒了！👏</h2>
                        <p style="color: #777777; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;">
                            <strong>%s</strong> 已经成功启动并开始运行了！一切准备就绪，让我们开始吧！
                        </p>

                        <!-- Info Box -->
                        <div style="background: #E8F9E8; border: 2px solid #58CC02; padding: 25px; border-radius: 12px; margin: 30px 0;">
                            <p style="margin: 8px 0; color: #3C3C3C; font-size: 15px; line-height: 1.8;">
                                <strong>📅 启动时间：</strong> %s
                            </p>
                            <p style="margin: 8px 0; color: #3C3C3C; font-size: 15px; line-height: 1.8;">
                                <strong>🌐 应用名称：</strong> %s
                            </p>
                            <p style="margin: 8px 0; color: #3C3C3C; font-size: 15px; line-height: 1.8;">
                                <strong>✅ 运行状态：</strong> <span style="color: #58CC02; font-weight: 700;">正常运行中</span>
                            </p>
                        </div>

                        <!-- Success Message -->
                        <div style="text-align: center; margin: 30px 0;">
                            <div style="font-size: 48px; margin-bottom: 15px;">🎉</div>
                            <p style="color: #58CC02; font-size: 18px; font-weight: 700; margin: 0;">
                                继续保持！系统运行状态良好
                            </p>
                        </div>

                        <p style="color: #AFAFAF; font-size: 13px; margin: 30px 0 0 0; text-align: center; line-height: 1.6;">
                            这是一封系统自动发送的启动通知邮件 📧<br>
                            如有任何问题，请联系系统管理员
                        </p>
                    </div>

                    <!-- Footer -->
                    <div style="padding: 30px; background: #F7F7F7; border-radius: 0 0 16px 16px; text-align: center;">
                        <p style="color: #AFAFAF; font-size: 12px; margin: 0;">
                            © 2025 %s. All rights reserved.
                        </p>
                    </div>
                </div>
                """
                .formatted(appName, formattedTime, appName, appName);
    }
}
