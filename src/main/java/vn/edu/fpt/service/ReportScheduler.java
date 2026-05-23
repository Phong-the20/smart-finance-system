package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.Wallet;
import vn.edu.fpt.repository.WalletRepository;

@Service
public class ReportScheduler {

    @Autowired
    private FinancialAnalysisService aiService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private WalletRepository walletRepository;

    // Giây 0, Phút 0, Giờ 8, Mọi ngày trong tháng, Mọi tháng, Ngày thứ 2 trong tuần
    @Scheduled(cron = "0 0 8 * * MON")
    public void generateAndSendReport() {
        System.out.println("⏳ Đang tự động quét dữ liệu và gọi AI...");

        // Lấy tạm Ví số 1 (của ông) để test
        Integer walletId = 1;
        Wallet wallet = walletRepository.findById(walletId).orElse(null);

        if (wallet != null) {
            try {
                // 1. Nhờ AI phân tích
                String advice = aiService.analyzeSpending(walletId);

                // 2. Lấy email của chủ ví và Gửi
                String userEmail = "tranthephong208@gmail.com";
                emailService.sendReport(userEmail, advice);

            } catch (Exception e) {
                System.out.println("Lỗi khi gửi email: " + e.getMessage());
            }
        }
    }
}