package com.foxstyle.api.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyBackupRequest {
    @NotNull private LocalDate date;
    @NotBlank private String createdBy;
    @NotNull private JsonNode snapshot;
}
