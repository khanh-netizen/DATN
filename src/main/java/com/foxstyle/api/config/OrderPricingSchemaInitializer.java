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
public class OrderPricingSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                IF COL_LENGTH('orders', 'tax_amount') IS NULL
                    ALTER TABLE orders ADD tax_amount DECIMAL(12,2) NOT NULL
                        CONSTRAINT DF_orders_tax_amount DEFAULT 0 WITH VALUES
                """);
    }
}
