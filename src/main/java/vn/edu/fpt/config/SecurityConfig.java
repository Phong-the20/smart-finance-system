package vn.edu.fpt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.edu.fpt.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(java.util.List.of("http://localhost:5173")); // Cho phép React gọi sang
                    corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
                .csrf(csrf -> csrf.disable())

                // 2. Không lưu trạng thái Session (Stateless) vì Token đã tự mang thông tin rồi
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Phân quyền các đường dẫn API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api/ai/**").permitAll()
                        .anyRequest().authenticated() // Phòng VIP: Tất cả các API còn lại (chuyển tiền, xem ví, AI...) đều phải có Token
                );

        // 4. Bố trí "Bác bảo vệ" đứng kiểm tra trước khi vào hệ thống
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}