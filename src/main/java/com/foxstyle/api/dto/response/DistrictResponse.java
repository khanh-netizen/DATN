package com.foxstyle.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictResponse {
    private Integer districtId;
    private String districtName;
    private String province;
    private Byte status;
}
