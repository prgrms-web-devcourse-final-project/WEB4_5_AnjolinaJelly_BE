package com.jelly.zzirit.domain.adminItem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ItemUpdateRequest(
	@NotBlank String name,
	@PositiveOrZero int stockQuantity,
	@PositiveOrZero BigDecimal price,
	@NotNull Long typeId,
	@NotNull Long brandId
) {}
