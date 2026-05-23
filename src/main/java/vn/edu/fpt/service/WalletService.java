package vn.edu.fpt.service;

import vn.edu.fpt.entity.Wallet;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.WalletRepository;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Tạo ví mới cho người dùng
    public Wallet createWallet(Integer userId, String name, BigDecimal initialBalance) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại!"));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setName(name);
        wallet.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);

        return walletRepository.save(wallet);
    }

    // 2. Logic chuyển tiền giữa 2 ví (Internal Transfer)
    @Transactional
    public void transferMoney(Integer fromWalletId, Integer toWalletId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền chuyển phải lớn hơn 0!");
        }

        Wallet fromWallet = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví gửi!"));

        Wallet toWallet = walletRepository.findById(toWalletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví nhận!"));

        // Kiểm tra số dư ví gửi
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Ví gửi không đủ số dư để thực hiện chuyển tiền!");
        }

        // Thực hiện trừ tiền ví A và cộng tiền ví B
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        // Lưu cả 2 ví vào DB
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Lưu ý: Trong thực tế, ông nên gọi thêm TransactionService
        // để lưu lại 2 bản ghi giao dịch (1 thu, 1 chi) cho việc truy soát.
    }

    public List<Wallet> getWalletsByUserId(Integer userId) {
        return walletRepository.findByUser_UserId(userId);
    }

    // 3. Tìm thông tin chi tiết 1 ví theo Wallet ID
    public Wallet getWalletById(Integer walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));
    }

    // 4. Logic nạp tiền thủ công vào ví
    @Transactional
    public Wallet depositMoney(Integer walletId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền nạp phải lớn hơn 0!");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        // Thực hiện cộng tiền vào số dư hiện tại
        wallet.setBalance(wallet.getBalance().add(amount));

        return walletRepository.save(wallet);
    }
}