package vn.edu.fpt.repository;

import vn.edu.fpt.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    // Tìm kiếm các giao dịch thuộc về một ví cụ thể
    List<Transaction> findByWallet_WalletIdOrderByTransactionDateDesc(Integer walletId);

    // Tìm kiếm các giao dịch theo danh mục (Ăn uống, Lương...)
    List<Transaction> findByCategory_CategoryId(Integer categoryId);
}