package com.jelly.zzirit.domain.item.repository;

import com.jelly.zzirit.domain.item.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeRepository extends JpaRepository<Type, Long> {
	Optional<Type> findByName(String name);
}