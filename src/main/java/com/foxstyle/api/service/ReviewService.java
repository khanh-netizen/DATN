package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.ReviewRequest;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    PageResponse<ReviewResponse> getAllReviews(Pageable pageable);

    PageResponse<ReviewResponse> getReviewsByProduct(Integer productId, Pageable pageable);

    ReviewResponse createReview(String username, ReviewRequest request);

    /** Chủ review được sửa review của mình. */
    ReviewResponse updateReview(String username, Integer reviewId, ReviewRequest request);

    /** Chủ review hoặc ADMIN được xóa. */
    void deleteReview(String username, boolean isAdmin, Integer reviewId);
}
