package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.entity.Wallet;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    // Hàm 1: Tìm danh sách ví theo Email (Dùng cho luồng Đăng nhập AuthController)
    List<Wallet> findByUser_Email(String email);

    // 👉 HÀM MỚI THÊM: Tìm danh sách ví theo UserId (Để cứu file WalletService)
    List<Wallet> findByUser_UserId(Integer userId);
}