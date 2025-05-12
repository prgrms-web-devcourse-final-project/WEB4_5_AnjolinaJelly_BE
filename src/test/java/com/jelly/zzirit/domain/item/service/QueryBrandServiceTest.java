package com.jelly.zzirit.domain.item.service;

import static com.jelly.zzirit.domain.item.domain.fixture.BrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeBrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.item.dto.response.BrandFetchResponse;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;

@ExtendWith(MockitoExtension.class)
public class QueryBrandServiceTest {

	@Mock
	private TypeBrandRepository typeBrandRepository;

	@InjectMocks
	private QueryBrandService queryBrandService;

	@Test
	void 상품_종류에_따른_브랜드를_조회한다() {
		// given
		Type type = 노트북();
		Brand brand1 = 삼성();
		Brand brand2 = 브랜드_생성("LG");

		List<TypeBrand> mockBrands = List.of(타입_브랜드_생성(type, brand1), 타입_브랜드_생성(type, brand2));
		given(typeBrandRepository.findByTypeId(1L)).willReturn(mockBrands);

		// when
		List<BrandFetchResponse> 응답 = queryBrandService.getByType(1L);

		// then
		assertThat(응답).hasSize(2);
	}
}
