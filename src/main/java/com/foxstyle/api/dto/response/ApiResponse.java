package com.foxstyle.api.dto.response;

import lombok.*;

/**
 * Cấu trúc response chuẩn cho toàn bộ API:
 * { "status": 200, "message": "...", "data": {...} }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private String status;      // Trạng thái ("success", "error" hoặc mã lỗi)
    private String message;     // Thông báo thân thiện với người dùng
    private T data;             // Dữ liệu trả về (null nếu lỗi)
    private java.time.LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(String.valueOf(status))
                .message(message)
                .data(null)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}
