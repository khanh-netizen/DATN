package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.SettingRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.SettingResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.service.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<SettingResponse>>> getAllSettings(Pageable pageable) {
        PageResponse<SettingResponse> settings = settingService.getAllSettings(pageable);
        ApiResponse<PageResponse<SettingResponse>> response = ApiResponse.<PageResponse<SettingResponse>>builder()
                .status("success")
                .message("Lấy toàn bộ cấu hình thành công")
                .data(settings)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SettingResponse>> getSettingById(@PathVariable Integer id) {
        SettingResponse setting = settingService.getSettingById(id);
        ApiResponse<SettingResponse> response = ApiResponse.<SettingResponse>builder()
                .status("success")
                .message("Lấy thông tin cấu hình theo ID thành công")
                .data(setting)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/key/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SettingResponse>> getSettingByKey(@PathVariable String key) {
        SettingResponse setting = settingService.getSettingByKey(key);
        ApiResponse<SettingResponse> response = ApiResponse.<SettingResponse>builder()
                .status("success")
                .message("Lấy cấu hình theo key thành công")
                .data(setting)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SettingResponse>> createSetting(@Valid @RequestBody SettingRequest request) {
        SettingResponse saved = settingService.createSetting(request);
        ApiResponse<SettingResponse> response = ApiResponse.<SettingResponse>builder()
                .status("success")
                .message("Tạo cấu hình mới thành công")
                .data(saved)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettingResponse>> updateSetting(
            @PathVariable Integer id,
            @Valid @RequestBody SettingRequest request) {
        SettingResponse updated = settingService.updateSetting(id, request);
        ApiResponse<SettingResponse> response = ApiResponse.<SettingResponse>builder()
                .status("success")
                .message("Cập nhật cấu hình theo ID thành công")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/key/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SettingResponse>> updateSettingByKey(
            @PathVariable String key,
            @Valid @RequestBody SettingRequest request) {
        SettingResponse updated = settingService.updateSettingByKey(key, request);
        ApiResponse<SettingResponse> response = ApiResponse.<SettingResponse>builder()
                .status("success")
                .message("Cập nhật cấu hình theo key thành công")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(@PathVariable Integer id) {
        settingService.deleteSetting(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa cấu hình thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
