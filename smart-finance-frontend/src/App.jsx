// src/App.jsx
import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import api from './api/axiosConfig';
import Sidebar from './components/Sidebar';
import Dashboard from './components/Dashboard';
import ScanReceipt from './components/ScanReceipt';
import ProtectedRoute from './components/ProtectedRoute';
import TransactionHistory from './components/TransactionHistory';

function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));

  // --- STATE QUẢN LÝ FORM ---
  const [authMode, setAuthMode] = useState('login'); // login | register | forgot
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [message, setMessage] = useState({ type: '', content: '' }); // Để hiện thông báo xanh/đỏ
  const [isOtpSent, setIsOtpSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);

  const resetForm = () => {
    setEmail(''); setPassword(''); setOtp('');
    setMessage({ type: '', content: '' }); setIsOtpSent(false);
  };

  // ================= XỬ LÝ API =================

  // 1. Đăng nhập
  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/auth/login', { email, password });

      // Bóc tách cả 3 dữ liệu từ Backend đẩy về
      const jwtToken = response.data.token;
      const userWalletId = response.data.walletId;
      const userFullName = response.data.fullName; // 👉 BÓC TÁCH THÊM TÊN ĐỘNG

      localStorage.setItem('token', jwtToken);
      localStorage.setItem('walletId', userWalletId);
      localStorage.setItem('fullName', userFullName); // 👉 GĂM TÊN ĐỘNG VÀO TRÌNH DUYỆT Ở ĐÂY!

      setToken(jwtToken);
    } catch (err) {
      setMessage({ type: 'error', content: 'Email hoặc mật khẩu không đúng!' });
    }
  };

  // Hàm Đăng xuất để xóa sạch dấu vết tài khoản cũ
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('walletId');
    localStorage.removeItem('fullName'); // 👉 XÓA LUÔN TÊN KHI LOGOUT
    setToken(null);
    resetForm();
  };

  // 2. Gửi OTP (Đã tích hợp cơ chế chống spam 60s)
  const handleSendOtp = async () => {
    if (!email) return alert("Vui lòng nhập Email!");

    // 👉 Nếu đang trong thời gian đếm ngược thì chặn không cho bấm
    if (countdown > 0) return;

    setLoading(true);
    setMessage({ type: '', content: '' });

    try {
      const endpoint = authMode === 'register' ? '/auth/register/send-otp' : '/auth/forgot-password/send-otp';
      await api.post(`${endpoint}?email=${email}`);

      setIsOtpSent(true);
      setMessage({ type: 'success', content: 'Mã OTP đã gửi vào Email của bạn!' });

      // 👉 KÍCH HOẠT ĐẾM NGƯỢC 60 GIÂY
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer); // Hết giờ thì xóa bộ đếm
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

    } catch (err) {
      setMessage({ type: 'error', content: err.response?.data || 'Lỗi gửi OTP!' });
    } finally { setLoading(false); }
  };

  // 3. Xác nhận Đăng ký (Đã bổ sung bộ lọc bắt lỗi)
  const handleRegister = async (e) => {
    e.preventDefault();
    setMessage({ type: '', content: '' });

    // 👉 Chốt 1: Kiểm tra định dạng Email bằng Regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setMessage({ type: 'error', content: 'Email không đúng định dạng chuẩn!' });
      return;
    }

    // 👉 Chốt 2: Kiểm tra độ dài mật khẩu
    if (password.length < 6) {
      setMessage({ type: 'error', content: 'Mật khẩu phải có ít nhất 6 ký tự để bảo mật!' });
      return;
    }

    // 👉 Chốt 3: Ép buộc phải nhập mã OTP
    if (!isOtpSent || !otp) {
      setMessage({ type: 'error', content: 'Vui lòng bấm lấy mã OTP và nhập mã trước khi đăng ký!' });
      return;
    }

    try {
      await api.post(`/auth/register/verify?email=${email}&otp=${otp}&password=${password}`);
      alert("🎉 Đăng ký tài khoản thành công! Quay lại đăng nhập nhé ông.");
      setAuthMode('login'); resetForm();
    } catch (err) {
      setMessage({ type: 'error', content: err.response?.data || 'Mã OTP không đúng hoặc đã hết hạn!' });
    }
  };

  // 4. Xác nhận Đổi mật khẩu
  const handleResetPassword = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/auth/forgot-password/reset?email=${email}&otp=${otp}&newPassword=${password}`);
      alert("✅ Đổi mật khẩu thành công!");
      setAuthMode('login'); resetForm();
    } catch (err) {
      setMessage({ type: 'error', content: err.response?.data || 'Lỗi đổi mật khẩu!' });
    }
  };

  // --- GIAO DIỆN KHI CHƯA ĐĂNG NHẬP ---
  if (!token) {
    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f0f2f5', fontFamily: 'Arial' }}>
          <div style={{ padding: '30px', background: 'white', borderRadius: '12px', boxShadow: '0 8px 24px rgba(0,0,0,0.1)', width: '380px' }}>
            <h2 style={{ textAlign: 'center', color: '#1877f2', marginBottom: '25px', fontSize: '28px' }}>Smart Finance</h2>

            <form onSubmit={authMode === 'login' ? handleLogin : authMode === 'register' ? handleRegister : handleResetPassword}>
              <div style={{ marginBottom: '15px' }}>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required style={{ flex: 1, padding: '10px', borderRadius: '6px', border: '1px solid #ddd' }} />
                  {authMode !== 'login' && (
                      <button
                          type="button"
                          onClick={handleSendOtp}
                          disabled={loading || countdown > 0} // 👉 Khóa nút khi đang load hoặc đang đếm ngược
                          style={{
                            padding: '5px 10px',
                            backgroundColor: countdown > 0 ? '#e4e6eb' : '#e7f3ff', // Đổi màu xám khi bị khóa
                            color: countdown > 0 ? '#65676b' : '#1877f2',
                            border: 'none',
                            borderRadius: '6px',
                            cursor: countdown > 0 ? 'not-allowed' : 'pointer', // Đổi chuột thành hình cấm
                            fontSize: '12px',
                            fontWeight: 'bold',
                            minWidth: '90px'
                          }}
                      >
                        {/* 👉 HIỂN THỊ SỐ GIÂY ĐẾM NGƯỢC Y HỆT APP XỊN */}
                        {loading ? '...' : countdown > 0 ? `Thử lại sau (${countdown}s)` : isOtpSent ? 'Gửi lại mã' : 'Lấy OTP'}
                      </button>
                  )}
                </div>
              </div>

              {(authMode !== 'login' && isOtpSent) && (
                  <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Mã OTP (6 số):</label>
                    <input type="text" value={otp} onChange={(e) => setOtp(e.target.value)} required style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #ddd', boxSizing: 'border-box' }} />
                  </div>
              )}

              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  {authMode === 'forgot' ? 'Mật khẩu mới:' : 'Mật khẩu:'}
                </label>
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #ddd', boxSizing: 'border-box' }} />
              </div>

              <button type="submit" style={{ width: '100%', padding: '12px', backgroundColor: '#1877f2', color: 'white', border: 'none', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}>
                {authMode === 'login' ? 'Đăng nhập' : authMode === 'register' ? 'Xác nhận Đăng ký' : 'Cập nhật mật khẩu'}
              </button>
            </form>

            {message.content && <p style={{ color: message.type === 'error' ? '#dc3545' : '#28a745', marginTop: '15px', textAlign: 'center', fontSize: '14px', fontWeight: 'bold' }}>{message.content}</p>}

            <div style={{ marginTop: '20px', textAlign: 'center', borderTop: '1px solid #eee', paddingTop: '15px', fontSize: '14px' }}>
              {authMode === 'login' ? (
                  <>
                    <span onClick={() => { setAuthMode('register'); resetForm(); }} style={{ color: '#1877f2', cursor: 'pointer', fontWeight: 'bold' }}>Tạo tài khoản mới</span>
                    <br /><br />
                    <span onClick={() => { setAuthMode('forgot'); resetForm(); }} style={{ color: '#65676b', cursor: 'pointer' }}>Quên mật khẩu?</span>
                  </>
              ) : (
                  <span onClick={() => { setAuthMode('login'); resetForm(); }} style={{ color: '#1877f2', cursor: 'pointer', fontWeight: 'bold' }}>← Quay lại Đăng nhập</span>
              )}
            </div>
          </div>
        </div>
    );
  }

  // --- GIAO DIỆN KHI ĐÃ ĐĂNG NHẬP (GIỮ NGUYÊN) ---
  // --- GIAO DIỆN KHI ĐÃ ĐĂNG NHẬP ---
  return (
      <Router>
        <div style={{ fontFamily: 'Arial', backgroundColor: '#f0f2f5', minHeight: '100vh' }}>
          {/* 👉 Truyền handleLogout vào Sidebar để khi bấm nút Đăng xuất nó chạy hàm này */}
          <Sidebar onLogout={handleLogout} />
          <div style={{ marginLeft: '250px', padding: '30px', boxSizing: 'border-box' }}>
            <Routes>
              <Route path="/" element={<Navigate to="/dashboard" />} />
              <Route path="/dashboard" element={
                <ProtectedRoute>
                  {/* 👉 Thêm key={token} vào đây */}
                  <Dashboard key={token} />
                </ProtectedRoute>
              } />
              <Route path="/scan" element={
                <ProtectedRoute>
                  {/* 👉 Thêm key={token} vào đây */}
                  <ScanReceipt key={token} />
                </ProtectedRoute>
              } />
              <Route path="/history" element={
                <ProtectedRoute>
                  {/* 👉 Thêm key={token} vào đây */}
                  <TransactionHistory key={token} />
                </ProtectedRoute>
              } />
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </div>
        </div>
      </Router>
  );
}

export default App;