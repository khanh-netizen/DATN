package com.foxstyle.api.config;

import com.foxstyle.api.entity.OrderStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusConverter implements Converter<String, OrderStatus> {
    @Override
    public OrderStatus convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            // Try by value (ordinal/byte value)
            byte val = Byte.parseByte(source);
            return OrderStatus.fromValue(val);
        } catch (NumberFormatException e) {
            // Try by name (case-insensitive)
            return OrderStatus.valueOf(source.toUpperCase());
        }
    }
}
