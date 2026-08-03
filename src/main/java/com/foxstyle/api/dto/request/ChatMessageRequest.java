package com.foxstyle.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    @NotBlank
    private String channelId;
    private String customerName;
    private String senderName;
    @NotBlank
    private String content;
}
