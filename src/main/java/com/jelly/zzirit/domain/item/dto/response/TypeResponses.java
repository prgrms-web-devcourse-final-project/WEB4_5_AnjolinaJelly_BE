// package com.jelly.zzirit.domain.item.dto;
//
// import java.util.List;
//
// import com.jelly.zzirit.domain.item.entity.Type;
//
// public record TypeResponses(
// 	List<TypeResponse> types
// ) {
//
// 	public static TypeResponses from(List<Type> types) {
// 		return new TypeResponses(
// 			types.stream()
// 				.map(TypeResponse::from)
// 				.toList()
// 		);
// 	}
//
// 	public record TypeResponse(
// 		Long typeId,
// 		String name
// 	) {
//
// 		public static TypeResponse from(Type type) {
// 			return new TypeResponse(type.getId(), type.getName());
// 		}
// 	}
// }
