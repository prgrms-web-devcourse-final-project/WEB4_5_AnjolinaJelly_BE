package com.jelly.zzirit.item.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.item.dto.response.ItemResponse;

@RestController
@RequestMapping("/items")
public class ItemController {

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<ItemResponse> getAll() {
		return List.of(new ItemResponse(1L, "test", "test.img", 10, 10000));
	}
}
