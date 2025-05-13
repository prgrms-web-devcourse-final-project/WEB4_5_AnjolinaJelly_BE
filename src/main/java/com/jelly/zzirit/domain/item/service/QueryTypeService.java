package com.jelly.zzirit.domain.item.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.response.TypeFetchResponse;
import com.jelly.zzirit.domain.item.repository.TypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryTypeService {

	private final TypeRepository typeRepository;

	public List<TypeFetchResponse> getAll() {
		return typeRepository.findAll().stream()
			.map(TypeFetchResponse::from)
			.toList();
	}
}
