package com.foxstyle.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_backups", uniqueConstraints = @UniqueConstraint(name = "uk_daily_backup_date", columnNames = "backup_date"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyBackup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "backup_id")
    private Long backupId;

    @Column(name = "backup_date", nullable = false)
    private LocalDate backupDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "payload", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String payload;
}
