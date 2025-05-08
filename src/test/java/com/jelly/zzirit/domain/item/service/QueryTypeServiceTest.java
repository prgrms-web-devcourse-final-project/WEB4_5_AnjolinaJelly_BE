package com.jelly.zzirit.domain.item.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.item.dto.response.TypeResponse;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.repository.TypeRepository;

@ExtendWith(MockitoExtension.class)
public class QueryTypeServiceTest {

	@Mock
	private TypeRepository typeRepository;

	@InjectMocks
	private QueryTypeService queryTypeService;

	@Test
	void 상품_종률를_전체를_반환한다() {
		// given
		List<Type> mockTypes = List.of(mock(Type.class), mock(Type.class), mock(Type.class));
		given(typeRepository.findAll()).willReturn(mockTypes);

		// when
		List<TypeResponse> 응답 = queryTypeService.getAll();

		// then
		assertThat(응답).hasSize(mockTypes.size());
	}
}
