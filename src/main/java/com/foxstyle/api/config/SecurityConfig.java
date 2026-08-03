package com.foxstyle.api.config;

import com.foxstyle.api.security.JwtAuthenticationEntryPoint;
import com.foxstyle.api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Bật @PreAuthorize trên Controller/Service
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {}) // Dùng cấu hình CORS của WebMvcConfig
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // Swagger & OpenAPI endpoints công khai
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger/**"
                ).permitAll()
                // API công khai: đăng nhập/đăng ký, xem sản phẩm, danh mục, banner, đánh giá
                .requestMatchers("/api/v1/auth/repair-db").hasRole("ADMIN")
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/payments/payos/webhook").permitAll()
                .requestMatchers("/api/v1/coupons/subscribe-newsletter").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/site-settings").permitAll()
                .requestMatchers(HttpMethod.GET, "/media/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/chats").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/products/**",
                        "/api/v1/categories/**",
                        "/api/v1/banners/**",
                        "/api/v1/articles/**",
                        "/api/v1/reviews/**",
                        "/api/v1/coupons/validate",
                        "/api/v1/language/**").permitAll()
                // Còn lại yêu cầu đăng nhập, phân quyền chi tiết bằng @PreAuthorize
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
