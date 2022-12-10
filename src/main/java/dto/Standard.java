package dto;

public class Standard {
    private final String[] tools;
    private final String[] ingredients;

    public Standard(String[] ingredients, String[] tools){
        this.ingredients = ingredients;
        this.tools = tools;
    }

    public String[] getTools() {
        return tools;
    }

    public String[] getIngredients() {
        return ingredients;
    }
}
