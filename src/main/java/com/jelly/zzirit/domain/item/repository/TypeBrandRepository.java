package com.jelly.zzirit.domain.item.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jelly.zzirit.domain.item.entity.TypeBrand;

public interface TypeBrandRepository extends JpaRepository<TypeBrand, Long> {

	List<TypeBrand> findByTypeId(Long typeId);
	Optional<TypeBrand> findByTypeIdAndBrandId(Long typeId, Long brandId);
}