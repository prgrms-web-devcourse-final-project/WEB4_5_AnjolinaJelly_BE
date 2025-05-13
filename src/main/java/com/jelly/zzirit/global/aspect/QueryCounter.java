package com.jelly.zzirit.global.aspect;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import lombok.Getter;

@Getter
@Component
@RequestScope
public class QueryCounter {

	private int count;

	public void increaseCount() {
		count++;
	}
}