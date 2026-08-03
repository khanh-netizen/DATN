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
public class StaffProfileSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("IF COL_LENGTH('users', 'citizen_id') IS NULL ALTER TABLE users ADD citizen_id VARCHAR(12) NULL");
        jdbcTemplate.execute("IF COL_LENGTH('users', 'address') IS NULL ALTER TABLE users ADD address NVARCHAR(500) NULL");
        jdbcTemplate.execute("IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_users_citizen_id' AND object_id = OBJECT_ID('users')) CREATE UNIQUE INDEX UX_users_citizen_id ON users(citizen_id) WHERE citizen_id IS NOT NULL");
    }
}
