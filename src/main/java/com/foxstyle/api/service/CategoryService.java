package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.CategoryRequest;
import com.foxstyle.api.dto.response.CategoryResponse;
import com.foxstyle.api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    /** Danh sách phân trang; onlyActive = true chỉ lấy danh mục đang hiển thị. */
    PageResponse<CategoryResponse> getAllCategories(boolean onlyActive, Pageable pageable);

    CategoryResponse getCategoryById(Integer categoryId);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Integer categoryId, CategoryRequest request);

    void deleteCategory(Integer categoryId);
}
