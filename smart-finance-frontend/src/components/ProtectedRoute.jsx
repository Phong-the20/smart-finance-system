// src/components/ProtectedRoute.jsx
import React from 'react';
import { Navigate } from 'react-router-dom';

function ProtectedRoute({ children }) {
    const token = localStorage.getItem('token');

    // Nếu chưa có token (chưa đăng nhập), lập tức đá về trang chủ (form login)
    if (!token) {
        return <Navigate to="/" replace />;
    }

    // Nếu đã có token, cho phép đi tiếp vào giao diện bên trong
    return children;
}

export default ProtectedRoute;