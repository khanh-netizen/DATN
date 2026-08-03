package com.foxstyle.api.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Object bao bọc chuẩn cho mọi API trả về danh sách có phân trang.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> content;     // Danh sách dữ liệu của trang hiện tại
    private int page;            // Trang hiện tại (bắt đầu từ 0)
    private int size;            // Kích thước trang
    private long totalElements;  // Tổng số phần tử
    private int totalPages;      // Tổng số trang
    private boolean last;        // Có phải trang cuối cùng không

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
