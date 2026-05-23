// src/components/ScanReceipt.jsx
import React, { useState } from 'react';
import api from '../api/axiosConfig';

function ScanReceipt() {
    // 👉 1. Lấy ID Ví của tài khoản hiện tại từ LocalStorage
    const walletId = localStorage.getItem('walletId') || 1;

    // State cho Quét hóa đơn AI
    const [selectedFile, setSelectedFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [scanLoading, setScanLoading] = useState(false);

    // State chung cho kết quả hiển thị
    const [scanResult, setScanResult] = useState(null);
    const [scanError, setScanError] = useState('');

    // State cho Form Nhập tay thủ công
    const [manualAmount, setManualAmount] = useState('');
    const [manualDesc, setManualDesc] = useState('');
    const [manualCategory, setManualCategory] = useState('3');
    const [manualLoading, setManualLoading] = useState(false);

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setSelectedFile(file);
            setPreviewUrl(URL.createObjectURL(file));
            setScanResult(null);
            setScanError('');
        }
    };

    // Luồng 1: Xử lý quét Bill bằng AI
    const handleScanReceipt = async () => {
        setScanLoading(true);
        setScanError('');
        setScanResult(null);

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            // 👉 2. Nhúng biến walletId vào API Quét AI
            const response = await api.post(`/ai/scan-receipt/${walletId}`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setScanResult(response.data);
        } catch (err) {
            setScanError('Lỗi khi quét hóa đơn: ' + (err.response?.data?.message || err.message));
        } finally {
            setScanLoading(false);
        }
    };

    // Luồng 2: Xử lý tự nhập tay thủ công
    const handleManualSubmit = async (e) => {
        e.preventDefault();
        if (!manualAmount || Number(manualAmount) <= 0 || !manualDesc) {
            alert("Vui lòng điền đầy đủ số tiền và nội dung chi tiêu!");
            return;
        }

        setManualLoading(true);
        setScanError('');
        setScanResult(null);

        try {
            // 👉 3. Nhúng biến walletId vào API Nhập tay
            const response = await api.post(`/wallets/${walletId}/add-transaction?amount=${manualAmount}&description=${manualDesc}&categoryId=${manualCategory}`);

            setScanResult(response.data);
            setManualAmount('');
            setManualDesc('');
            alert("🎉 Ghi nhận chi tiêu bằng tay thành công!");
        } catch (err) {
            setScanError('Lỗi khi lưu giao dịch: ' + (err.response?.data?.message || err.message));
        } finally {
            setManualLoading(false);
        }
    };

    return (
        <div>
            {/* 👉 4. Hiển thị ID Ví động lên tiêu đề cho ngầu */}
            <h2>📸 Quản Lý Chi Tiêu Hệ Thống (Ví #{walletId})</h2>
            <p style={{ color: '#65676b', marginTop: 0 }}>Ông có thể chọn Quét hóa đơn tự động bằng Gemini AI hoặc Tự nhập bằng tay bên dưới.</p>

            <div style={{ display: 'grid', gridTemplateColumns: '1.1fr 0.9fr', gap: '30px', marginTop: '20px' }}>

                {/* CỘT BÊN TRÁI: CHỨA CẢ QUÉT AI VÀ FORM NHẬP TAY */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

                    {/* KHỐI 1: QUÉT HÓA ĐƠN AI */}
                    <div style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
                        <h4 style={{ margin: '0 0 10px 0', color: '#1877f2' }}>🤖 Cách 1: Quét hóa đơn tự động bằng AI</h4>
                        <div style={{ border: '2px dashed #1877f2', padding: '20px', textAlign: 'center', borderRadius: '8px', cursor: 'pointer', backgroundColor: '#f0f7ff', marginBottom: '15px' }}>
                            <input type="file" accept="image/*" onChange={handleFileChange} id="fileInput" style={{ display: 'none' }} />
                            <label htmlFor="fileInput" style={{ cursor: 'pointer', color: '#1877f2', fontWeight: 'bold' }}>
                                {selectedFile ? '✏️ Thay đổi ảnh hóa đơn' : '📸 Bấm vào đây để chọn ảnh hóa đơn'}
                            </label>
                        </div>
                        {previewUrl && (
                            <button onClick={handleScanReceipt} disabled={scanLoading} style={{ width: '100%', padding: '12px', backgroundColor: scanLoading ? '#92bcff' : '#28a745', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer', fontSize: '15px' }}>
                                {scanLoading ? '⏳ Đang gửi ảnh sang cho Gemini AI bóc tách...' : '🚀 Bắt đầu quét hóa đơn'}
                            </button>
                        )}
                    </div>

                    {/* KHỐI 2: FORM TỰ NHẬP BẰNG TAY */}
                    <div style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
                        <h4 style={{ margin: '0 0 15px 0', color: '#28a745' }}>✍️ Cách 2: Tự ghi nhận chi tiêu bằng tay</h4>
                        <form onSubmit={handleManualSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            <div>
                                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>Số tiền (VND):</label>
                                <input type="number" placeholder="Ví dụ: 20000" value={manualAmount} onChange={(e) => setManualAmount(e.target.value)} style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccd0d5', boxSizing: 'border-box' }} />
                            </div>
                            <div>
                                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>Nội dung chi tiêu:</label>
                                <input type="text" placeholder="Ví dụ: Gửi xe, Mua trà đá..." value={manualDesc} onChange={(e) => setManualDesc(e.target.value)} style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccd0d5', boxSizing: 'border-box' }} />
                            </div>
                            <div>
                                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>Danh mục phân loại:</label>
                                <select
                                    value={manualCategory}
                                    onChange={(e) => setManualCategory(e.target.value)}
                                    style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccd0d5', backgroundColor: 'white' }}
                                >
                                    <option value="3">🍔 Ăn uống (FOOD)</option>
                                    <option value="4">🏠 Tiền nhà (HOUSE)</option>
                                    <option value="5">🚌 Di chuyển (TRANSPORT)</option>
                                </select>
                            </div>
                            <button type="submit" disabled={manualLoading} style={{ padding: '12px', backgroundColor: '#1877f2', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer', fontSize: '15px', marginTop: '5px' }}>
                                {manualLoading ? '⏳ Đang lưu...' : '💾 Ghi nhận giao dịch'}
                            </button>
                        </form>
                        {scanError && <p style={{ color: 'red', marginTop: '15px', fontWeight: 'bold' }}>{scanError}</p>}
                    </div>

                </div>

                {/* CỘT BÊN PHẢI: HIỂN THỊ KẾT QUẢ TRỰC QUAN (DÙNG CHUNG CHO CẢ 2 CÁCH) */}
                <div style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '400px' }}>
                    {previewUrl && !scanResult && <img src={previewUrl} alt="Preview" style={{ maxWidth: '100%', maxHeight: '250px', borderRadius: '4px', marginBottom: '20px' }} />}

                    {scanResult ? (
                        <div style={{ width: '100%', background: '#d4edda', color: '#155724', padding: '20px', borderRadius: '6px', boxSizing: 'border-box' }}>
                            <h4 style={{ marginTop: 0 }}>✅ Tự động cập nhật thành công vào SQL Server!</h4>
                            <hr style={{ border: '0.5px solid #c3e6cb' }} />
                            <p><b>Mã giao dịch:</b> #{scanResult.transactionId}</p>
                            <p><b>Nội dung:</b> {scanResult.description}</p>
                            <p><b>Số tiền trừ:</b> <span style={{ color: 'red', fontWeight: 'bold' }}>-{scanResult.amount?.toLocaleString()} VND</span></p>
                            <p><b>Danh mục:</b> {scanResult.category?.name || 'Chi tiêu'}</p>
                            <p><b>Số dư ví mới còn:</b> <span style={{ color: '#155724', fontWeight: 'bold' }}>{scanResult.wallet?.balance?.toLocaleString()} VND</span></p>
                        </div>
                    ) : (
                        <div style={{ textAlign: 'center', color: '#65676b' }}>
                            <p style={{ fontSize: '50px', margin: 0 }}>📊</p>
                            <p>Hóa đơn bóc tách hoặc Giao dịch nhập tay của ông sẽ xuất hiện tại đây.</p>
                        </div>
                    )}
                </div>

            </div>
        </div>
    );
}

export default ScanReceipt;