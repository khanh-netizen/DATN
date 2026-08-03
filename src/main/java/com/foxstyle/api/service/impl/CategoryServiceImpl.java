package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.CategoryRequest;
import com.foxstyle.api.dto.response.CategoryResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.entity.Category;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.CategoryRepository;
import com.foxstyle.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public PageResponse<CategoryResponse> getAllCategories(boolean onlyActive, Pageable pageable) {
        Page<Category> page = onlyActive
                ? categoryRepository.findByStatus((byte) 1, pageable)
                : categoryRepository.findAll(pageable);
        return PageResponse.of(page.map(this::convertToResponse));
    }

    @Override
    public CategoryResponse getCategoryById(Integer categoryId) {
        return convertToResponse(findCategoryById(categoryId));
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new BadRequestException("Tên danh mục đã tồn tại: " + request.getCategoryName());
        }

        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : (byte) 1)
                .build();

        return convertToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer categoryId, CategoryRequest request) {
        Category category = findCategoryById(categoryId);

        boolean nameChanged = !category.getCategoryName().equalsIgnoreCase(request.getCategoryName());
        if (nameChanged && categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new BadRequestException("Tên danh mục đã tồn tại: " + request.getCategoryName());
        }

        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }

        return convertToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Integer categoryId) {
        categoryRepository.delete(findCategoryById(categoryId));
    }

    // ==================== Private helpers ====================

    private Category findCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục có ID: " + categoryId));
    }

    private CategoryResponse convertToResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .status(category.getStatus())
                .build();
    }
}
