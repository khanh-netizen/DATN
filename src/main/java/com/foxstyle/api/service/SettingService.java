package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.SettingRequest;
import com.foxstyle.api.dto.response.SettingResponse;
import com.foxstyle.api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface SettingService {
    PageResponse<SettingResponse> getAllSettings(Pageable pageable);
    SettingResponse getSettingById(Integer settingId);
    SettingResponse getSettingByKey(String settingKey);
    SettingResponse createSetting(SettingRequest request);
    SettingResponse updateSetting(Integer settingId, SettingRequest request);
    SettingResponse updateSettingByKey(String settingKey, SettingRequest request);
    void deleteSetting(Integer settingId);
}
