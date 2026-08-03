package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.SettingRequest;
import com.foxstyle.api.dto.response.SettingResponse;
import com.foxstyle.api.dto.response.PageResponse;
import com.foxstyle.api.entity.Setting;
import com.foxstyle.api.exception.BadRequestException;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.SettingRepository;
import com.foxstyle.api.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SettingServiceImpl implements SettingService {

    private final SettingRepository settingRepository;

    @Override
    public PageResponse<SettingResponse> getAllSettings(Pageable pageable) {
        Page<Setting> page = settingRepository.findAll(pageable);
        return PageResponse.of(page.map(this::convertToResponse));
    }

    @Override
    public SettingResponse getSettingById(Integer settingId) {
        return convertToResponse(findSettingById(settingId));
    }

    @Override
    public SettingResponse getSettingByKey(String settingKey) {
        Setting setting = settingRepository.findBySettingKey(settingKey)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình với key: " + settingKey));
        return convertToResponse(setting);
    }

    @Override
    @Transactional
    public SettingResponse createSetting(SettingRequest request) {
        if (settingRepository.existsBySettingKeyIgnoreCase(request.getSettingKey())) {
            throw new BadRequestException("Key cấu hình đã tồn tại: " + request.getSettingKey());
        }

        Setting setting = Setting.builder()
                .settingKey(request.getSettingKey())
                .settingValue(request.getSettingValue())
                .description(request.getDescription())
                .build();

        return convertToResponse(settingRepository.save(setting));
    }

    @Override
    @Transactional
    public SettingResponse updateSetting(Integer settingId, SettingRequest request) {
        Setting setting = findSettingById(settingId);

        boolean keyChanged = !setting.getSettingKey().equalsIgnoreCase(request.getSettingKey());
        if (keyChanged && settingRepository.existsBySettingKeyIgnoreCase(request.getSettingKey())) {
            throw new BadRequestException("Key cấu hình đã tồn tại: " + request.getSettingKey());
        }

        setting.setSettingKey(request.getSettingKey());
        setting.setSettingValue(request.getSettingValue());
        setting.setDescription(request.getDescription());

        return convertToResponse(settingRepository.save(setting));
    }

    @Override
    @Transactional
    public SettingResponse updateSettingByKey(String settingKey, SettingRequest request) {
        Setting setting = settingRepository.findBySettingKey(settingKey)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình với key: " + settingKey));

        setting.setSettingValue(request.getSettingValue());
        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }

        return convertToResponse(settingRepository.save(setting));
    }

    @Override
    @Transactional
    public void deleteSetting(Integer settingId) {
        settingRepository.delete(findSettingById(settingId));
    }

    private Setting findSettingById(Integer settingId) {
        return settingRepository.findById(settingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình có ID: " + settingId));
    }

    private SettingResponse convertToResponse(Setting setting) {
        return SettingResponse.builder()
                .settingId(setting.getSettingId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .description(setting.getDescription())
                .build();
    }
}
