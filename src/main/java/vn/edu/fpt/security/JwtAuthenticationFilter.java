package vn.edu.fpt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. Lấy JWT từ request
            String jwt = parseJwt(request);

            // 2. Nếu có JWT và nó hợp lệ
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                // 3. Lấy email từ token
                String email = jwtUtils.getEmailFromToken(jwt);

                // 4. Tạo giấy thông hành và cấp quyền đi tiếp
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            System.out.println("Không thể xác thực người dùng: " + e.getMessage());
        }

        // Cho request đi tiếp tới các bộ lọc khác
        filterChain.doFilter(request, response);
    }

    // Hàm phụ: Lọc lấy chữ cái mã Token từ Header
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        // Token chuẩn thường có chữ "Bearer " đứng trước
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}