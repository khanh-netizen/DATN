package com.foxstyle.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Byte status;
    private String roleName;
    private String theme;
    private String language;
}
