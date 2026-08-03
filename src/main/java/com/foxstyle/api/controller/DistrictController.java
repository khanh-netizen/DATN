package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.DistrictRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.DistrictResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.service.DistrictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DistrictResponse>>> getActiveDistricts(Pageable pageable) {
        PageResponse<DistrictResponse> districts = districtService.getAllDistricts(true, pageable);
        ApiResponse<PageResponse<DistrictResponse>> response = ApiResponse.<PageResponse<DistrictResponse>>builder()
                .status("success")
                .message("Lấy danh sách quận/huyện hoạt động thành công")
                .data(districts)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<DistrictResponse>>> getAllDistricts(Pageable pageable) {
        PageResponse<DistrictResponse> districts = districtService.getAllDistricts(false, pageable);
        ApiResponse<PageResponse<DistrictResponse>> response = ApiResponse.<PageResponse<DistrictResponse>>builder()
                .status("success")
                .message("Lấy tất cả danh sách quận/huyện thành công")
                .data(districts)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DistrictResponse>> getDistrictById(@PathVariable Integer id) {
        DistrictResponse district = districtService.getDistrictById(id);
        ApiResponse<DistrictResponse> response = ApiResponse.<DistrictResponse>builder()
                .status("success")
                .message("Lấy thông tin quận/huyện thành công")
                .data(district)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<DistrictResponse>> createDistrict(@Valid @RequestBody DistrictRequest request) {
        DistrictResponse saved = districtService.createDistrict(request);
        ApiResponse<DistrictResponse> response = ApiResponse.<DistrictResponse>builder()
                .status("success")
                .message("Tạo thông tin quận/huyện thành công")
                .data(saved)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<DistrictResponse>> updateDistrict(
            @PathVariable Integer id,
            @Valid @RequestBody DistrictRequest request) {
        DistrictResponse updated = districtService.updateDistrict(id, request);
        ApiResponse<DistrictResponse> response = ApiResponse.<DistrictResponse>builder()
                .status("success")
                .message("Cập nhật thông tin quận/huyện thành công")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDistrict(@PathVariable Integer id) {
        districtService.deleteDistrict(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa quận/huyện thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
