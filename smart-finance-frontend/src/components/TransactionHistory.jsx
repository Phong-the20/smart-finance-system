// src/components/TransactionHistory.jsx
import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

function TransactionHistory() {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // 👉 Lấy ID ví từ LocalStorage để hiển thị lên tiêu đề
    const walletId = localStorage.getItem('walletId') || 1;

    useEffect(() => {
        const fetchTransactions = async () => {
            try {
                // 👉 Gỡ bỏ số 1 cứng ngắc, thay bằng biến walletId
                const response = await api.get(`/wallets/${walletId}/transactions`);
                setTransactions(response.data);
            } catch (err) {
                console.error("Lỗi lấy lịch sử giao dịch:", err);
                setError('Không thể tải lịch sử giao dịch!');
            } finally {
                setLoading(false);
            }
        };
        fetchTransactions();
    }, [walletId]); // Thêm walletId vào dependency array cho chuẩn React

    return (
        <div>
            {/* 👉 Cập nhật tiêu đề hiển thị đúng ID ví đang xem */}
            <h2>📜 Lịch Sử Giao Dịch (Ví #{walletId})</h2>
            <p style={{ color: '#65676b', marginTop: 0 }}>Hiển thị danh sách tất cả các khoản thu/chi thực tế từ SQL Server.</p>

            {loading ? (
                <p>🔄 Đang tải lịch sử giao dịch...</p>
            ) : error ? (
                <p style={{ color: 'red' }}>{error}</p>
            ) : transactions.length === 0 ? (
                <p style={{ fontStyle: 'italic', color: '#65676b' }}>Chưa có giao dịch nào được ghi nhận.</p>
            ) : (
                <div style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', marginTop: '20px' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead>
                        <tr style={{ borderBottom: '2px solid #e4e6eb', color: '#65676b' }}>
                            <th style={{ padding: '12px' }}>Mã GD</th>
                            <th style={{ padding: '12px' }}>Thời gian</th>
                            <th style={{ padding: '12px' }}>Nội dung</th>
                            <th style={{ padding: '12px' }}>Danh mục</th>
                            <th style={{ padding: '12px', textAlign: 'right' }}>Số tiền</th>
                        </tr>
                        </thead>
                        <tbody>
                        {transactions.map((t) => {
                            const isExpense = t.category?.type === 'EXPENSE';
                            return (
                                <tr key={t.transactionId} style={{ borderBottom: '1px solid #f0f2f5', hover: { backgroundColor: '#f8f9fa' } }}>
                                    <td style={{ padding: '12px', fontWeight: 'bold', color: '#65676b' }}>#{t.transactionId}</td>
                                    <td style={{ padding: '12px', fontSize: '14px', color: '#8a8d91' }}>
                                        {new Date(t.transactionDate).toLocaleString('vi-VN')}
                                    </td>
                                    <td style={{ padding: '12px', fontWeight: '500' }}>{t.description}</td>
                                    <td style={{ padding: '12px' }}>
                                            <span style={{ padding: '4px 8px', borderRadius: '4px', fontSize: '12px', fontWeight: 'bold', backgroundColor: isExpense ? '#fff0f0' : '#e6fcf5', color: isExpense ? '#dc3545' : '#28a745' }}>
                                                {t.category?.name || 'Chi tiêu'}
                                            </span>
                                    </td>
                                    <td style={{ padding: '12px', textAlign: 'right', fontWeight: 'bold', color: isExpense ? '#dc3545' : '#28a745' }}>
                                        {isExpense ? '-' : '+'}{t.amount?.toLocaleString()} VND
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default TransactionHistory;