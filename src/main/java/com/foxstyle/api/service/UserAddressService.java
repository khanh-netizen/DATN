package com.foxstyle.api.service;

import com.foxstyle.api.dto.request.UserAddressRequest;
import com.foxstyle.api.dto.response.UserAddressResponse;
import java.util.List;

public interface UserAddressService {

    List<UserAddressResponse> getMyAddresses(String username);

    UserAddressResponse getAddressById(String username, Integer addressId);

    UserAddressResponse createAddress(String username, UserAddressRequest request);

    UserAddressResponse updateAddress(String username, Integer addressId, UserAddressRequest request);

    void deleteAddress(String username, Integer addressId);
}
