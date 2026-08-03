package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.DistrictRequest;
import com.foxstyle.api.dto.response.DistrictResponse;
import com.foxstyle.api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface DistrictService {
    PageResponse<DistrictResponse> getAllDistricts(boolean onlyActive, Pageable pageable);
    DistrictResponse getDistrictById(Integer districtId);
    DistrictResponse createDistrict(DistrictRequest request);
    DistrictResponse updateDistrict(Integer districtId, DistrictRequest request);
    void deleteDistrict(Integer districtId);
}
