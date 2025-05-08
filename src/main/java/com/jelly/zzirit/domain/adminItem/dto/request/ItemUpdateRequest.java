package com.jelly.zzirit.domain.adminItem.dto.request;

import java.math.BigDecimal;

public record ItemUpdateRequest(
	Integer stockQuantity,
	BigDecimal price
) {}
