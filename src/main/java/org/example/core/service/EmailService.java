package org.example.core.service;

import org.example.infrastructure.db.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Service for sending email notifications with QR code attachments.
 * Reads SMTP settings from .env file, environment variables, or application.properties.
 */
public class EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final Map<String, String> dotEnv = loadDotEnv();
    private final String smtpHost;
    private final String smtpPort;
    private final String smtpUser;
    private final String smtpPassword;
    private final boolean enabled;

    public EmailService() {
        this.smtpHost = getConf("mail.smtp.host", "smtp.gmail.com");
        this.smtpPort = getConf("mail.smtp.port", "587");
        this.smtpUser = getConf("mail.smtp.user", "kuumuwu@gmail.com");
        this.smtpPassword = getConf("mail.smtp.password", "");
        this.enabled = !smtpUser.isEmpty() && !smtpPassword.isEmpty();
        if (!enabled) {
            LOGGER.warn("EmailService: SMTP password not configured for sender {}. Email sending will log attempt.", smtpUser);
        }
    }

    private Map<String, String> loadDotEnv() {
        Map<String, String> envMap = new HashMap<>();
        File file = new File(".env");
        if (file.exists() && file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int idx = line.indexOf('=');
                    if (idx > 0) {
                        String k = line.substring(0, idx).trim();
                        String v = line.substring(idx + 1).trim();
                        envMap.put(k, v);
                    }
                }
            } catch (Exception ignored) {}
        }
        return envMap;
    }

    private String getConf(String key, String defaultValue) {
        String envKey = key.replace('.', '_').toUpperCase();
        // 1. OS environment variable
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isEmpty()) return envVal;
        // 2. .env file
        String dotEnvVal = dotEnv.get(envKey);
        if (dotEnvVal != null && !dotEnvVal.isEmpty()) return dotEnvVal;
        // 3. application.properties
        String propVal = DatabaseConfig.getProperty(key);
        return (propVal != null && !propVal.isEmpty()) ? propVal : defaultValue;
    }

    /**
     * Sends an email with the QR code PNG as an attachment and inline image.
     *
     * @param to         recipient email address
     * @param orderId    order ID for display
     * @param bookTitle  book title
     * @param seatNumber selected seat number
     * @param qrPngBytes QR code PNG image bytes
     */
    public void sendOrderQrEmail(String to, Integer orderId, String bookTitle, String seatNumber, byte[] qrPngBytes) {
        if (!enabled) {
            LOGGER.info("EmailService: Simulated email send from {} to {} for order #{}", smtpUser, to, orderId);
            return;
        }
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUser, "Book Library"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Ваш QR-код для замовлення #" + orderId + " — " + bookTitle);

            // Multipart: text + QR image attachment
            MimeMultipart multipart = new MimeMultipart();

            // Text part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(
                "<h2>Ваше замовлення #" + orderId + " підтверджено!</h2>" +
                "<p><b>Книга:</b> " + bookTitle + "</p>" +
                "<p><b>Місце:</b> " + (seatNumber != null ? seatNumber : "-") + "</p>" +
                "<p>Пред'явіть цей QR-код адміністратору при відвідуванні бібліотеки.</p>" +
                "<p>Або назвіть номер замовлення: <b>#" + orderId + "</b></p>",
                "text/html; charset=utf-8"
            );
            multipart.addBodyPart(textPart);

            // QR image attachment
            MimeBodyPart qrPart = new MimeBodyPart();
            DataSource ds = new ByteArrayDataSource(qrPngBytes, "image/png");
            qrPart.setDataHandler(new DataHandler(ds));
            qrPart.setFileName("order_" + orderId + "_qr.png");
            multipart.addBodyPart(qrPart);

            message.setContent(multipart);
            Transport.send(message);
            LOGGER.info("QR email sent from {} to {} for order #{}", smtpUser, to, orderId);
        } catch (Exception e) {
            LOGGER.error("Failed to send QR email from {} to {} for order #{}: {}", smtpUser, to, orderId, e.getMessage());
        }
    }
}
