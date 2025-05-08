package com.jelly.zzirit.domain.admin.dto.request;

import java.math.BigDecimal;

public record ItemUpdateRequest(
	Integer stockQuantity,
	BigDecimal price
) {}
