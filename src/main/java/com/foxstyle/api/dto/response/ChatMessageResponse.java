package com.foxstyle.api.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private String channelId;
    private String customerName;
    private String senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private LocalDateTime sentAt;
}
