package com.sanele.recipebook.dto;

public record IngredientDto(
        Long id,
        String name,
        String quantity,
        String unit
) {
}
