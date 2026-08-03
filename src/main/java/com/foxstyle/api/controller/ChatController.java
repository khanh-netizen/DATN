package com.foxstyle.api.controller;

import com.foxstyle.api.dto.request.ChatMessageRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.dto.response.ChatMessageResponse;
import com.foxstyle.api.entity.ChatMessage;
import com.foxstyle.api.repository.ChatMessageRepository;
import com.foxstyle.api.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageRepository repository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getAll() {
        return ResponseEntity.ok(response(repository.findAllByOrderBySentAtAsc()));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChannel(
            @PathVariable String channelId,
            Principal principal,
            Authentication authentication) {
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
        String userId = userRepository.findByUsername(principal.getName())
                .map(user -> String.valueOf(user.getUserId()))
                .orElse("");
        if (!isStaff && !channelId.equals(userId) && !channelId.equals("general_group")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(response(repository.findByChannelIdOrderBySentAtAsc(channelId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageResponse>> send(
            @Valid @RequestBody ChatMessageRequest request,
            Principal principal,
            Authentication authentication) {
        String role = "customer";
        String senderId = request.getChannelId();
        String senderName = request.getSenderName() == null ? request.getCustomerName() : request.getSenderName();
        if (principal != null && authentication != null) {
            senderId = userRepository.findByUsername(principal.getName())
                    .map(user -> String.valueOf(user.getUserId()))
                    .orElse(principal.getName());
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) role = "admin";
            else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) role = "staff";
        }
        if (request.getChannelId().equals("staff_admin_group") && role.equals("customer")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ChatMessage saved = repository.save(ChatMessage.builder()
                .channelId(request.getChannelId())
                .customerName(request.getCustomerName())
                .senderId(senderId)
                .senderName(senderName == null || senderName.isBlank() ? "Khách hàng" : senderName)
                .senderRole(role)
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã lưu tin nhắn", toResponse(saved)));
    }

    private ApiResponse<List<ChatMessageResponse>> response(List<ChatMessage> messages) {
        return ApiResponse.success("Lấy tin nhắn thành công", messages.stream().map(this::toResponse).toList());
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .channelId(message.getChannelId())
                .customerName(message.getCustomerName())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }
}
