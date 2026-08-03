package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.UserAddressRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.UserAddressResponse;
import com.foxstyle.api.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAddressResponse>>> getMyAddresses(Principal principal) {
        List<UserAddressResponse> addresses = addressService.getMyAddresses(principal.getName());
        ApiResponse<List<UserAddressResponse>> response = ApiResponse.<List<UserAddressResponse>>builder()
                .status("success")
                .message("Lấy sổ địa chỉ thành công")
                .data(addresses)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAddressResponse>> getAddressById(
            Principal principal,
            @PathVariable Integer id) {
        UserAddressResponse address = addressService.getAddressById(principal.getName(), id);
        ApiResponse<UserAddressResponse> response = ApiResponse.<UserAddressResponse>builder()
                .status("success")
                .message("Lấy thông tin địa chỉ thành công")
                .data(address)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAddressResponse>> createAddress(
            Principal principal,
            @Valid @RequestBody UserAddressRequest request) {
        UserAddressResponse saved = addressService.createAddress(principal.getName(), request);
        ApiResponse<UserAddressResponse> response = ApiResponse.<UserAddressResponse>builder()
                .status("success")
                .message("Thêm địa chỉ mới thành công")
                .data(saved)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAddressResponse>> updateAddress(
            Principal principal,
            @PathVariable Integer id,
            @Valid @RequestBody UserAddressRequest request) {
        UserAddressResponse updated = addressService.updateAddress(principal.getName(), id, request);
        ApiResponse<UserAddressResponse> response = ApiResponse.<UserAddressResponse>builder()
                .status("success")
                .message("Cập nhật địa chỉ thành công")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            Principal principal,
            @PathVariable Integer id) {
        addressService.deleteAddress(principal.getName(), id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa địa chỉ thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
