package com.jelly.zzirit.domain.item.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jelly.zzirit.domain.item.entity.Type;

public interface TypeRepository extends JpaRepository<Type, Long> {
	Optional<Type> findByName(String name);
}