package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.UserRequest;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.UserResponse;
import com.foxstyle.api.entity.Role;
import com.foxstyle.api.entity.User;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.RoleRepository;
import com.foxstyle.api.repository.UserRepository;
import com.foxstyle.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<UserResponse> getAllUsers(String keyword, Pageable pageable) {
        Page<User> page = StringUtils.hasText(keyword)
                ? userRepository.findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(keyword, keyword, pageable)
                : userRepository.findAll(pageable);
        return PageResponse.of(page.map(this::convertToResponse));
    }

    @Override
    public UserResponse getUserById(Integer userId) {
        return convertToResponse(findUserById(userId));
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        validateUniqueUsernameAndEmail(request, null);
        if (StringUtils.hasText(request.getCitizenId()) && userRepository.existsByCitizenId(request.getCitizenId())) {
            throw new BadRequestException("Căn cước công dân đã được sử dụng");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("Mật khẩu không được để trống khi tạo tài khoản mới");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .citizenId(request.getCitizenId())
                .address(request.getAddress())
                .role(findRoleById(request.getRoleId()))
                .status(request.getStatus() != null ? request.getStatus() : (byte) 1)
                .build();

        return convertToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Integer userId, UserRequest request) {
        User user = findUserById(userId);
        validateUniqueUsernameAndEmail(request, user);

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCitizenId(request.getCitizenId());
        user.setAddress(request.getAddress());
        user.setRole(findRoleById(request.getRoleId()));
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
            if (request.getStatus() == 1) {
                user.setFailedLoginAttempts(0);
            }
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return convertToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void resetStaffPassword(Integer userId, String citizenId, String newPassword) {
        User user = findUserById(userId);
        if (!user.getRole().getRoleName().toUpperCase().endsWith("STAFF")) {
            throw new BadRequestException("Chỉ được reset mật khẩu cho tài khoản nhân viên");
        }
        if (!StringUtils.hasText(user.getCitizenId()) || !user.getCitizenId().equals(citizenId)) {
            throw new BadRequestException("Căn cước công dân không khớp với hồ sơ nhân viên");
        }
        if (!com.foxstyle.api.util.PasswordPolicy.isValid(newPassword)) {
            throw new BadRequestException(com.foxstyle.api.util.PasswordPolicy.MESSAGE);
        }
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse changeUserStatus(Integer userId, Byte status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BadRequestException("Trạng thái tài khoản chỉ nhận giá trị 0 (khóa) hoặc 1 (hoạt động)");
        }
        User user = findUserById(userId);
        user.setStatus(status);
        if (status == 1) {
            user.setFailedLoginAttempts(0);
        }
        return convertToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        userRepository.delete(findUserById(userId));
    }

    // ==================== Private helpers ====================

    private User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng có ID: " + userId));
    }

    private Role findRoleById(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quyền có ID: " + roleId));
    }

    private void validateUniqueUsernameAndEmail(UserRequest request, User existing) {
        boolean usernameChanged = existing == null || !existing.getUsername().equals(request.getUsername());
        if (usernameChanged && userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại!");
        }
        boolean emailChanged = existing == null || !existing.getEmail().equals(request.getEmail());
        if (emailChanged && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email này đã được sử dụng!");
        }
        if (existing != null && !existing.getUsername().equals(request.getUsername())) {
            throw new BadRequestException("Không được phép thay đổi tên đăng nhập");
        }
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .status(user.getStatus())
                .roleName(user.getRole().getRoleName())
                .build();
    }
}
