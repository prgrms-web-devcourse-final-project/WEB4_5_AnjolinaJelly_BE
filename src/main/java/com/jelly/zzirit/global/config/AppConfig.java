package com.jelly.zzirit.global.config;

import java.util.Arrays;
import java.util.List;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import lombok.Getter;

@EnableRetry
@Configuration
public class AppConfig {

	@Getter
	private static Tika tika;

	@Getter
	private static String siteBackUrl;

	@Getter
	private static List<String> siteFrontUrlList;

	@Getter
	private static String fileUploadDir;

	@Getter
	private static String customMaxImageSize;

	@Getter
	private static String customMaxFileSize;

	@Getter
	private static String siteDomain;

	@Value("${custom.site.back-url}")
	public void setSiteBackUrl(String siteBackUrl) {
		AppConfig.siteBackUrl = siteBackUrl;
	}

	@Value("${custom.site.front-url}")
	public void setSiteFrontUrl(String siteFrontUrl) {
		AppConfig.siteFrontUrlList = Arrays.asList(siteFrontUrl.split(","));
	}

	@Value("${custom.file.upload-dir}")
	public void setFileUploadDir(String fileUploadDir) {
		AppConfig.fileUploadDir = fileUploadDir;
	}

	@Value("${custom.upload.max-image-size}")
	public void setCustomMaxImageSize(String size) {
		AppConfig.customMaxImageSize = size;
	}

	@Value("${custom.upload.max-file-size}")
	public void setCustomMaxFileSize(String size) {
		AppConfig.customMaxFileSize = size;
	}

	@Value("${custom.site.domain}")
	public void setSiteDomain(String siteDomain) {
		AppConfig.siteDomain = siteDomain;
	}

	@Autowired
	public void setTika(Tika tika) {
		AppConfig.tika = tika;
	}
}