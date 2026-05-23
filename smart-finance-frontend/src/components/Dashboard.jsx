// src/components/Dashboard.jsx
import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Pie } from 'react-chartjs-2';

// Đăng ký các thành phần bắt buộc của Chart.js phiên bản mới
ChartJS.register(ArcElement, Tooltip, Legend);

function Dashboard() {
    const [balance, setBalance] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [depositAmount, setDepositAmount] = useState('');
    const [depositLoading, setDepositLoading] = useState(false);

    // 👉 ĐƯA CÂU LỆNH NÀY LÊN ĐẦU COMPONENT CHO ĐÚNG CÚ PHÁP REACT
    const userFullName = localStorage.getItem("fullName") || "Người dùng";
    const walletId = localStorage.getItem('walletId') || 1;

    // Hàm đồng bộ toàn bộ dữ liệu (Ví và Giao dịch) từ SQL Server
    const fetchDashboardData = async () => {
        try {
            // Gọi đồng thời cả 2 API bằng Promise.all cực nhanh
            const [walletRes, transRes] = await Promise.all([
                api.get(`/wallets/${walletId}`),
                api.get(`/wallets/${walletId}/transactions`)
            ]);

            setBalance(walletRes.data.balance);
            setTransactions(transRes.data);
        } catch (err) {
            console.error("Lỗi đồng bộ dữ liệu Dashboard:", err);
            setError('Không thể đồng bộ dữ liệu từ server');
        } finally {
            setLoading(false);
        }
    };

    // 👉 CHỈ GIỮ LẠI DUY NHẤT 1 BỘ USEEFFECT NÀY
    useEffect(() => {
        fetchDashboardData();
    }, []);

    const handleDeposit = async (e) => {
        e.preventDefault();
        if (!depositAmount || isNaN(depositAmount) || Number(depositAmount) <= 0) {
            alert("Vui lòng nhập số tiền nạp hợp lệ!");
            return;
        }

        setDepositLoading(true);
        try {
            await api.put(`/wallets/${walletId}/deposit?amount=${depositAmount}`);
            setDepositAmount('');
            alert("🎉 Nạp tiền vào SQL Server thành công!");
            fetchDashboardData(); // Cập nhật lại số dư
        } catch (err) {
            alert("Lỗi khi nạp tiền vào ví!");
        } finally {
            setDepositLoading(false);
        }
    };

    // --- LOGIC XỬ LÝ DỮ LIỆU ĐỂ VẼ BIỂU ĐỒ TRÒN ---
    const expenseTransactions = transactions.filter(t => t.category?.type === 'EXPENSE');

    const categoryTotals = {};
    expenseTransactions.forEach(t => {
        const catName = t.category?.name || 'Chi tiêu khác';
        categoryTotals[catName] = (categoryTotals[catName] || 0) + t.amount;
    });

    const chartLabels = Object.keys(categoryTotals);
    const chartDataValues = Object.values(categoryTotals);

    let maxExpenseCategory = 'Chưa có chi tiêu';
    if (chartLabels.length > 0) {
        const maxIndex = chartDataValues.indexOf(Math.max(...chartDataValues));
        maxExpenseCategory = chartLabels[maxIndex];
    }

    const chartData = {
        labels: chartLabels,
        datasets: [
            {
                label: 'Tổng chi tiêu (VND)',
                data: chartDataValues,
                backgroundColor: [
                    '#1877f2', // Xanh dương
                    '#28a745', // Xanh lá
                    '#ffc107', // Vàng
                    '#dc3545', // Đỏ
                    '#6f42c1', // Tím
                    '#fd7e14'  // Cam
                ],
                borderWidth: 1,
            },
        ],
    };

    return (
        <div>
            <h2>📊 Trang Tổng Quan Dự Án (Dashboard)</h2>
            <div style={{ background: 'white', padding: '30px', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', marginTop: '20px' }}>
                {/* 👉 KHÔNG CÒN ĐOẠN KHAI BÁO BIẾN LỖI Ở ĐÂY NỮA */}
                <h3>Chào mừng {userFullName} quay trở lại! 👋</h3>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px', marginTop: '20px' }}>

                    {/* CỘT BÊN TRÁI: VÍ TIỀN MẶT & FORM NẠP TIỀN */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                        <div style={{ background: '#e7f3ff', padding: '20px', borderRadius: '8px' }}>
                            {/* 👉 ĐỔI ID HIỂN THỊ ĐỘNG CHO NGẦU */}
                            <h4 style={{ margin: 0, color: '#1877f2' }}>💳 Ví Tiền Mặt (ID: #{walletId})</h4>
                            <p style={{ fontSize: '28px', fontWeight: 'bold', margin: '10px 0 0 0', color: '#1c1e21' }}>
                                {loading ? '🔄 Đang đồng bộ...' : error ? <span style={{color: 'red', fontSize: '16px'}}>{error}</span> : `${balance?.toLocaleString()} VND`}
                            </p>
                        </div>

                        <div style={{ background: '#f8f9fa', padding: '20px', borderRadius: '8px' }}>
                            <h4 style={{ margin: 0, color: '#65676b' }}>📈 Hạng mục chi nhiều nhất</h4>
                            <p style={{ fontSize: '28px', fontWeight: 'bold', margin: '10px 0 0 0', color: '#dc3545' }}>
                                {loading ? '🔄...' : maxExpenseCategory}
                            </p>
                        </div>

                        {!loading && !error && (
                            <form onSubmit={handleDeposit} style={{ background: '#fff', border: '1px solid #e4e6eb', padding: '20px', borderRadius: '8px' }}>
                                <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: 'bold', color: '#555' }}>💰 Nạp thêm tiền vào ví này:</label>
                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <input
                                        type="number"
                                        placeholder="Nhập số tiền nạp thủ công..."
                                        value={depositAmount}
                                        onChange={(e) => setDepositAmount(e.target.value)}
                                        style={{ flex: 1, padding: '10px', borderRadius: '4px', border: '1px solid #ccd0d5', outline: 'none' }}
                                    />
                                    <button type="submit" disabled={depositLoading} style={{ padding: '10px 20px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                                        {depositLoading ? 'Đang xử lý...' : '➕ Nạp'}
                                    </button>
                                </div>
                            </form>
                        )}
                    </div>

                    {/* CỘT BÊN PHẢI: BIỂU ĐỒ HÌNH TRÒN DỮ LIỆU THẬT */}
                    <div style={{ background: '#ffffff', border: '1px solid #e4e6eb', padding: '20px', borderRadius: '8px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '350px' }}>
                        <h4 style={{ margin: '0 0 15px 0', color: '#1c1e21' }}>📊 Tỷ lệ phân bổ chi tiêu thực tế</h4>
                        {loading ? (
                            <p>🔄 Đang tính toán dữ liệu biểu đồ...</p>
                        ) : expenseTransactions.length === 0 ? (
                            <p style={{ color: '#65676b', fontStyle: 'italic' }}>Chưa có dữ liệu chi tiêu nào để vẽ biểu đồ.</p>
                        ) : (
                            <div style={{ width: '100%', maxWidth: '280px' }}>
                                <Pie data={chartData} />
                            </div>
                        )}
                    </div>

                </div>
            </div>
        </div>
    );
}

export default Dashboard;