package com.toyProject.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record ProductDto(
        Long id,
        String productName,
        String description,
        String imageUrl,
        int price,
        int capacity,
        long joinedCount,
        LocalDateTime createdDate,
        LocalDateTime endDate,
        List<String> tagNames
) {}
