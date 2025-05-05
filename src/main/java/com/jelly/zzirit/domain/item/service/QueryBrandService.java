package com.jelly.zzirit.domain.item.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.response.BrandResponse;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryBrandService {

	private final TypeBrandRepository typeBrandRepository;

	public List<BrandResponse> getByType(Long typeId) {
		return typeBrandRepository.findByTypeId(typeId).stream()
			.map(typeBrand -> BrandResponse.from(typeBrand.getBrand()))
			.toList();
	}
}
