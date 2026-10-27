package com.sanele.recipebook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.recipebook.dto.IngredientDto;
import com.sanele.recipebook.dto.RecipeRequest;
import com.sanele.recipebook.dto.RecipeResponse;
import com.sanele.recipebook.entity.Category;
import com.sanele.recipebook.entity.CuisineType;
import com.sanele.recipebook.exception.ResourceNotFoundException;
import com.sanele.recipebook.service.RecipeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeService recipeService;

    private RecipeResponse sampleResponse() {
        return new RecipeResponse(
                1L,
                "Grilled Chicken",
                "A simple grilled chicken recipe",
                CuisineType.AMERICAN,
                Category.DINNER,
                15,
                30,
                4,
                "Season and grill chicken for 30 minutes.",
                LocalDateTime.now(),
                List.of(new IngredientDto(1L, "chicken breast", "500", "g"))
        );
    }

    private RecipeRequest sampleRequest() {
        return new RecipeRequest(
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
    @DisplayName("POST /api/recipes - should return 201 with the created recipe")
    void createRecipe_shouldReturnCreated() throws Exception {
        when(recipeService.createRecipe(any(RecipeRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Grilled Chicken"))
                .andExpect(jsonPath("$.ingredients[0].name").value("chicken breast"));

        verify(recipeService).createRecipe(any(RecipeRequest.class));
    }

    @Test
    @DisplayName("POST /api/recipes - should return 400 when title is blank")
    void createRecipe_withBlankTitle_shouldReturnBadRequest() throws Exception {
        RecipeRequest invalid = new RecipeRequest(
                "  ",
                "desc",
                CuisineType.AMERICAN,
                Category.DINNER,
                15,
                30,
                4,
                "instructions",
                List.of()
        );

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").exists());

        verify(recipeService, org.mockito.Mockito.never()).createRecipe(any());
    }

    @Test
    @DisplayName("POST /api/recipes - should return 400 when servings is below minimum")
    void createRecipe_withInvalidServings_shouldReturnBadRequest() throws Exception {
        RecipeRequest invalid = new RecipeRequest(
                "Soup",
                "desc",
                CuisineType.AMERICAN,
                Category.DINNER,
                15,
                30,
                0,
                "instructions",
                List.of()
        );

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.servings").exists());
    }

    @Test
    @DisplayName("GET /api/recipes - should return the list of recipes")
    void getAllRecipes_shouldReturnList() throws Exception {
        when(recipeService.getAllRecipes(null, null)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Grilled Chicken"));

        verify(recipeService).getAllRecipes(null, null);
    }

    @Test
    @DisplayName("GET /api/recipes - should pass cuisine and category filters to the service")
    void getAllRecipes_withFilters_shouldDelegate() throws Exception {
        when(recipeService.getAllRecipes(CuisineType.ITALIAN, Category.DINNER))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/recipes")
                        .param("cuisine", "ITALIAN")
                        .param("category", "DINNER"))
                .andExpect(status().isOk());

        verify(recipeService).getAllRecipes(CuisineType.ITALIAN, Category.DINNER);
    }

    @Test
    @DisplayName("GET /api/recipes/{id} - should return the recipe when found")
    void getRecipeById_whenFound_shouldReturnOk() throws Exception {
        when(recipeService.getRecipeById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/recipes/{id} - should return 404 when the recipe is not found")
    void getRecipeById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(recipeService.getRecipeById(99L))
                .thenThrow(new ResourceNotFoundException("Recipe", 99L));

        mockMvc.perform(get("/api/recipes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Recipe not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/recipes/{id} - should return the updated recipe")
    void updateRecipe_shouldReturnOk() throws Exception {
        when(recipeService.updateRecipe(eq(1L), any(RecipeRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/recipes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Grilled Chicken"));

        verify(recipeService).updateRecipe(eq(1L), any(RecipeRequest.class));
    }

    @Test
    @DisplayName("PUT /api/recipes/{id} - should return 404 when updating a missing recipe")
    void updateRecipe_whenNotFound_shouldReturnNotFound() throws Exception {
        when(recipeService.updateRecipe(eq(99L), any(RecipeRequest.class)))
                .thenThrow(new ResourceNotFoundException("Recipe", 99L));

        mockMvc.perform(put("/api/recipes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/recipes/{id} - should return 204 on success")
    void deleteRecipe_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/recipes/1"))
                .andExpect(status().isNoContent());

        verify(recipeService).deleteRecipe(1L);
    }

    @Test
    @DisplayName("DELETE /api/recipes/{id} - should return 404 when the recipe is missing")
    void deleteRecipe_whenNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Recipe", 99L))
                .when(recipeService).deleteRecipe(99L);

        mockMvc.perform(delete("/api/recipes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/recipes/search - should return recipes matching the ingredient")
    void searchByIngredient_shouldReturnMatches() throws Exception {
        when(recipeService.searchByIngredient("chicken")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/recipes/search").param("ingredient", "chicken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Grilled Chicken"));

        verify(recipeService).searchByIngredient("chicken");
    }

    @Test
    @DisplayName("GET /api/recipes/search - should return 400 when the ingredient param is missing")
    void searchByIngredient_withoutParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/recipes/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ingredient")));

        verify(recipeService, org.mockito.Mockito.never()).searchByIngredient(any());
    }

    @Test
    @DisplayName("GET /api/recipes/meal-plan - should return the generated meal plan")
    void getMealPlan_shouldReturnRecipes() throws Exception {
        when(recipeService.getMealPlan()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/recipes/meal-plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(recipeService).getMealPlan();
    }
}
