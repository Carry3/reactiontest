package neu.info.gThree.reactiontest.service;

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

    @Value("${app.name:Cognitive Reaction Test System}")
    private String appName;

    @Value("${app.startup-notification.enabled:true}")
    private boolean startupNotificationEnabled;

    @Value("${app.startup-notification.recipient:chen121666@gmail.com}")
    private String recipientEmail;

    /**
     * Send email notification when application startup completes
     */
    @EventListener(ApplicationReadyEvent.class)
    public void sendStartupNotification() {
        if (!startupNotificationEnabled) {
            log.info("Startup email notification disabled");
            return;
        }

        try {
            log.info("Preparing to send startup notification email to: {}", recipientEmail);

            String subject = "🚀 System is Online!";
            String content = buildStartupEmailContent();

            emailService.sendStartupNotification(recipientEmail, subject, content);

            log.info("Startup notification email sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send startup notification email: {}", e.getMessage(), e);
            // Do not throw exception to avoid affecting application startup
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
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">System is Online!</h1>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px 30px; background: #ffffff;">
                        <h2 style="color: #3C3C3C; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;">Awesome! 👏</h2>
                        <p style="color: #777777; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;">
                            <strong>%s</strong> has successfully started and is now running! Everything is ready, let's get started!
                        </p>

                        <!-- Info Box -->
                        <div style="background: #E8F9E8; border: 2px solid #58CC02; padding: 25px; border-radius: 12px; margin: 30px 0;">
                            <p style="margin: 8px 0; color: #3C3C3C; font-size: 15px; line-height: 1.8;">
                                <strong>📅 Startup Time:</strong> %s
                            </p>
                            <p style="margin: 8px 0; color: #3C3C3C; font-size: 15px; line-height: 1.8;">
                                <strong>🌐 Application Name:</strong> %s
                            </p>
                            <p style="margin: 8px 0; color: #3C3C3C; font-size: 15px; line-height: 1.8;">
                                <strong>✅ Running Status:</strong> <span style="color: #58CC02; font-weight: 700;">Running Normally</span>
                            </p>
                        </div>

                        <!-- Success Message -->
                        <div style="text-align: center; margin: 30px 0;">
                            <div style="font-size: 48px; margin-bottom: 15px;">🎉</div>
                            <p style="color: #58CC02; font-size: 18px; font-weight: 700; margin: 0;">
                                Keep it up! System is running smoothly
                            </p>
                        </div>

                        <p style="color: #AFAFAF; font-size: 13px; margin: 30px 0 0 0; text-align: center; line-height: 1.6;">
                            This is an automatically sent startup notification email 📧<br>
                            If you have any questions, please contact the system administrator
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
