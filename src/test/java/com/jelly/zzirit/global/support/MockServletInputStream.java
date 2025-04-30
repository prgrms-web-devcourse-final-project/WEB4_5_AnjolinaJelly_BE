package com.jelly.zzirit.global.support;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MockServletInputStream extends ServletInputStream {

	private final ByteArrayInputStream inputStream;

	public MockServletInputStream(byte[] bytes) {
		this.inputStream = new ByteArrayInputStream(bytes);
	}

	@Override
	public boolean isFinished() {
		return inputStream.available() == 0;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		// 구현 필요 없음
	}

	@Override
	public int read() throws IOException {
		return inputStream.read();
	}
}