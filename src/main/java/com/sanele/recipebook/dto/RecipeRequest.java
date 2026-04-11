package com.sanele.recipebook.dto;

import com.sanele.recipebook.entity.Category;
import com.sanele.recipebook.entity.CuisineType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecipeRequest(
        @NotBlank(message = "Title must not be blank")
        String title,

        String description,

        @NotNull(message = "Cuisine type must not be null")
        CuisineType cuisine,

        @NotNull(message = "Category must not be null")
        Category category,

        @Min(value = 0, message = "Prep time must be zero or greater")
        int prepTimeMinutes,

        @Min(value = 0, message = "Cook time must be zero or greater")
        int cookTimeMinutes,

        @Min(value = 1, message = "Servings must be at least 1")
        int servings,

        String instructions,

        @Valid
        List<IngredientDto> ingredients
) {
}
