package com.jelly.zzirit.domain.item.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jelly.zzirit.domain.item.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
	Optional<Brand> findByName(String name);
}
