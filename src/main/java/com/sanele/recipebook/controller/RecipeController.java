package com.sanele.recipebook.controller;

import com.sanele.recipebook.dto.RecipeRequest;
import com.sanele.recipebook.dto.RecipeResponse;
import com.sanele.recipebook.entity.Category;
import com.sanele.recipebook.entity.CuisineType;
import com.sanele.recipebook.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody RecipeRequest request) {
        RecipeResponse response = recipeService.createRecipe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponse>> getAllRecipes(
            @RequestParam(required = false) CuisineType cuisine,
            @RequestParam(required = false) Category category) {
        return ResponseEntity.ok(recipeService.getAllRecipes(cuisine, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeResponse>> searchByIngredient(
            @RequestParam String ingredient) {
        return ResponseEntity.ok(recipeService.searchByIngredient(ingredient));
    }

    @GetMapping("/meal-plan")
    public ResponseEntity<List<RecipeResponse>> getMealPlan() {
        return ResponseEntity.ok(recipeService.getMealPlan());
    }
}
