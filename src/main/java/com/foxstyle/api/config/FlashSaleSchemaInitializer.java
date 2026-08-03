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
public class FlashSaleSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("IF COL_LENGTH('products','flash_sale_start_at') IS NULL " +
                "ALTER TABLE products ADD flash_sale_start_at DATETIME2 NULL");
        jdbcTemplate.execute("IF COL_LENGTH('products','flash_sale_end_at') IS NULL " +
                "ALTER TABLE products ADD flash_sale_end_at DATETIME2 NULL");
    }
}
