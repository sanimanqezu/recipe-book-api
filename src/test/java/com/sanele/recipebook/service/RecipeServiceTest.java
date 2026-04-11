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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe sampleRecipe;
    private RecipeRequest sampleRequest;

    @BeforeEach
    void setUp() {
        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("chicken breast")
                .quantity("500")
                .unit("g")
                .build();

        sampleRecipe = Recipe.builder()
                .id(1L)
                .title("Grilled Chicken")
                .description("A simple grilled chicken recipe")
                .cuisine(CuisineType.AMERICAN)
                .category(Category.DINNER)
                .prepTimeMinutes(15)
                .cookTimeMinutes(30)
                .servings(4)
                .instructions("Season and grill chicken for 30 minutes.")
                .createdAt(LocalDateTime.now())
                .build();

        ingredient.setRecipe(sampleRecipe);
        sampleRecipe.getIngredients().add(ingredient);

        sampleRequest = new RecipeRequest(
                "Grilled Chicken",
                "A simple grilled chicken recipe",
                CuisineType.AMERICAN,
                Category.DINNER,
                15,
                30,
                4,
                "Season and grill chicken for 30 minutes.",
                List.of(new IngredientDto(null, "chicken breast", "500", "g"))
        );
    }

    @Test
    @DisplayName("createRecipe - should save and return a RecipeResponse")
    void createRecipe_shouldSaveAndReturnResponse() {
        when(recipeRepository.save(any(Recipe.class))).thenReturn(sampleRecipe);

        RecipeResponse response = recipeService.createRecipe(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Grilled Chicken");
        assertThat(response.cuisine()).isEqualTo(CuisineType.AMERICAN);
        assertThat(response.category()).isEqualTo(Category.DINNER);
        assertThat(response.servings()).isEqualTo(4);
        assertThat(response.ingredients()).hasSize(1);
        assertThat(response.ingredients().get(0).name()).isEqualTo("chicken breast");

        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }

    @Test
    @DisplayName("getRecipeById - should return response when recipe exists")
    void getRecipeById_whenFound_shouldReturnResponse() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(sampleRecipe));

        RecipeResponse response = recipeService.getRecipeById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Grilled Chicken");

        verify(recipeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getRecipeById - should throw ResourceNotFoundException when recipe does not exist")
    void getRecipeById_whenNotFound_shouldThrowException() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getRecipeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(recipeRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("getAllRecipes - without filters should return all recipes")
    void getAllRecipes_withNoFilters_shouldReturnAll() {
        when(recipeRepository.findAll()).thenReturn(List.of(sampleRecipe));

        List<RecipeResponse> responses = recipeService.getAllRecipes(null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("Grilled Chicken");

        verify(recipeRepository, times(1)).findAll();
        verify(recipeRepository, never()).findByCuisine(any());
        verify(recipeRepository, never()).findByCategory(any());
    }

    @Test
    @DisplayName("getAllRecipes - with cuisine filter should delegate to findByCuisine")
    void getAllRecipes_withCuisineFilter_shouldFilterByCuisine() {
        when(recipeRepository.findByCuisine(CuisineType.AMERICAN)).thenReturn(List.of(sampleRecipe));

        List<RecipeResponse> responses = recipeService.getAllRecipes(CuisineType.AMERICAN, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).cuisine()).isEqualTo(CuisineType.AMERICAN);

        verify(recipeRepository, times(1)).findByCuisine(CuisineType.AMERICAN);
    }

    @Test
    @DisplayName("getAllRecipes - with cuisine and category filters should delegate to findByCuisineAndCategory")
    void getAllRecipes_withBothFilters_shouldFilterByCuisineAndCategory() {
        when(recipeRepository.findByCuisineAndCategory(CuisineType.AMERICAN, Category.DINNER))
                .thenReturn(List.of(sampleRecipe));

        List<RecipeResponse> responses = recipeService.getAllRecipes(CuisineType.AMERICAN, Category.DINNER);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).category()).isEqualTo(Category.DINNER);

        verify(recipeRepository, times(1)).findByCuisineAndCategory(CuisineType.AMERICAN, Category.DINNER);
    }

    @Test
    @DisplayName("searchByIngredient - should return recipes containing the ingredient")
    void searchByIngredient_shouldReturnMatchingRecipes() {
        when(recipeRepository.findByIngredientsNameContainingIgnoreCase("chicken"))
                .thenReturn(List.of(sampleRecipe));

        List<RecipeResponse> responses = recipeService.searchByIngredient("chicken");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).ingredients())
                .anyMatch(i -> i.name().toLowerCase().contains("chicken"));

        verify(recipeRepository, times(1)).findByIngredientsNameContainingIgnoreCase("chicken");
    }

    @Test
    @DisplayName("deleteRecipe - should delete existing recipe without error")
    void deleteRecipe_whenExists_shouldDelete() {
        when(recipeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(recipeRepository).deleteById(1L);

        assertThatCode(() -> recipeService.deleteRecipe(1L)).doesNotThrowAnyException();

        verify(recipeRepository, times(1)).existsById(1L);
        verify(recipeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteRecipe - should throw ResourceNotFoundException when recipe does not exist")
    void deleteRecipe_whenNotExists_shouldThrowException() {
        when(recipeRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> recipeService.deleteRecipe(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");

        verify(recipeRepository, times(1)).existsById(42L);
        verify(recipeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getMealPlan - should return up to 3 random recipes")
    void getMealPlan_shouldReturnUpToThreeRecipes() {
        Recipe recipe2 = Recipe.builder()
                .id(2L)
                .title("Pasta Carbonara")
                .cuisine(CuisineType.ITALIAN)
                .category(Category.DINNER)
                .prepTimeMinutes(10)
                .cookTimeMinutes(20)
                .servings(2)
                .createdAt(LocalDateTime.now())
                .build();

        Recipe recipe3 = Recipe.builder()
                .id(3L)
                .title("Jollof Rice")
                .cuisine(CuisineType.AFRICAN)
                .category(Category.LUNCH)
                .prepTimeMinutes(20)
                .cookTimeMinutes(40)
                .servings(6)
                .createdAt(LocalDateTime.now())
                .build();

        when(recipeRepository.findAll()).thenReturn(new ArrayList<>(List.of(sampleRecipe, recipe2, recipe3)));

        List<RecipeResponse> mealPlan = recipeService.getMealPlan();

        assertThat(mealPlan).isNotNull();
        assertThat(mealPlan.size()).isLessThanOrEqualTo(3);
        assertThat(mealPlan).isNotEmpty();

        verify(recipeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("updateRecipe - should update and return the updated recipe")
    void updateRecipe_whenExists_shouldUpdateAndReturn() {
        RecipeRequest updateRequest = new RecipeRequest(
                "Updated Chicken",
                "Updated description",
                CuisineType.ASIAN,
                Category.LUNCH,
                10,
                25,
                2,
                "Updated instructions.",
                List.of(new IngredientDto(null, "soy sauce", "2", "tbsp"))
        );

        Recipe updatedRecipe = Recipe.builder()
                .id(1L)
                .title("Updated Chicken")
                .description("Updated description")
                .cuisine(CuisineType.ASIAN)
                .category(Category.LUNCH)
                .prepTimeMinutes(10)
                .cookTimeMinutes(25)
                .servings(2)
                .instructions("Updated instructions.")
                .createdAt(sampleRecipe.getCreatedAt())
                .build();

        Ingredient updatedIngredient = Ingredient.builder()
                .id(2L)
                .name("soy sauce")
                .quantity("2")
                .unit("tbsp")
                .recipe(updatedRecipe)
                .build();
        updatedRecipe.getIngredients().add(updatedIngredient);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(sampleRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(updatedRecipe);

        RecipeResponse response = recipeService.updateRecipe(1L, updateRequest);

        assertThat(response.title()).isEqualTo("Updated Chicken");
        assertThat(response.cuisine()).isEqualTo(CuisineType.ASIAN);
        assertThat(response.category()).isEqualTo(Category.LUNCH);

        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }
}
