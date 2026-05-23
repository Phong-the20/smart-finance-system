package vn.edu.fpt.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.service.FinancialAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private FinancialAnalysisService financialAnalysisService;

    @GetMapping("/analyze/{walletId}")
    public String analyze(@PathVariable Integer walletId) {
        return financialAnalysisService.analyzeSpending(walletId);
    }

    // Thêm tham số walletId vào URL: /api/ai/scan-receipt/{walletId}
    @PostMapping(value = "/scan-receipt/{walletId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> scanReceipt(
            @RequestParam("file") MultipartFile file,
            @PathVariable Integer walletId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng chọn một file ảnh!");
            }

            // Gọi hàm xử lý tự động quét và lưu giao dịch
            vn.edu.fpt.entity.Transaction resultTx = financialAnalysisService.scanAndCreateTransaction(file, walletId);

            // Trả về Object Giao dịch đã tạo thành công trong DB
            return ResponseEntity.ok(resultTx);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi xử lý hóa đơn tự động: " + e.getMessage());
        }
    }
}