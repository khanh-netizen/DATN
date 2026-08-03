package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.LoginRequest;
import com.foxstyle.api.dto.request.RegisterRequest;
import com.foxstyle.api.dto.response.AuthResponse;
import com.foxstyle.api.dto.response.UserResponse;
import com.foxstyle.api.entity.Role;
import com.foxstyle.api.entity.User;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.RoleRepository;
import com.foxstyle.api.repository.UserRepository;
import com.foxstyle.api.security.JwtUtil;
import com.foxstyle.api.service.AuthService;
import com.foxstyle.api.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_CUSTOMER_ROLE = "ROLE_CUSTOMER";
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final com.foxstyle.api.service.MailService mailService;

    @Override
    public AuthResponse login(LoginRequest request) {
        // Ném BadCredentialsException/DisabledException nếu sai — GlobalExceptionHandler xử lý
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername()).orElse(null));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            if (user == null) {
                throw ex;
            }

            int failedAttempts = user.getFailedLoginAttempts() == null
                    ? 1
                    : user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setStatus((byte) 0);
                userRepository.save(user);
                throw new DisabledException("Tài khoản đã bị khóa sau 5 lần đăng nhập sai");
            }

            userRepository.save(user);
            throw ex;
        }

        if (user == null) {
            throw new BadCredentialsException("Sai tài khoản hoặc mật khẩu");
        }

        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getRoleName());

        return AuthResponse.builder()
                .accessToken(token)
                .user(convertToUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateNewAccount(request.getUsername(), request.getEmail());

        // Xác thực OTP qua Email
        otpService.verifyOtp(request.getEmail(), request.getOtp());

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(getCustomerRole())
                .status((byte) 1)
                .build();

        User savedUser = userRepository.save(newUser);

        // Gửi email chào mừng thành viên mới kèm mã giảm giá tới email đã đăng ký
        try {
            mailService.sendDiscountCouponEmail(savedUser.getEmail(), "FOXSTYLE50");
        } catch (Exception e) {
            System.err.println("[REGISTER MAIL ERROR] Không thể gửi email chào mừng: " + e.getMessage());
        }

        return convertToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse registerStaff(RegisterRequest request) {
        validateNewAccount(request.getUsername(), request.getEmail());

        // Xác thực OTP qua Email
        otpService.verifyOtp(request.getEmail(), request.getOtp());

        Role staffRole = roleRepository.findByRoleName("ROLE_STAFF")
                .orElseThrow(() -> new ResourceNotFoundException("Chưa khởi tạo vai trò ROLE_STAFF"));

        User newStaff = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(staffRole)
                .status((byte) 1) // Approved / Active
                .build();

        return convertToUserResponse(userRepository.save(newStaff));
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        return convertToUserResponse(findUserByUsername(username));
    }

    @Override
    @Transactional
    public void deactivateAccount(String username) {
        User user = findUserByUsername(username);
        if ("admin_fox".equals(username)) {
            throw new BadRequestException("Không thể khóa tài khoản quản trị hệ thống!");
        }
        user.setStatus((byte) 0);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteMyAccount(String username) {
        User user = findUserByUsername(username);
        if ("admin_fox".equals(username)) {
            throw new BadRequestException("Không thể xóa tài khoản quản trị hệ thống!");
        }
        // Soft delete & Anonymization to preserve database FK constraints and order ledgers
        user.setStatus((byte) 0);
        user.setFullName("Tài khoản đã xóa");
        user.setEmail("deleted_" + user.getUserId() + "_" + System.currentTimeMillis() + "@foxstyle.vn");
        user.setPhone("");
        user.setPassword(""); // Clear password hash to disable login forever
        userRepository.save(user);
    }

    // ==================== Private helpers ====================

    private User findUserByUsername(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + usernameOrEmail)));
    }

    private void validateNewAccount(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email này đã được sử dụng!");
        }
    }

    private Role getCustomerRole() {
        return roleRepository.findByRoleName(DEFAULT_CUSTOMER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa khởi tạo quyền " + DEFAULT_CUSTOMER_ROLE));
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roleName(user.getRole().getRoleName())
                .theme(user.getTheme())
                .language(user.getLanguage())
                .build();
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public AuthResponse googleLogin(com.foxstyle.api.dto.request.GoogleLoginRequest request) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
            
            java.util.Map<String, Object> payload = restTemplate.getForObject(url, java.util.Map.class);
            if (payload == null || !payload.containsKey("email")) {
                throw new BadRequestException("Google ID Token không hợp lệ hoặc đã hết hạn!");
            }
            
            String email = (String) payload.get("email");
            String fullName = (String) payload.get("name");
            String sub = (String) payload.get("sub");
            
            java.util.Optional<User> userOpt = userRepository.findByEmail(email);
            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
            } else {
                String baseUsername = email.split("@")[0];
                String username = baseUsername;
                int suffix = 1;
                while (userRepository.findByUsername(username).isPresent()) {
                    username = baseUsername + suffix;
                    suffix++;
                }
                
                Role customerRole = getCustomerRole();
                user = User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(sub))
                        .fullName(fullName != null ? fullName : "Google User")
                        .email(email)
                        .phone("")
                        .role(customerRole)
                        .status((byte) 1)
                        .build();
                user = userRepository.save(user);
            }
            
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getRoleName());
            return AuthResponse.builder()
                    .accessToken(token)
                    .user(convertToUserResponse(user))
                    .build();
        } catch (Exception e) {
            throw new BadRequestException("Xác thực tài khoản Google thất bại: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resetPassword(com.foxstyle.api.dto.request.ResetPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email không được để trống!");
        }
        if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
            throw new BadRequestException("Mã OTP không được để trống!");
        }
        if (!com.foxstyle.api.util.PasswordPolicy.isValid(request.getNewPassword())) {
            throw new BadRequestException(com.foxstyle.api.util.PasswordPolicy.MESSAGE);
        }

        String email = request.getEmail().trim();
        // 1. Xác thực OTP
        otpService.verifyOtp(email, request.getOtp().trim());

        // 2. Tìm người dùng theo Email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản sở hữu email: " + email));

        // 3. Đổi mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    public com.foxstyle.api.dto.response.AccountSearchResponse findAccountForPasswordReset(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng nhập Email, Số điện thoại hoặc Tên tài khoản để tìm kiếm!");
        }
        String cleanKw = keyword.trim();

        User user = userRepository.findByEmail(cleanKw)
                .orElseGet(() -> userRepository.findByUsername(cleanKw)
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> (u.getPhone() != null && u.getPhone().trim().equalsIgnoreCase(cleanKw))
                                || (u.getEmail() != null && u.getEmail().equalsIgnoreCase(cleanKw))
                                || (u.getUsername() != null && u.getUsername().equalsIgnoreCase(cleanKw)))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản tương ứng với thông tin '" + keyword + "' trên hệ thống!"))
                ));

        return com.foxstyle.api.dto.response.AccountSearchResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .maskedEmail(maskEmail(user.getEmail()))
                .fullName(user.getFullName())
                .maskedPhone(maskPhone(user.getPhone()))
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email != null ? email : "";
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name.substring(0, 1) + "***@" + domain;
        }
        return name.substring(0, 2) + "***" + name.substring(name.length() - 1) + "@" + domain;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.trim().length() < 6) return phone != null && !phone.trim().isEmpty() ? phone : "Chưa cập nhật";
        String clean = phone.trim();
        int len = clean.length();
        return clean.substring(0, 2) + "****" + clean.substring(len - 3);
    }
}

