// src/api/axiosConfig.js
import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api', // Đường dẫn gốc của Spring Boot Backend
    headers: {
        'Content-Type': 'application/json',
    }
});

// Tự động đính kèm Token vào Header nếu có (Phục vụ cho các API sau như quét bill, chuyển tiền)
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

export default api;