package com.foxstyle.api.service.impl;

import com.foxstyle.api.dto.request.UserAddressRequest;
import com.foxstyle.api.dto.response.UserAddressResponse;
import com.foxstyle.api.entity.User;
import com.foxstyle.api.entity.UserAddress;
import com.foxstyle.api.exception.ResourceNotFoundException;
import com.foxstyle.api.repository.UserAddressRepository;
import com.foxstyle.api.repository.UserRepository;
import com.foxstyle.api.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserAddressResponse> getMyAddresses(String username) {
        User user = findUserByUsername(username);
        return addressRepository.findByUserUserIdOrderByIsDefaultDesc(user.getUserId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public UserAddressResponse getAddressById(String username, Integer addressId) {
        return convertToResponse(findOwnedAddress(username, addressId));
    }

    @Override
    @Transactional
    public UserAddressResponse createAddress(String username, UserAddressRequest request) {
        User user = findUserByUsername(username);
        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());
        if (isDefault) {
            addressRepository.clearDefaultAddress(user.getUserId());
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .recipientName(request.getRecipientName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .detailAddress(request.getDetailAddress())
                .isDefault(isDefault)
                .build();

        return convertToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public UserAddressResponse updateAddress(String username, Integer addressId, UserAddressRequest request) {
        UserAddress address = findOwnedAddress(username, addressId);

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddress(address.getUser().getUserId());
        }

        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setDetailAddress(request.getDetailAddress());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        return convertToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(String username, Integer addressId) {
        addressRepository.delete(findOwnedAddress(username, addressId));
    }

    // ==================== Private helpers ====================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + username));
    }

    private UserAddress findOwnedAddress(String username, Integer addressId) {
        User user = findUserByUsername(username);
        return addressRepository.findByAddressIdAndUserUserId(addressId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy địa chỉ có ID: " + addressId + " thuộc tài khoản của bạn"));
    }

    private UserAddressResponse convertToResponse(UserAddress address) {
        return UserAddressResponse.builder()
                .addressId(address.getAddressId())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .detailAddress(address.getDetailAddress())
                .isDefault(address.getIsDefault())
                .build();
    }
}
