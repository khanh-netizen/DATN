package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.BannerRequest;
import com.foxstyle.api.dto.response.BannerResponse;
import com.foxstyle.api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BannerService {

    /** Trang chủ: các banner đang hiển thị theo thứ tự vị trí. */
    List<BannerResponse> getActiveBanners();

    /** Quản trị: toàn bộ banner có phân trang. */
    PageResponse<BannerResponse> getAllBanners(Pageable pageable);

    BannerResponse getBannerById(Integer bannerId);

    BannerResponse createBanner(BannerRequest request);

    BannerResponse updateBanner(Integer bannerId, BannerRequest request);

    void deleteBanner(Integer bannerId);
}
