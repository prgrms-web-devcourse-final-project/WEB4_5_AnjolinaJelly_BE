package com.jelly.zzirit.global.support;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.Snippet;

import com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper;

import io.restassured.filter.Filter;

public class OpenApiDocumentationFilter {

	public static Filter of(String identifier) {
		return RestAssuredRestDocumentationWrapper.document(identifier);
	}

	public static Filter of(String identifier, FieldDescriptor[] requestFields) {
		return RestAssuredRestDocumentationWrapper.document(
			identifier,
			requestFields(requestFields)
		);
	}

	public static Filter of(String identifier, FieldDescriptor[] requestFields, FieldDescriptor[] responseFields) {
		return RestAssuredRestDocumentationWrapper.document(
			identifier,
			requestFields(requestFields),
			responseFields(responseFields)
		);
	}

	public static Filter of(String identifier, ParameterDescriptor[] pathParameters, FieldDescriptor[] requestFields, FieldDescriptor[] responseFields) {
		return RestAssuredRestDocumentationWrapper.document(
			identifier,
			pathParameters(pathParameters),
			requestFields(requestFields),
			responseFields(responseFields)
		);
	}

	public static Filter ofWithResponseFields(String identifier, FieldDescriptor[] responseFields) {
		return RestAssuredRestDocumentationWrapper.document(
			identifier,
			new Snippet[] {
				responseFields(responseFields)
			}
		);
	}

	public static Filter ofWithPathParamsAndResponseFields(
		String identifier,
		ParameterDescriptor[] pathParams,
		FieldDescriptor[] responseFields
	) {
		return RestAssuredRestDocumentationWrapper.document(
			identifier,
			pathParameters(pathParams),
			responseFields(responseFields)
		);
	}
}
