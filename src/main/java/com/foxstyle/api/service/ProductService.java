package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.ProductRequest;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface ProductService {

    /** Tìm kiếm và lọc sản phẩm đang bán, có phân trang và sắp xếp. */
    PageResponse<ProductResponse> getProducts(Integer categoryId, String keyword,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              Pageable pageable);

    /** Danh sách toàn bộ sản phẩm (kể cả ngừng bán) cho trang quản trị. */
    PageResponse<ProductResponse> getAllProductsForAdmin(Pageable pageable);

    ProductResponse getProductById(Integer productId);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Integer productId, ProductRequest request);

    void deleteProduct(Integer productId);
}
