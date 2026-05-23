package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    // Lưu tạm OTP trên RAM: Key = Email, Value = OTP
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    // Sinh OTP ngẫu nhiên 6 số
    public String generateOTP(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        return otp;
    }

    // Xác thực OTP
    public boolean validateOTP(String email, String otp) {
        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(otp)) {
            otpStorage.remove(email); // Xác thực xong thì xóa luôn để bảo mật
            return true;
        }
        return false;
    }
}