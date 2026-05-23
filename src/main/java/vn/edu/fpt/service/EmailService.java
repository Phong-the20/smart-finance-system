package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Hàm cũ của ông: Gửi báo cáo AI
    public void sendReport(String toEmail, String advice) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("📊 [Smart Finance] Báo cáo tài chính & Lời khuyên từ Trợ lý AI");
        message.setText("Chào bạn,\n\nDưới đây là phân tích chi tiêu tuần này của bạn:\n\n"
                + advice + "\n\nChúc bạn một ngày làm việc hiệu quả!\nSmart Finance Team.");

        mailSender.send(message);
        System.out.println("Đã gửi email báo cáo thành công tới: " + toEmail);
    }

    // THÊM HÀM MỚI: Gửi mã OTP
    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Smart Finance] Xác thực mã OTP - " + purpose);
        message.setText("Xin chào,\n\nMã OTP của ông là: " + otp + "\nMã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ cho bất kỳ ai!\n\nSmart Finance System");

        mailSender.send(message);
        System.out.println("Đã gửi email OTP thành công tới: " + toEmail);
    }
}