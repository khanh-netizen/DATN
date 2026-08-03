package com.foxstyle.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DailyBackupSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
            IF OBJECT_ID('daily_backups', 'U') IS NULL
            CREATE TABLE daily_backups (
                backup_id BIGINT IDENTITY(1,1) PRIMARY KEY,
                backup_date DATE NOT NULL,
                created_at DATETIME2 NOT NULL,
                created_by NVARCHAR(100) NOT NULL,
                payload NVARCHAR(MAX) NOT NULL,
                CONSTRAINT uk_daily_backup_date UNIQUE (backup_date)
            )
            """);
    }
}
