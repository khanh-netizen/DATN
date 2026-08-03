package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.ProductImageRequest;
import com.foxstyle.api.dto.request.ProductRequest;
import com.foxstyle.api.dto.request.ProductVariantRequest;
import com.foxstyle.api.dto.response.*;
import com.foxstyle.api.entity.*;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.*;
import com.foxstyle.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductComboItemRepository comboItemRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final CartDetailRepository cartDetailRepository;


    @Override
    public PageResponse<ProductResponse> getProducts(Integer categoryId, String keyword,
                                                     BigDecimal minPrice, BigDecimal maxPrice,
                                                     Pageable pageable) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<Product> page = productRepository.filterProducts(
                categoryId, normalizedKeyword, minPrice, maxPrice, pageable);
        return PageResponse.of(page.map(this::convertToSummaryResponse));
    }

    @Override
    public PageResponse<ProductResponse> getAllProductsForAdmin(Pageable pageable) {
        return PageResponse.of(productRepository.findAll(pageable).map(this::convertToSummaryResponse));
    }

    @Override
    public ProductResponse getProductById(Integer productId) {
        Product product = findProductById(productId);
        return convertToDetailResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .category(findCategoryById(request.getCategoryId()))
                .status(request.getStatus() != null ? request.getStatus() : (byte) 1)
                .build();
        applyBasicInfo(product, request);

        Product saved = productRepository.save(product);
        saveVariants(saved, request.getVariants());
        saveImages(saved, request.getImages());
        saveComboItems(saved, request.getComboProductIds(), request.getComboGiftProductIds());

        return convertToDetailResponse(findProductById(saved.getProductId()));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer productId, ProductRequest request) {
        Product product = findProductById(productId);
        product.setCategory(findCategoryById(request.getCategoryId()));
        applyBasicInfo(product, request);
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        if (request.getVariants() != null) {
            updateVariants(product, request.getVariants());
        }

        if (request.getImages() != null) {
            updateImages(product, request.getImages());
        }
        if (request.getComboProductIds() != null) {
            comboItemRepository.deleteByComboProductProductId(product.getProductId());
            saveComboItems(product, request.getComboProductIds(), request.getComboGiftProductIds());
        }

        return convertToDetailResponse(productRepository.save(product));

    }

    @Override
    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = findProductById(productId);
        // Xóa mềm: chuyển trạng thái ngừng kinh doanh để không phá vỡ đơn hàng cũ
        product.setStatus((byte) 0);
        productRepository.save(product);
    }

    // ==================== Private helpers ====================

    private Product findProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + productId));
    }

    private Category findCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục có ID: " + categoryId));
    }

    private void applyBasicInfo(Product product, ProductRequest request) {
        validatePrices(request);
        if (StringUtils.hasText(request.getProductName())) {
            product.setProductName(request.getProductName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }
        product.setFlashSaleStartAt(request.getFlashSaleStartAt());
        product.setFlashSaleEndAt(request.getFlashSaleEndAt());
        if (StringUtils.hasText(request.getDescription())) {
            product.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getImageUrl())) {
            product.setImageUrl(request.getImageUrl());
        }
        if (StringUtils.hasText(request.getMaterial())) {
            product.setMaterial(request.getMaterial());
        }
        if (StringUtils.hasText(request.getBrand())) {
            product.setBrand(request.getBrand().trim());
        }
        if (StringUtils.hasText(request.getOrigin())) {
            product.setOrigin(request.getOrigin());
        }
        if (request.getCareInstructions() != null) {
            product.setCareInstructions(request.getCareInstructions());
        }
        if (request.getFitGuide() != null) {
            product.setFitGuide(request.getFitGuide());
        }
        if (request.getIsCombo() != null) {
            product.setIsCombo(request.getIsCombo());
        } else if (product.getProductId() == null) {
            product.setIsCombo(Boolean.FALSE);
        }
        if (request.getVideoUrl() != null) {
            product.setVideoUrl(request.getVideoUrl());
        }
    }

    private void validatePrices(ProductRequest request) {
        if (request.getOriginalPrice() != null
                && request.getOriginalPrice().compareTo(request.getPrice()) < 0) {
            throw new BadRequestException("Giá gốc không được nhỏ hơn giá bán hiện tại");
        }
    }

    private void saveVariants(Product product, List<ProductVariantRequest> variantRequests) {
        if (variantRequests == null) {
            return;
        }
        validateUniqueVariants(variantRequests);
        for (ProductVariantRequest req : variantRequests) {
            String sku = req.getSku();
            if (sku == null || sku.trim().isEmpty()) {
                sku = "SKU-" + product.getProductId() + "-" +
                        java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            } else {
                sku = sku.trim();
                if (variantRepository.existsBySku(sku)) {
                    throw new BadRequestException("SKU đã tồn tại: " + sku);
                }
            }
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .color(req.getColor())
                    .size(req.getSize())
                    .quantity(req.getQuantity())
                    .sku(sku)
                    .price(req.getPrice())
                    .costPrice(req.getCostPrice() == null ? java.math.BigDecimal.ZERO : req.getCostPrice())
                    .imageUrl(req.getImageUrl())
                    .build();
            variantRepository.save(variant);
        }
    }

    private void updateVariants(Product product, List<ProductVariantRequest> variantRequests) {
        validateUniqueVariants(variantRequests);
        List<ProductVariant> existingVariants = variantRepository.findByProductProductId(product.getProductId());
        
        List<Integer> requestVariantIds = variantRequests.stream()
                .map(ProductVariantRequest::getVariantId)
                .filter(java.util.Objects::nonNull)
                .toList();

        for (ProductVariant existing : existingVariants) {
            if (!requestVariantIds.contains(existing.getVariantId())) {
                cartDetailRepository.deleteByVariantVariantId(existing.getVariantId());
                variantRepository.delete(existing);
            }
        }

        for (ProductVariantRequest req : variantRequests) {
            if (req.getVariantId() != null) {
                ProductVariant variant = variantRepository.findById(req.getVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy biến thể có ID: " + req.getVariantId()));
                if (!variant.getProduct().getProductId().equals(product.getProductId())) {
                    throw new BadRequestException("Biến thể không thuộc sản phẩm đang cập nhật");
                }
                variant.setColor(req.getColor());
                variant.setSize(req.getSize());
                variant.setQuantity(req.getQuantity());
                if (req.getSku() != null) {
                    String sku = req.getSku().trim();
                    if (!sku.isEmpty() && variantRepository.existsBySkuAndVariantIdNot(sku, variant.getVariantId())) {
                        throw new BadRequestException("SKU đã tồn tại: " + sku);
                    }
                    variant.setSku(sku.isEmpty() ? variant.getSku() : sku);
                }
                variant.setPrice(req.getPrice());
                if (req.getCostPrice() != null) variant.setCostPrice(req.getCostPrice());
                variant.setImageUrl(req.getImageUrl());
                variantRepository.save(variant);
            } else {
                saveVariants(product, List.of(req));
            }
        }
    }

    private void updateImages(Product product, List<ProductImageRequest> imageRequests) {
        List<ProductImage> existingImages = imageRepository.findByProductProductIdOrderByDisplayOrderAsc(product.getProductId());
        imageRepository.deleteAll(existingImages);
        saveImages(product, imageRequests);
    }


    private void saveImages(Product product, List<ProductImageRequest> imageRequests) {
        if (imageRequests == null) {
            return;
        }
        for (ProductImageRequest req : imageRequests) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(req.getImageUrl())
                    .isPrimary(Boolean.TRUE.equals(req.getIsPrimary()))
                    .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 1)
                    .build();
            imageRepository.save(image);
        }
    }

    private void validateUniqueVariants(List<ProductVariantRequest> requests) {
        java.util.Set<String> keys = new java.util.HashSet<>();
        for (ProductVariantRequest request : requests) {
            String key = request.getColor().trim().toLowerCase(java.util.Locale.ROOT)
                    + "|" + request.getSize().trim().toLowerCase(java.util.Locale.ROOT);
            if (!keys.add(key)) {
                throw new BadRequestException("Biến thể màu " + request.getColor()
                        + " và size " + request.getSize() + " đang bị trùng");
            }
        }
    }

    private void saveComboItems(Product comboProduct, List<Integer> componentIds, List<Integer> giftIds) {
        if (componentIds == null || componentIds.isEmpty()) {
            return;
        }
        if (!Boolean.TRUE.equals(comboProduct.getIsCombo())) {
            throw new BadRequestException("Chỉ sản phẩm loại combo mới được chứa sản phẩm thành phần");
        }
        List<Integer> uniqueIds = componentIds.stream().distinct().toList();
        java.util.Set<Integer> gifts = giftIds == null ? java.util.Set.of() : new java.util.HashSet<>(giftIds);
        if (!uniqueIds.containsAll(gifts)) {
            throw new BadRequestException("Quà tặng phải là một sản phẩm nằm trong combo");
        }
        if (uniqueIds.size() < 2) {
            throw new BadRequestException("Set combo phải có ít nhất 2 sản phẩm lẻ");
        }
        int displayOrder = 1;
        for (Integer componentId : uniqueIds) {
            if (componentId.equals(comboProduct.getProductId())) {
                throw new BadRequestException("Combo không thể chứa chính nó");
            }
            Product component = findProductById(componentId);
            if (Boolean.TRUE.equals(component.getIsCombo())) {
                throw new BadRequestException("Combo chỉ được tạo từ sản phẩm lẻ");
            }
            comboItemRepository.save(ProductComboItem.builder()
                    .comboProduct(comboProduct)
                    .componentProduct(component)
                    .quantity(1)
                    .displayOrder(displayOrder++)
                    .isGift(gifts.contains(componentId))
                    .build());
        }
    }

    /** Bản rút gọn cho danh sách (không kèm variants/images chi tiết). */
    private ProductResponse convertToSummaryResponse(Product product) {
        return convertToDetailResponse(product);
    }

    /** Bản đầy đủ cho trang chi tiết sản phẩm. */
    private ProductResponse convertToDetailResponse(Product product) {
        List<ProductVariantResponse> variants = variantRepository
                .findByProductProductId(product.getProductId())
                .stream()
                .map(this::convertVariant)
                .toList();

        List<ProductImageResponse> images = imageRepository
                .findByProductProductIdOrderByDisplayOrderAsc(product.getProductId())
                .stream()
                .map(this::convertImage)
                .toList();

        ProductResponse response = buildBaseResponse(product);
        response.setVariants(variants);
        response.setImages(images);
        return response;
    }

    private ProductResponse buildBaseResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .categoryId(product.getCategory().getCategoryId())
                .categoryName(product.getCategory().getCategoryName())
                .productName(product.getProductName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .flashSaleStartAt(product.getFlashSaleStartAt())
                .flashSaleEndAt(product.getFlashSaleEndAt())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .material(product.getMaterial())
                .brand(product.getBrand())
                .origin(product.getOrigin())
                .careInstructions(product.getCareInstructions())
                .fitGuide(product.getFitGuide())
                .isCombo(Boolean.TRUE.equals(product.getIsCombo()))
                .comboProductIds(comboItemRepository
                        .findByComboProductProductIdOrderByDisplayOrderAsc(product.getProductId())
                        .stream()
                        .map(item -> item.getComponentProduct().getProductId())
                        .toList())
                .comboGiftProductIds(comboItemRepository
                        .findByComboProductProductIdOrderByDisplayOrderAsc(product.getProductId())
                        .stream().filter(item -> Boolean.TRUE.equals(item.getIsGift()))
                        .map(item -> item.getComponentProduct().getProductId()).toList())
                .status(product.getStatus())
                .averageRating(reviewRepository.findAverageRatingByProductId(product.getProductId()))
                .videoUrl(product.getVideoUrl())
                .build();
    }

    private ProductVariantResponse convertVariant(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .variantId(variant.getVariantId())
                .color(variant.getColor())
                .size(variant.getSize())
                .quantity(variant.getQuantity())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .costPrice(variant.getCostPrice())
                .imageUrl(variant.getImageUrl())
                .build();
    }

    private ProductImageResponse convertImage(ProductImage image) {
        return ProductImageResponse.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .isPrimary(image.getIsPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}
