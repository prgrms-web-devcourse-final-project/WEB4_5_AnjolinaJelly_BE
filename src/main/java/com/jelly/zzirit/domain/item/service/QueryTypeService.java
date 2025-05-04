package com.jelly.zzirit.domain.item.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.TypeResponses;
import com.jelly.zzirit.domain.item.repository.TypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryTypeService {

	private final TypeRepository typeRepository;

	public TypeResponses getAll() {
		return TypeResponses.from(typeRepository.findAll());
	}
}
