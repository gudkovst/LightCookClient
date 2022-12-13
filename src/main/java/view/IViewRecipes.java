package view;

import dto.Recipe;
import dto.Step;

import java.util.List;

public interface IViewRecipes {
    public void findRecipes();
    public void showRecipes(List<Recipe> recipes);
    public void showOneRecipe(Recipe recipe);
    public void showStep(Step[] steps, int num);
    public void writeComment();
}
