package com.jelly.zzirit.domain.order.service.cache.stock;

public enum RedisStockKey {

	ITEM("stock:item"),
	TIME_DEAL("stock:timedeal"),

	LOCK_ITEM("lock:stock:item"),
	LOCK_TIME_DEAL("lock:stock:timedeal");

	private final String prefix;

	RedisStockKey(String prefix) {
		this.prefix = prefix;
	}

	public String of(Long id) {
		return prefix + ":" + id;
	}
}