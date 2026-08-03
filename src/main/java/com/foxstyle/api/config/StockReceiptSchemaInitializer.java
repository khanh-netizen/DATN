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
public class StockReceiptSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        add("subtotal_amount", "DECIMAL(14,2)");
        add("discount_amount", "DECIMAL(14,2)");
        add("shipping_fee", "DECIMAL(14,2)");
        add("other_fee", "DECIMAL(14,2)");
        add("tax_rate", "DECIMAL(5,2)");
        add("tax_amount", "DECIMAL(14,2)");
    }

    private void add(String column, String type) {
        jdbcTemplate.execute("IF COL_LENGTH('stock_import_receipts', '" + column + "') IS NULL " +
                "ALTER TABLE stock_import_receipts ADD " + column + " " + type +
                " NOT NULL DEFAULT 0 WITH VALUES");
    }
}
