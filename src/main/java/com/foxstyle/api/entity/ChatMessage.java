package com.foxstyle.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_channel_sent_at", columnList = "channel_id,sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "channel_id", nullable = false, length = 150)
    private String channelId;

    @Column(name = "customer_name", columnDefinition = "NVARCHAR(150)")
    private String customerName;

    @Column(name = "sender_id", length = 150)
    private String senderId;

    @Column(name = "sender_name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String senderName;

    @Column(name = "sender_role", nullable = false, length = 30)
    private String senderRole;

    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "sent_at", nullable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();
}
