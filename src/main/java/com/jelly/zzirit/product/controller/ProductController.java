package com.jelly.zzirit.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.product.dto.response.ProductResponse;

@RestController
@RequestMapping("/products")
public class ProductController {

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<ProductResponse> getAll() {
		return List.of(new ProductResponse(1L, "test", "test.img", 10, 10000));
	}
}
