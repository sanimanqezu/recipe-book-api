package com.sanele.recipebook.repository;

import com.sanele.recipebook.entity.Category;
import com.sanele.recipebook.entity.CuisineType;
import com.sanele.recipebook.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByCuisine(CuisineType cuisine);

    List<Recipe> findByCategory(Category category);

    List<Recipe> findByCuisineAndCategory(CuisineType cuisine, Category category);

    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Recipe> findByIngredientsNameContainingIgnoreCase(@Param("name") String name);
}
