package com.sanele.recipebook.dto;

import com.sanele.recipebook.entity.Category;
import com.sanele.recipebook.entity.CuisineType;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeResponse(
        Long id,
        String title,
        String description,
        CuisineType cuisine,
        Category category,
        int prepTimeMinutes,
        int cookTimeMinutes,
        int servings,
        String instructions,
        LocalDateTime createdAt,
        List<IngredientDto> ingredients
) {
}
