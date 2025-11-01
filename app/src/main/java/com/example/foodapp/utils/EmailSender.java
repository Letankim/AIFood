package com.example.foodapp.utils;

import android.content.Context;
import android.widget.Toast;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailSender {

    private final Context context;
    private final String toEmail;
    private final String newPassword;

    public EmailSender(Context context, String toEmail, String newPassword) {
        this.context = context;
        this.toEmail = toEmail;
        this.newPassword = newPassword;
    }

    public void send() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean success = sendMail();
            ((android.app.Activity) context).runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(context, "Mật khẩu mới đã được gửi đến email!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Gửi email thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean sendMail() {
        final String username = "3do.service.veo@gmail.com";
        final String password = "gnfvubkzrihwuuvl";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        try {
            Session session = Session.getInstance(props,
                    new jakarta.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "FoodApp Team"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Đặt lại mật khẩu - FoodApp");
            message.setText("Chào bạn,\n\nMật khẩu mới của bạn là: " + newPassword +
                    "\n\nVui lòng đăng nhập và thay đổi mật khẩu ngay lập tức.\n\nTrân trọng,\nFoodApp Team");

            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
