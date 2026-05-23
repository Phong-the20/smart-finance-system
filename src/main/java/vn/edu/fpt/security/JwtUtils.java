package vn.edu.fpt.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // Lấy khóa bí mật ra
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Hàm 1: Tạo Token (In thẻ từ) dựa vào email của user
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email) // Lưu email vào token
                .setIssuedAt(new Date()) // Thời gian phát hành
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Thời gian hết hạn
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Ký tên bằng thuật toán HS256
                .compact();
    }

    // Hàm 2: Lấy email từ Token (Đọc thẻ từ xem của ai)
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Hàm 3: Kiểm tra Token có hợp lệ không (Có bị làm giả, hay hết hạn không?)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
        }
        return false;
    }
}