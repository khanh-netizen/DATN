package com.foxstyle.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxstyle.api.dto.request.DailyBackupRequest;
import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.entity.DailyBackup;
import com.foxstyle.api.repository.DailyBackupRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/daily-backups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DailyBackupController {
    private final DailyBackupRepository repository;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<JsonNode>> create(@Valid @RequestBody DailyBackupRequest request)
            throws JsonProcessingException {
        DailyBackup backup = repository.findByBackupDate(request.getDate()).orElseGet(() ->
                DailyBackup.builder().backupDate(request.getDate()).createdAt(LocalDateTime.now()).build());
        backup.setCreatedBy(request.getCreatedBy());
        backup.setPayload(objectMapper.writeValueAsString(request.getSnapshot()));
        repository.save(backup);
        return ResponseEntity.ok(ApiResponse.<JsonNode>builder().status("success")
                .message("Đã lưu backup hằng ngày vĩnh viễn").data(objectMapper.readTree(backup.getPayload()))
                .timestamp(LocalDateTime.now()).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JsonNode>>> getAll() {
        List<JsonNode> backups = repository.findAllByOrderByBackupDateDesc().stream().map(item -> {
            try { return objectMapper.readTree(item.getPayload()); }
            catch (JsonProcessingException error) { return objectMapper.createObjectNode(); }
        }).toList();
        return ResponseEntity.ok(ApiResponse.<List<JsonNode>>builder().status("success")
                .message("Lấy lịch sử backup thành công").data(backups)
                .timestamp(LocalDateTime.now()).build());
    }
}
