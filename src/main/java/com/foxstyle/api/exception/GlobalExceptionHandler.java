package com.foxstyle.api.exception;

import com.foxstyle.api.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

/**
 * Bắt toàn bộ exception và trả về ApiResponse chuẩn { status, message, data }.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Lỗi validate dữ liệu đầu vào (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, errors);
    }

    // Không tìm thấy tài nguyên
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Lỗi nghiệp vụ do dữ liệu người dùng gửi lên
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Sai tài khoản hoặc mật khẩu
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu!");
    }

    // Tài khoản bị khóa
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(DisabledException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa!");
    }

    // Không đủ quyền truy cập (@PreAuthorize từ chối)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
    }

    // Lỗi thuộc tính sắp xếp hoặc truy vấn không hợp lệ
    @ExceptionHandler({
            org.springframework.data.mapping.PropertyReferenceException.class,
            org.hibernate.query.sqm.UnknownPathException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidPropertyException(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Thuộc tính tìm kiếm hoặc sắp xếp không hợp lệ: " + ex.getMessage());
    }

    // Các lỗi hệ thống chưa lường trước
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã xảy ra lỗi hệ thống: " + ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(@NonNull HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), message));
    }
}
