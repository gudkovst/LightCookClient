package application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Ingredient;
import dto.Recipe;
import dto.Standard;
import dto.Step;

import java.util.ArrayList;
import java.util.List;

public class JsonConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<Recipe> parseRecipes(String json){
        ArrayList<Recipe> res = new ArrayList<>();
        try {
            JsonNode jsonNode = objectMapper.readValue(json, JsonNode.class);
            for (JsonNode recipe : jsonNode){
                int id = recipe.get("id").asInt(0);
                String name = recipe.get("name").asText("");
                int media = recipe.get("media").get("media_id").asInt();
                int time = recipe.get("cook_time_mins").asInt(0);
                JsonNode listIngredients = recipe.get("ingredients");
                ArrayList<Ingredient> ingredients = new ArrayList<>();
                for (JsonNode ingredient : listIngredients){
                    String nameIngredient = ingredient.get("name").asText("");
                    String unit = ingredient.get("measure_unit_name").asText("");
                    int count = ingredient.get("count").asInt(0);
                    ingredients.add(new Ingredient(nameIngredient, unit, count));
                }
                JsonNode listSteps = recipe.get("steps");
                ArrayList<Step> steps = new ArrayList<>();
                for (JsonNode step : listSteps){
                    int stepMedia = step.get("media").get("media_id").asInt();
                    String description = step.get("description").asText("");
                    int stepTime = step.get("wait_time_mins").asInt(0);
                    steps.add(new Step(stepMedia, description, stepTime));
                }
                res.add(new Recipe(id, name, media, time, ingredients.toArray(new Ingredient[0]),
                        steps.toArray(new Step[0])));
            }
            return res;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static Standard parseAuth(String json){
        try{
            JsonNode jsonNode = objectMapper.readValue(json, JsonNode.class);
            JsonNode ingredients = jsonNode.get("ingredients");
            ArrayList<String> listIngredients = new ArrayList<>();
            for (JsonNode ingredient : ingredients)
                listIngredients.add(ingredient.asText());
            JsonNode tools = jsonNode.get("tools");
            ArrayList<String> listTools = new ArrayList<>();
            for (JsonNode tool : tools)
                listTools.add(tool.asText());
            return new Standard(listIngredients.toArray(new String[0]), listTools.toArray(new String[0]));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static String makeJsonStandard(String[] ingredients, String[] tools){
        String json = "{\n\t\"ingredients\": [\n\t\t";
        for (String ingredient : ingredients){
            json += "{\"name\": " + ingredient + "}, ";
        }
        json = json.substring(0, json.length() - 2) + "\n\t],";
        json += "\n\t\"tools\": [\n\t\t";
        for (String tool : tools){
            json += "{\"name\": " + tool + "}, ";
        }
        json = json.substring(0, json.length() - 2) + "\n\t]\n}";
        return json;
    }

    public static String makeJsonReview(int id, String review, User user){
        return  "{\n\t\"id\": " + id + ",\n\t\"review\": " + review + "\n\t\"login\": "
                + user.getLogin() + ",\n\t\"pass\": " + user.getPass() + "\n}";
    }
}
