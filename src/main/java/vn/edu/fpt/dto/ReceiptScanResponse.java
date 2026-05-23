package vn.edu.fpt.dto;

import java.math.BigDecimal; // Nhớ import thư viện này nhé

public class ReceiptScanResponse {
    private BigDecimal amount; // Đổi từ Double sang BigDecimal
    private String description;
    private String categorySuggestion;

    // Sửa lại Getter và Setter cho chuẩn kiểu dữ liệu mới
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorySuggestion() { return categorySuggestion; }
    public void setCategorySuggestion(String categorySuggestion) { this.categorySuggestion = categorySuggestion; }
}