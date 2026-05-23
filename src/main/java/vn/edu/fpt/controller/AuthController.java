package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.dto.LoginRequest;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.security.JwtUtils;
import vn.edu.fpt.service.EmailService;
import vn.edu.fpt.service.OtpService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import vn.edu.fpt.entity.Wallet;
import vn.edu.fpt.repository.WalletRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*") // Thêm cái này để mở cổng cho React
public class AuthController {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    // Sửa lại đoạn tìm Ví trong hàm login của AuthController.java
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            String token = jwtUtils.generateToken(user.getEmail());

            // 1. Lấy ra danh sách ví theo email
            List<Wallet> wallets = walletRepository.findByUser_Email(user.getEmail());

            // 2. Nếu danh sách không trống, lấy ID của cái ví đầu tiên (index 0)
            Integer walletId = (wallets != null && !wallets.isEmpty()) ? wallets.get(0).getWalletId() : 1;

            // Đóng gói gửi về Frontend
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("walletId", walletId);
            response.put("fullName", user.getFullName());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Email hoặc mật khẩu không đúng!");
        }
    }

    // ================= LUỒNG ĐĂNG KÝ MỚI TÀI KHOẢN =================
    @PostMapping("/register/send-otp")
    public ResponseEntity<String> sendRegisterOtp(@RequestParam String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email này đã được đăng ký!");
        }
        String otp = otpService.generateOTP(email);
        emailService.sendOtpEmail(email, otp, "Đăng ký tài khoản");
        return ResponseEntity.ok("Mã OTP đã được gửi đến email của ông!");
    }

    @PostMapping("/register/verify")
    public ResponseEntity<String> verifyRegisterOtp(@RequestParam String email, @RequestParam String otp, @RequestParam String password) {
        if (!otpService.validateOTP(email, otp)) {
            return ResponseEntity.badRequest().body("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password);
        String defaultName = email.split("@")[0];
        newUser.setFullName(defaultName);
        newUser.setCreatedAt(java.time.LocalDateTime.now());

        userRepository.save(newUser); // Lưu User trước

        // TẠO NGAY MỘT CÁI VÍ RIÊNG CHO TÀI KHOẢN MỚI
        Wallet newWallet = new Wallet();
        newWallet.setUser(newUser);
        newWallet.setName("Ví Tiền Mặt");
        newWallet.setBalance(java.math.BigDecimal.ZERO); // Tiền mặc định là 0 đồng
        newWallet.setCurrency("VND");
        newWallet.setCreatedAt(java.time.LocalDateTime.now());
        walletRepository.save(newWallet); // Lưu Ví

        return ResponseEntity.ok("Xác thực OTP thành công! Đăng ký hoàn tất.");
    }

    // ================= LUỒNG QUÊN MẬT KHẨU =================
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<String> sendForgotPasswordOtp(@RequestParam String email) {
        if (!userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email không tồn tại trong hệ thống!");
        }
        String otp = otpService.generateOTP(email);
        emailService.sendOtpEmail(email, otp, "Khôi phục mật khẩu");
        return ResponseEntity.ok("Mã OTP khôi phục đã được gửi!");
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword) {
        if (!otpService.validateOTP(email, otp)) {
            return ResponseEntity.badRequest().body("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        // Lấy User lên và đổi pass mới
        User user = userRepository.findByEmail(email).get();
        user.setPassword(newPassword);
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công! Hãy đăng nhập lại.");
    }
}