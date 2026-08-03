package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.UserRequest;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponse<UserResponse> getAllUsers(String keyword, Pageable pageable);

    UserResponse getUserById(Integer userId);

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Integer userId, UserRequest request);

    void resetStaffPassword(Integer userId, String citizenId, String newPassword);

    /** Khóa / mở khóa tài khoản (status 0/1). */
    UserResponse changeUserStatus(Integer userId, Byte status);

    void deleteUser(Integer userId);
}
