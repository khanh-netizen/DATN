package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {

    @NotBlank(message = "Google Token ID không được để trống")
    private String idToken;
}
