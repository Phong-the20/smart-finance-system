package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.edu.fpt.dto.ReceiptScanResponse;
import vn.edu.fpt.entity.Transaction;
import vn.edu.fpt.entity.Wallet;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.repository.TransactionRepository;
import vn.edu.fpt.repository.WalletRepository;
import vn.edu.fpt.repository.CategoryRepository;

import java.util.List;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class FinancialAnalysisService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AIService aiService;

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    @org.springframework.beans.factory.annotation.Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String geminiApiUrl;

    @org.springframework.beans.factory.annotation.Value("${gemini.api.key}")
    private String geminiApiKey;

    public String analyzeSpending(Integer walletId) {
        List<Transaction> transactions = transactionRepository.findByWallet_WalletIdOrderByTransactionDateDesc(walletId);

        String history = transactions.stream()
                .map(t -> String.format("- Ngày %s: %s %.0fđ (%s)",
                        t.getTransactionDate(), t.getCategory().getType(), t.getAmount(), t.getDescription()))
                .collect(Collectors.joining("\n"));

        String prompt = "Dưới đây là danh sách chi tiêu của tôi:\n" + history +
                "\nHãy phân tích thói quen chi tiêu của tôi bằng tiếng Việt, ngắn gọn, vui vẻ và đưa ra 1 lời khuyên tài chính.";

        return aiService.getFinancialAdvice(prompt);
    }

    public String analyzeReceipt(MultipartFile file) throws Exception {
        byte[] fileContent = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(fileContent);
        String mimeType = file.getContentType();

        String prompt = "Bạn là một trợ lý quét hóa đơn tài chính. Hãy đọc ảnh hóa đơn này và trích xuất các thông tin sau dưới dạng một JSON thuần túy, không để trong dấu nháy khối ```json ... ```. "
                + "Nếu không tìm thấy thông tin nào, hãy để null hoặc 0.\n"
                + "Cấu trúc JSON bắt buộc:\n"
                + "{\n"
                + "  \"amount\": (số tiền kiểu số),\n"
                + "  \"description\": \"(tên món ăn/mặt hàng hoặc tên quán ngắn gọn)\",\n"
                + "  \"categorySuggestion\": \"(gợi ý hạng mục: FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT)\"\n"
                + "}";

        Map<String, Object> inlineData = Map.of(
                "mimeType", mimeType,
                "data", base64Image
        );

        Map<String, Object> partPart1 = Map.of("text", prompt);
        Map<String, Object> partPart2 = Map.of("inlineData", inlineData);

        Map<String, Object> contents = Map.of(
                "parts", java.util.List.of(partPart1, partPart2)
        );

        Map<String, Object> requestBody = Map.of(
                "contents", java.util.List.of(contents)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            java.util.List candidates = (java.util.List) response.getBody().get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map contentRes = (Map) candidate.get("content");
            java.util.List parts = (java.util.List) contentRes.get("parts");
            Map part = (Map) parts.get(0);

            return (String) part.get("text");
        }

        throw new RuntimeException("Không thể kết nối với Gemini AI");
    }

    public Transaction scanAndCreateTransaction(MultipartFile file, Integer walletId) throws Exception {
        // 1. Gọi hàm quét ảnh bằng Gemini lấy chuỗi JSON String
        String jsonString = analyzeReceipt(file);

        // 2. Ép chuỗi String thành Object Java
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiptScanResponse receiptData = objectMapper.readValue(jsonString, ReceiptScanResponse.class);

        // 3. Lấy thông tin Ví để chuẩn bị trừ tiền
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví với ID: " + walletId));

        // 4. Tạo Object Transaction mới
        Transaction transaction = new Transaction();
        transaction.setAmount(receiptData.getAmount());
        transaction.setDescription(receiptData.getDescription());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setWallet(wallet);

        // --- LOGIC TỰ ĐỘNG PHÂN LOẠI CATEGORY TỪ AI ---
        Integer categoryId = 3; // Mặc định là 3 (Ăn uống)
        String aiSuggestion = receiptData.getCategorySuggestion();

        if (aiSuggestion != null) {
            switch (aiSuggestion.toUpperCase()) {
                case "FOOD":
                    categoryId = 3;
                    break;
                case "TRANSPORT":
                    categoryId = 5;
                    break;
                case "SHOPPING":
                case "ENTERTAINMENT":
                    categoryId = 3; // Tạm thời ánh xạ về 3 hoặc tùy chọn theo DB của ông
                    break;
            }
        }

        final Integer finalCategoryId = categoryId;
        Category category = categoryRepository.findById(finalCategoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Category với ID: " + finalCategoryId));
        transaction.setCategory(category);
        // ---------------------------------------------

        // 5. Cập nhật số dư của Ví (Trừ tiền bằng .subtract)
        wallet.setBalance(wallet.getBalance().subtract(receiptData.getAmount()));
        walletRepository.save(wallet);

        // 6. Lưu giao dịch và trả về kết quả
        return transactionRepository.save(transaction);
    }
}