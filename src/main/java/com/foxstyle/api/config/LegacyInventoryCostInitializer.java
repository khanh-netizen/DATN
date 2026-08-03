package com.foxstyle.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Gives legacy stock a reasonable estimated cost when it was inserted directly
 * into product_variants without a stock-import receipt.
 *
 * Real costs entered through stock-import receipts are never overwritten.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LegacyInventoryCostInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        int updated = jdbcTemplate.update("""
                UPDATE pv
                SET pv.cost_price =
                    CASE
                        WHEN estimated.estimated_cost >= prices.sale_price
                            THEN prices.sale_price - 1000
                        ELSE estimated.estimated_cost
                    END
                FROM product_variants pv
                INNER JOIN products p ON p.product_id = pv.product_id
                CROSS APPLY (
                    SELECT COALESCE(NULLIF(pv.price, 0), p.price) AS sale_price
                ) prices
                CROSS APPLY (
                    SELECT ROUND(
                        prices.sale_price *
                        (0.88 + (ABS(CHECKSUM(
                            COALESCE(pv.color, ''), COALESCE(pv.size, '')
                        )) % 5) / 100.0),
                        -3
                    ) AS estimated_cost
                ) estimated
                WHERE (pv.cost_price IS NULL OR pv.cost_price <= 0)
                  AND pv.quantity > 0
                  AND prices.sale_price > 1000
                """);

        if (updated > 0) {
            log.info("Initialized estimated costs for {} legacy inventory variants", updated);
        }
    }
}
