package chemlab_system.util;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Simple email helper for the JavaFX desktop app.
 *
 * NOTE: Requires JavaMail 1.6.x (javax.mail) on the classpath.
 *
 * Update SMTP_USERNAME and SMTP_APP_PASSWORD with real credentials before use.
 */
public final class EmailService {

    // TODO: Replace with your Gmail address
    private static final String SMTP_USERNAME = "lexmatondo2719@gmail.com";

    // TODO: Replace with your Gmail App Password (NOT your normal password)
    private static final String SMTP_APP_PASSWORD = "vyox dddf kvgv xijw";

    private EmailService() {
    }

    /**
     * Sends an email to the student group when their request status changes.
     * Runs on a background thread to avoid blocking the UI.
     */
    public static void sendStatusNotification(String toEmail,
                                              String groupName,
                                              String apparatusName,
                                              int qty,
                                              String newStatus) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("EmailService: toEmail is empty; skipping notification.");
            return;
        }

        // Only intended for Approved/Rejected per spec, but keep method generic.
        Runnable job = () -> {
            try {
                Session session = createSession();

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_USERNAME, "ChemLab System"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
                message.setSubject("ChemLab Request Status Update: " + newStatus, "UTF-8");
                message.setContent(buildHtmlBody(groupName, apparatusName, qty, newStatus), "text/html; charset=UTF-8");

                Transport.send(message);
                System.out.println("EmailService: notification sent to " + toEmail);
            } catch (Exception ex) {
                System.err.println("EmailService: failed to send email: " + ex.getMessage());
                ex.printStackTrace();
            }
        };

        Thread t = new Thread(job, "EmailService-sendStatusNotification");
        t.setDaemon(true);
        t.start();
    }

    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_APP_PASSWORD);
            }
        };

        return Session.getInstance(props, auth);
    }

    private static String buildHtmlBody(String groupName, String apparatusName, int qty, String newStatus) {
        String statusColor = "Approved".equalsIgnoreCase(newStatus) ? "#2e7d32"
                : ("Rejected".equalsIgnoreCase(newStatus) ? "#dc3545" : "#333333");

        return "<!doctype html>"
                + "<html><head><meta charset='utf-8'></head><body style='font-family: Arial, sans-serif; background:#f6f6f6; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow:hidden; border:1px solid #e6e6e6;'>"
                + "  <div style='background: linear-gradient(135deg, #1a5d1a, #2e7d32); padding: 16px 20px; color:#fff;'>"
                + "    <h2 style='margin:0; font-size: 18px;'>ChemLab System</h2>"
                + "    <div style='opacity:0.9; margin-top:4px;'>Request Status Update</div>"
                + "  </div>"
                + "  <div style='padding: 18px 20px; color:#333;'>"
                + "    <p style='margin:0 0 10px 0;'>Hello <b>" + escape(groupName) + "</b>,</p>"
                + "    <p style='margin:0 0 14px 0;'>Your borrowing request status has been updated, please claim in the chemistry laboratory:</p>"
                + "    <table style='width:100%; border-collapse: collapse;'>"
                + "      <tr><td style='padding:8px 0; color:#666;'>Apparatus</td><td style='padding:8px 0; text-align:right;'><b>" + escape(apparatusName) + "</b></td></tr>"
                + "      <tr><td style='padding:8px 0; color:#666;'>Quantity</td><td style='padding:8px 0; text-align:right;'><b>" + qty + "</b></td></tr>"
                + "      <tr><td style='padding:8px 0; color:#666;'>New Status</td><td style='padding:8px 0; text-align:right;'><b style='color:" + statusColor + ";'>" + escape(newStatus) + "</b></td></tr>"
                + "    </table>"
                + "    <hr style='border:none; border-top:1px solid #eee; margin: 16px 0;'/>"
                + "    <p style='margin:0; font-size: 12px; color:#777;'>This is an automated message from the Chemistry Lab System.</p>"
                + "  </div>"
                + "</div>"
                + "</body></html>";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
