// src/components/Sidebar.jsx
import React from 'react';
import { NavLink } from 'react-router-dom';

// 👉 Nhận vào props (hoặc dùng destructing { onLogout }) để lấy hàm từ App.jsx xuống
function Sidebar({ onLogout }) {

    // Hàm dùng chung để tạo style cho Menu, tự động đổi màu khi trang đó đang hiển thị
    const getMenuStyle = ({ isActive }) => ({
        color: 'white',
        textDecoration: 'none',
        fontSize: '18px',
        fontWeight: 'bold',
        display: 'block',
        padding: '10px',
        borderRadius: '4px',
        backgroundColor: isActive ? '#1877f2' : 'transparent',
        transition: 'background-color 0.2s ease'
    });

    return (
        <div style={{ width: '250px', background: '#1c1e21', color: 'white', height: '100vh', position: 'fixed', top: 0, left: 0, padding: '20px', boxSizing: 'border-box', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', zIndex: 1000 }}>
            <div>
                <h2 style={{ color: '#1877f2', textAlign: 'center', marginBottom: '30px', marginTop: 0 }}>Smart Finance 🤖</h2>
                <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                    <li style={{ marginBottom: '15px' }}>
                        <NavLink to="/dashboard" style={getMenuStyle}>
                            📊 Tổng quan (Dashboard)
                        </NavLink>
                    </li>
                    <li style={{ marginBottom: '15px' }}>
                        <NavLink to="/scan" style={getMenuStyle}>
                            📸 Mắt thần AI (Quét Bill)
                        </NavLink>
                    </li>
                    <li style={{ marginBottom: '15px' }}>
                        <NavLink to="/history" style={getMenuStyle}>
                            📜 Lịch sử chi tiêu
                        </NavLink>
                    </li>
                </ul>
            </div>

            {/* 👉 Thay vì tự xử lý, nút này sẽ gọi trực tiếp hàm onLogout được truyền từ App.jsx */}
            <button onClick={onLogout} style={{ width: '100%', padding: '10px', backgroundColor: '#3a3b3c', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                Đăng xuất 🚪
            </button>
        </div>
    );
}

export default Sidebar;