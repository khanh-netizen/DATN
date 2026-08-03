package com.foxstyle.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingResponse {
    private Integer settingId;
    private String settingKey;
    private String settingValue;
    private String description;
}
