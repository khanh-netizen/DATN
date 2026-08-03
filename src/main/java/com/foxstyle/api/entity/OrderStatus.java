package com.foxstyle.api.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING((byte) 0, "Chờ xử lý"),
    PROCESSING((byte) 1, "Đang xử lý"),
    SHIPPING((byte) 2, "Đang giao"),
    DELIVERED((byte) 3, "Đã giao"),
    CANCELLED((byte) 4, "Đã hủy"),
    RETURNED((byte) 5, "Hoàn hàng");

    private final byte value;
    private final String description;

    OrderStatus(byte value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public byte getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static OrderStatus fromValue(byte value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status value: " + value);
    }
}
