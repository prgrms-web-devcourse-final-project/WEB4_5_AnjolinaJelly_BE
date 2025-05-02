package com.jelly.zzirit.global.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseTime extends BaseEntity{

	@CreatedDate
	@DateTimeFormat(pattern = "yyyy.MM.dd HH:mm:ss")
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@DateTimeFormat(pattern = "yyyy.MM.dd HH:mm:ss")
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void onPrePersist() {
		this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		this.updatedAt = this.createdAt;
	}

	@PreUpdate
	public void onPreUpdate() {
		this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
	}
}