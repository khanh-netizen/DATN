package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.ArticleRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.ArticleResponse;
import com.foxstyle.api.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> published() {
        return ok(articleService.getPublished(), "Lấy danh sách bài viết thành công");
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> all() {
        return ok(articleService.getAll(), "Lấy toàn bộ bài viết thành công");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponse>> create(@Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(response(
                articleService.create(request), "Tạo bài viết thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponse>> update(
            @PathVariable Integer id, @Valid @RequestBody ArticleRequest request) {
        return ok(articleService.update(id, request), "Cập nhật bài viết thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        articleService.delete(id);
        return ok(null, "Xóa bài viết thành công");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(response(data, message));
    }

    private <T> ApiResponse<T> response(T data, String message) {
        return ApiResponse.<T>builder()
                .status("success").message(message).data(data)
                .timestamp(LocalDateTime.now()).build();
    }
}
