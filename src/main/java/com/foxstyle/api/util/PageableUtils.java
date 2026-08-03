package com.foxstyle.api.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

public class PageableUtils {

    /**
     * Sanitizes Pageable object by removing invalid Swagger default sort properties (like "string").
     */
    public static Pageable sanitize(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> validOrders = pageable.getSort().stream()
                .filter(order -> !"string".equalsIgnoreCase(order.getProperty()))
                .collect(Collectors.toList());

        if (validOrders.size() == pageable.getSort().toList().size()) {
            return pageable;
        }

        Sort newSort = validOrders.isEmpty() ? Sort.unsorted() : Sort.by(validOrders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);
    }
}
