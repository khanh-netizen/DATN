package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.BannerRequest;
import com.foxstyle.api.dto.response.BannerResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.entity.Banner;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.BannerRepository;
import com.foxstyle.api.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    public List<BannerResponse> getActiveBanners() {
        List<Banner> banners = new ArrayList<>(bannerRepository.findByStatusOrderByPositionAsc((byte) 1));
        bannerRepository.findByBannerTypeOrderByPositionAsc("MARQUEE").forEach(marquee -> {
            boolean alreadyIncluded = banners.stream()
                    .anyMatch(banner -> banner.getBannerId().equals(marquee.getBannerId()));
            if (!alreadyIncluded) {
                banners.add(marquee);
            }
        });

        return banners
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public PageResponse<BannerResponse> getAllBanners(Pageable pageable) {
        return PageResponse.of(bannerRepository.findAll(pageable).map(this::convertToResponse));
    }

    @Override
    public BannerResponse getBannerById(Integer bannerId) {
        return convertToResponse(findBannerById(bannerId));
    }

    @Override
    @Transactional
    public BannerResponse createBanner(BannerRequest request) {
        Banner banner = Banner.builder()
                .title(request.getTitle())
                .imageUrl(request.getImageUrl())
                .bannerType(request.getBannerType() != null ? request.getBannerType() : "IMAGE")
                .linkUrl(request.getLinkUrl())
                .position(request.getPosition() != null ? request.getPosition() : 1)
                .status(request.getStatus() != null ? request.getStatus() : (byte) 1)
                .build();
        return convertToResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(Integer bannerId, BannerRequest request) {
        Banner banner = findBannerById(bannerId);
        banner.setTitle(request.getTitle());
        banner.setImageUrl(request.getImageUrl());
        banner.setBannerType(request.getBannerType() != null ? request.getBannerType() : "IMAGE");
        banner.setLinkUrl(request.getLinkUrl());
        if (request.getPosition() != null) {
            banner.setPosition(request.getPosition());
        }
        if (request.getStatus() != null) {
            banner.setStatus(request.getStatus());
        }
        return convertToResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public void deleteBanner(Integer bannerId) {
        bannerRepository.delete(findBannerById(bannerId));
    }

    private Banner findBannerById(Integer bannerId) {
        return bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner có ID: " + bannerId));
    }

    private BannerResponse convertToResponse(Banner banner) {
        return BannerResponse.builder()
                .bannerId(banner.getBannerId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .bannerType(banner.getBannerType() != null ? banner.getBannerType() : "IMAGE")
                .linkUrl(banner.getLinkUrl())
                .position(banner.getPosition())
                .status(banner.getStatus())
                .build();
    }
}
