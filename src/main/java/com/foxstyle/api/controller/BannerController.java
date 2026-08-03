package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.BannerRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.BannerResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getActiveBanners() {
        List<BannerResponse> banners = bannerService.getActiveBanners();
        ApiResponse<List<BannerResponse>> response = ApiResponse.<List<BannerResponse>>builder()
                .status("success")
                .message("Lấy danh sách banner hiển thị thành công")
                .data(banners)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BannerResponse>>> getAllBanners(Pageable pageable) {
        PageResponse<BannerResponse> banners = bannerService.getAllBanners(pageable);
        ApiResponse<PageResponse<BannerResponse>> response = ApiResponse.<PageResponse<BannerResponse>>builder()
                .status("success")
                .message("Lấy danh sách tất cả banner thành công")
                .data(banners)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> getBannerById(@PathVariable Integer id) {
        BannerResponse banner = bannerService.getBannerById(id);
        ApiResponse<BannerResponse> response = ApiResponse.<BannerResponse>builder()
                .status("success")
                .message("Lấy banner chi tiết thành công")
                .data(banner)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerResponse>> createBanner(@Valid @RequestBody BannerRequest request) {
        BannerResponse saved = bannerService.createBanner(request);
        ApiResponse<BannerResponse> response = ApiResponse.<BannerResponse>builder()
                .status("success")
                .message("Tạo banner mới thành công")
                .data(saved)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerResponse>> updateBanner(
            @PathVariable Integer id,
            @Valid @RequestBody BannerRequest request) {
        BannerResponse updated = bannerService.updateBanner(id, request);
        ApiResponse<BannerResponse> response = ApiResponse.<BannerResponse>builder()
                .status("success")
                .message("Cập nhật banner thành công")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Integer id) {
        bannerService.deleteBanner(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("success")
                .message("Xóa banner thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
