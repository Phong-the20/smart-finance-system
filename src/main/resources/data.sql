-- Thêm dữ liệu người dùng mẫu
INSERT INTO Users (full_name, email, password, created_at)
VALUES (N'Trần Thế Phong', 'phong@fpt.edu.vn', '123456', GETDATE());

-- Thêm các ví mẫu
INSERT INTO Wallets (user_id, name, balance, currency, created_at)
VALUES (1, N'Ví Tiền Mặt', 5000000.00, 'VND', GETDATE()),
       (1, N'Tài khoản Techcombank', 10000000.00, 'VND', GETDATE());

-- Thêm các danh mục Thu (INCOME) và Chi (EXPENSE)
INSERT INTO Categories (name, type, icon) VALUES (N'Lương', 'INCOME', 'money_bag');
INSERT INTO Categories (name, type, icon) VALUES (N'Thưởng', 'INCOME', 'gift');
INSERT INTO Categories (name, type, icon) VALUES (N'Ăn uống', 'EXPENSE', 'food');
INSERT INTO Categories (name, type, icon) VALUES (N'Tiền nhà', 'EXPENSE', 'house');
INSERT INTO Categories (name, type, icon) VALUES (N'Di chuyển', 'EXPENSE', 'car');