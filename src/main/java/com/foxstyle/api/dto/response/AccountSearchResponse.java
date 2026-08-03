package com.foxstyle.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSearchResponse {
    private String username;
    private String email;
    private String maskedEmail;
    private String fullName;
    private String maskedPhone;
}
