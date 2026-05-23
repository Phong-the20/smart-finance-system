package vn.edu.fpt.service;

import vn.edu.fpt.entity.*;
import vn.edu.fpt.repository.*;
import vn.edu.fpt.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Transaction createTransaction(Integer walletId, Integer categoryId, BigDecimal amount, String description) {
        // 1. Kiểm tra ví và danh mục có tồn tại không
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví!"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không hợp lệ!"));

        // 2. Lưu số dư cũ để làm bằng chứng (Audit Trail)
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter;

        // 3. Xử lý logic cộng/trừ tiền dựa trên loại danh mục
        if ("EXPENSE".equalsIgnoreCase(category.getType())) {
            if (balanceBefore.compareTo(amount) < 0) {
                throw new RuntimeException("Số dư ví không đủ để thực hiện giao dịch!");
            }
            balanceAfter = balanceBefore.subtract(amount);
        } else {
            balanceAfter = balanceBefore.add(amount);
        }

        // 4. Cập nhật số dư mới vào ví
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // 5. Tạo và lưu lịch sử giao dịch
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);

        return transactionRepository.save(transaction);
    }
}