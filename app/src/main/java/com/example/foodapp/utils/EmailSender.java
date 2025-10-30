//package com.example.foodapp.utils;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.widget.Toast;
//
//import java.util.Properties;
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//
//public class EmailSender extends AsyncTask<Void, Void, Boolean> {
//
//    private Context context;
//    private String toEmail;
//    private String newPassword;
//
//    public EmailSender(Context context, String toEmail, String newPassword) {
//        this.context = context;
//        this.toEmail = toEmail;
//        this.newPassword = newPassword;
//    }
//
//    @Override
//    protected Boolean doInBackground(Void... params) {
//        final String username = "3do.service.veo@gmail.com";
//        final String password = "gnfvubkzrihwuuvl";
//
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.port", "587");
//
//        Session session = Session.getInstance(props,
//                new javax.mail.Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication(username, password);
//                    }
//                });
//
//        try {
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(username));
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
//            message.setSubject("Đặt lại mật khẩu - FoodApp");
//            message.setText("Chào bạn,\n\nMật khẩu mới của bạn là: " + newPassword +
//                    "\n\nVui lòng đăng nhập và thay đổi mật khẩu ngay lập tức.\n\nTrân trọng,\nFoodApp Team");
//
//            Transport.send(message);
//            return true;
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    @Override
//    protected void onPostExecute(Boolean success) {
//        if (success) {
//            Toast.makeText(context, "Mật khẩu mới đã được gửi đến email!", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, "Gửi email thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
//        }
//    }
//}