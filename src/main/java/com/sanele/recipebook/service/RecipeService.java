package com.sanele.recipebook.service;

import com.sanele.recipebook.dto.IngredientDto;
import com.sanele.recipebook.dto.RecipeRequest;
import com.sanele.recipebook.dto.RecipeResponse;
import com.sanele.recipebook.entity.Category;
import com.sanele.recipebook.entity.CuisineType;
import com.sanele.recipebook.entity.Ingredient;
import com.sanele.recipebook.entity.Recipe;
import com.sanele.recipebook.exception.ResourceNotFoundException;
import com.sanele.recipebook.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    private static final int MEAL_PLAN_SIZE = 3;

    private final RecipeRepository recipeRepository;

    @Transactional
    public RecipeResponse createRecipe(RecipeRequest request) {
        Recipe recipe = Recipe.builder()
                .title(request.title())
                .description(request.description())
                .cuisine(request.cuisine())
                .category(request.category())
                .prepTimeMinutes(request.prepTimeMinutes())
                .cookTimeMinutes(request.cookTimeMinutes())
                .servings(request.servings())
                .instructions(request.instructions())
                .build();

        if (request.ingredients() != null) {
            for (IngredientDto dto : request.ingredients()) {
                Ingredient ingredient = Ingredient.builder()
                        .name(dto.name())
                        .quantity(dto.quantity())
                        .unit(dto.unit())
                        .build();
                recipe.addIngredient(ingredient);
            }
        }

        Recipe saved = recipeRepository.save(recipe);
        return toResponse(saved);
    }

    public List<RecipeResponse> getAllRecipes(CuisineType cuisine, Category category) {
        List<Recipe> recipes;

        if (cuisine != null && category != null) {
            recipes = recipeRepository.findByCuisineAndCategory(cuisine, category);
        } else if (cuisine != null) {
            recipes = recipeRepository.findByCuisine(cuisine);
        } else if (category != null) {
            recipes = recipeRepository.findByCategory(category);
        } else {
            recipes = recipeRepository.findAll();
        }

        return recipes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RecipeResponse getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", id));
        return toResponse(recipe);
    }

    @Transactional
    public RecipeResponse updateRecipe(Long id, RecipeRequest request) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", id));

        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setCuisine(request.cuisine());
        recipe.setCategory(request.category());
        recipe.setPrepTimeMinutes(request.prepTimeMinutes());
        recipe.setCookTimeMinutes(request.cookTimeMinutes());
        recipe.setServings(request.servings());
        recipe.setInstructions(request.instructions());

        recipe.clearIngredients();

        if (request.ingredients() != null) {
            for (IngredientDto dto : request.ingredients()) {
                Ingredient ingredient = Ingredient.builder()
                        .name(dto.name())
                        .quantity(dto.quantity())
                        .unit(dto.unit())
                        .build();
                recipe.addIngredient(ingredient);
            }
        }

        Recipe updated = recipeRepository.save(recipe);
        return toResponse(updated);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recipe", id);
        }
        recipeRepository.deleteById(id);
    }

    public List<RecipeResponse> searchByIngredient(String ingredientName) {
        return recipeRepository.findByIngredientsNameContainingIgnoreCase(ingredientName)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RecipeResponse> getMealPlan() {
        List<Recipe> all = new ArrayList<>(recipeRepository.findAll());
        Collections.shuffle(all);
        List<Recipe> selected = all.size() <= MEAL_PLAN_SIZE ? all : all.subList(0, MEAL_PLAN_SIZE);
        return selected.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RecipeResponse toResponse(Recipe recipe) {
        List<IngredientDto> ingredientDtos = recipe.getIngredients().stream()
                .map(i -> new IngredientDto(i.getId(), i.getName(), i.getQuantity(), i.getUnit()))
                .collect(Collectors.toList());

        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getCuisine(),
                recipe.getCategory(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                recipe.getServings(),
                recipe.getInstructions(),
                recipe.getCreatedAt(),
                ingredientDtos
        );
    }
}
