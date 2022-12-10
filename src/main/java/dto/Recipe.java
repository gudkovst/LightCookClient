package dto;

public class Recipe {
    private final int id;
    private final String name;
    private final int media;
    private final int cookTimeMins;
    private final Ingredient[] ingredients;
    private final Step[] steps;

    public Recipe(int id, String name, int media, int cook_time_mins, Ingredient[] ingredients, Step[] steps){
        this.id = id;
        this.name = name;
        this.media = media;
        this.cookTimeMins = cook_time_mins;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMedia() {
        return media;
    }

    public int getCookTimeMins() {
        return cookTimeMins;
    }

    public Ingredient[] getIngredients() {
        return ingredients;
    }

    public Step[] getSteps() {
        return steps;
    }
}
