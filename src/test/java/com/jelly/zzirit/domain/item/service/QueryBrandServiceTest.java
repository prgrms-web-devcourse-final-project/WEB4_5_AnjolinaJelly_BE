package com.jelly.zzirit.domain.item.service;

import static com.jelly.zzirit.domain.item.domain.fixture.BrandFixture.*;
import static com.jelly.zzirit.domain.item.domain.fixture.TypeFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.item.domain.fixture.BrandFixture;
import com.jelly.zzirit.domain.item.domain.fixture.TypeFixture;
import com.jelly.zzirit.domain.item.dto.response.BrandResponse;
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

		List<TypeBrand> mockBrands = List.of(new TypeBrand(type, brand1), new TypeBrand(type, brand2));
		given(typeBrandRepository.findByTypeId(1L)).willReturn(mockBrands);

		// when
		List<BrandResponse> 응답 = queryBrandService.getByType(1L);

		// then
		assertThat(응답.size()).isEqualTo(2);
	}
}
