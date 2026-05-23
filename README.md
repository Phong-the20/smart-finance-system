# 🤖 Smart Finance System - Fullstack Management App

Hệ thống quản lý tài chính cá nhân thông minh sử dụng trí tuệ nhân tạo (AI) để tự động hóa quy trình theo dõi thu chi. Dự án được phát triển Fullstack tích hợp cơ chế cô lập dữ liệu đa người dùng bảo mật.

## 🚀 Tính Năng Nổi Bật
- **Xác thực OTP qua Email:** Hệ thống đăng ký, khôi phục mật khẩu thông qua mã OTP bảo mật gửi về Gmail với cơ chế giới hạn thời gian (anti-spam 60s).
- **Mắt thần AI (Quét Bill):** Tích hợp **Gemini AI API** bóc tách tự động các thông tin (Số tiền, nội dung, ngày tháng) từ hình ảnh hóa đơn thực tế và lưu trữ trực tiếp vào database.
- **Quản lý đa tài khoản độc lập:** Thiết lập cơ chế phân tách ví (`Wallet ID`) tự động cho từng tài khoản, đảm bảo biệt lập số dư và dữ liệu thu chi thực tế.
- **Dashboard trực quan:** Thống kê biến động số dư theo thời gian thực và trực quan hóa cơ cấu chi tiêu qua biểu đồ tròn sinh động.

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)
- **Backend:** Java 21, Spring Boot 3.2.5, Spring Security (JWT Tokens), Spring Mail, JPA / Hibernate.
- **Frontend:** React.js, Vite, Axios, React Router v6, Chart.js.
- **Database:** Microsoft SQL Server.
