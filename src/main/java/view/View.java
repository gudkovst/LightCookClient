package view;

import dto.Recipe;
import dto.Step;

import java.util.List;

public interface View {
    public void start();
    public void startPage();
    public void authPage();
    public void authorization();
    public void registration();
    public void findRecipes();
    public void showRecipes(List<Recipe> recipes);
    public void showOneRecipe(Recipe recipe);
    public void showStep(Step[] steps, int num);
    public void setTools();
    public void setIngredients();
    public void writeComment();
}
