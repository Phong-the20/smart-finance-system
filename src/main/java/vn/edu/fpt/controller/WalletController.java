package vn.edu.fpt.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import vn.edu.fpt.dto.TransferRequest;
import vn.edu.fpt.entity.Wallet;
import vn.edu.fpt.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin("*")
public class WalletController {

    @Autowired
    private vn.edu.fpt.repository.CategoryRepository categoryRepository;

    @Autowired
    private vn.edu.fpt.repository.TransactionRepository transactionRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private MessageSource messageSource;

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        walletService.transferMoney(request.getFromWalletId(), request.getToWalletId(), request.getAmount());

        // Lấy thông báo từ file properties
        String message = messageSource.getMessage("transfer.success", null, LocaleContextHolder.getLocale());

        // Trả về ResponseEntity kèm theo mã HTTP 200 OK
        return ResponseEntity.ok(message);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wallet>> getWallets(@PathVariable Integer userId) {
        return ResponseEntity.ok(walletService.getWalletsByUserId(userId));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Integer walletId) {
        // Giả sử WalletService của ông đã có hàm tìm ví theo ID, ví dụ: walletService.getWalletById(walletId)
        // Hoặc ông gọi trực tiếp repo nếu service chưa viết: walletRepository.findById(walletId).orElseThrow(...)
        return ResponseEntity.ok(walletService.getWalletById(walletId));
    }

    @PutMapping("/{walletId}/deposit")
    public ResponseEntity<Wallet> deposit(@PathVariable Integer walletId, @RequestParam java.math.BigDecimal amount) {
        Wallet updatedWallet = walletService.depositMoney(walletId, amount);
        return ResponseEntity.ok(updatedWallet);
    }

    @GetMapping("/{walletId}/transactions") // Phải là @GetMapping nha ông!
    public ResponseEntity<List<vn.edu.fpt.entity.Transaction>> getWalletTransactions(@PathVariable Integer walletId) {
        return ResponseEntity.ok(transactionRepository.findByWallet_WalletIdOrderByTransactionDateDesc(walletId));
    }

    @PostMapping("/{walletId}/add-transaction")
    public ResponseEntity<vn.edu.fpt.entity.Transaction> addManualTransaction(
            @PathVariable Integer walletId,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam String description,
            @RequestParam Integer categoryId) {


        // THAY BẰNG ĐOẠN NÀY:
        Wallet wallet = walletService.getWalletById(walletId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Số dư ví không đủ để thực hiện chi tiêu này!");
        }

// Trừ số dư trực tiếp của đối tượng ví và lưu thẳng vào SQL Server
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletService.createWallet(wallet.getUser().getUserId(), wallet.getName(), wallet.getBalance());
// Hoặc nếu WalletRepository đã được Autowired ở trên, ông dùng dòng dưới này cho ngắn gọn:
// walletRepository.save(wallet);

        // 2. Lấy thông tin danh mục
        vn.edu.fpt.entity.Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        // 3. Tạo và lưu bản ghi giao dịch mới
        vn.edu.fpt.entity.Transaction transaction = new vn.edu.fpt.entity.Transaction();
        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionDate(java.time.LocalDateTime.now());

        return ResponseEntity.ok(transactionRepository.save(transaction));
    }
}