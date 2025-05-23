package com.jelly.zzirit.global.util;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.EMPTY_LIST;

import java.util.Arrays;
import java.util.List;

import io.jsonwebtoken.lang.Strings;

public class StringUtils {

	private static final String COMMA_SEPARATOR = ",";

	private StringUtils() {
	}

	public static List<String> convertStringsToCollection(String values) {
		if (Strings.hasText(values)) {
			String decodedValues = decode(values, UTF_8);
			return Arrays.asList(decodedValues.split(COMMA_SEPARATOR));
		}
		return EMPTY_LIST;
	}
}
