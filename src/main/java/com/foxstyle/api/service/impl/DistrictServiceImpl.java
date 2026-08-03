package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.DistrictRequest;
import com.foxstyle.api.dto.response.DistrictResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.entity.District;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.DistrictRepository;
import com.foxstyle.api.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;

    @Override
    public PageResponse<DistrictResponse> getAllDistricts(boolean onlyActive, Pageable pageable) {
        Page<District> page = onlyActive
                ? districtRepository.findByStatus((byte) 1, pageable)
                : districtRepository.findAll(pageable);
        return PageResponse.of(page.map(this::convertToResponse));
    }

    @Override
    public DistrictResponse getDistrictById(Integer districtId) {
        return convertToResponse(findDistrictById(districtId));
    }

    @Override
    @Transactional
    public DistrictResponse createDistrict(DistrictRequest request) {
        if (districtRepository.existsByDistrictNameIgnoreCaseAndProvinceIgnoreCase(request.getDistrictName(), request.getProvince())) {
            throw new BadRequestException("Quận/Huyện " + request.getDistrictName() + " thuộc tỉnh " + request.getProvince() + " đã tồn tại!");
        }

        District district = District.builder()
                .districtName(request.getDistrictName())
                .province(request.getProvince())
                .status(request.getStatus() != null ? request.getStatus() : (byte) 1)
                .build();

        return convertToResponse(districtRepository.save(district));
    }

    @Override
    @Transactional
    public DistrictResponse updateDistrict(Integer districtId, DistrictRequest request) {
        District district = findDistrictById(districtId);

        boolean nameOrProvChanged = !district.getDistrictName().equalsIgnoreCase(request.getDistrictName())
                || !district.getProvince().equalsIgnoreCase(request.getProvince());

        if (nameOrProvChanged && districtRepository.existsByDistrictNameIgnoreCaseAndProvinceIgnoreCase(request.getDistrictName(), request.getProvince())) {
            throw new BadRequestException("Quận/Huyện " + request.getDistrictName() + " thuộc tỉnh " + request.getProvince() + " đã tồn tại!");
        }

        district.setDistrictName(request.getDistrictName());
        district.setProvince(request.getProvince());
        if (request.getStatus() != null) {
            district.setStatus(request.getStatus());
        }

        return convertToResponse(districtRepository.save(district));
    }

    @Override
    @Transactional
    public void deleteDistrict(Integer districtId) {
        districtRepository.delete(findDistrictById(districtId));
    }

    private District findDistrictById(Integer districtId) {
        return districtRepository.findById(districtId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quận/huyện có ID: " + districtId));
    }

    private DistrictResponse convertToResponse(District district) {
        return DistrictResponse.builder()
                .districtId(district.getDistrictId())
                .districtName(district.getDistrictName())
                .province(district.getProvince())
                .status(district.getStatus())
                .build();
    }
}
