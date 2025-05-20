package com.jelly.zzirit.domain.admin.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ItemUpdateRequest(
	@PositiveOrZero Integer stockQuantity,
	@PositiveOrZero BigDecimal price,
    String imageUrl
) {}
