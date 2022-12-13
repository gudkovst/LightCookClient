package view;

import application.JsonConverter;
import application.Requester;
import application.User;
import config.Config;
import dto.Ingredient;
import dto.Recipe;
import dto.Step;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ViewSwingRecipes extends JFrame implements IViewRecipes{
    private final JPanel entryField;
    private final User user;
    private final Requester requester;
    private final IView manager;
    private Recipe currentRecipe;
    private JButton currentMediaButton;

    public ViewSwingRecipes(User user, JPanel field, IView view){
        entryField = field;
        this.user = user;
        requester = new Requester();
        manager = view;
    }

    @Override
    public void findRecipes() {
        JTextField ingredientsField = new JTextField();
        ingredientsField.setToolTipText("Enter your ingredients separated by commas");
        ingredientsField.setHorizontalAlignment(JTextField.CENTER);

        JTextField toolsField = new JTextField();
        toolsField.setToolTipText("Enter your tools separated by commas");
        toolsField.setHorizontalAlignment(JTextField.CENTER);

        JCheckBox sort = new JCheckBox("Sort");
        sort.setHorizontalAlignment(JCheckBox.CENTER);

        JButton find = new JButton("Find");
        find.setMnemonic(KeyEvent.VK_ENTER);
        find.addActionListener(e -> {
            boolean sortFlag = sort.isSelected();
            String[] ingredients = ingredientsField.getText().split(",");
            String[] tools = toolsField.getText().split(",");
            String body = JsonConverter.makeJsonStandard(ingredients, tools);
            CompletableFuture<List<Recipe>> recipes = requester.requestRecipes(body);
            if (sortFlag)
                recipes = recipes.thenApply(this::sortRecipes);
            recipes.thenAccept(this::showRecipes);
            JLabel label = new JLabel("Wait, please. There is a search.");
            label.setHorizontalAlignment(JLabel.CENTER);
            javax.swing.SwingUtilities.invokeLater(() -> {
                entryField.add(label);
                manager.showPane();
            });
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(4, 1));
            entryField.add(ingredientsField);
            entryField.add(toolsField);
            entryField.add(sort);
            entryField.add(find);
            manager.showPane();
        });
    }

    private List<Recipe> sortRecipes(List<Recipe> recipes) {
        HashMap<String, Integer> marks = new HashMap<>();
        try {
            Scanner scanner = new Scanner(new File(Config.marksFile));
            while (scanner.hasNext()){
                String mark = scanner.nextLine();
                String[] record = mark.split(",");
                String recipe = record[0];
                Integer score = Integer.valueOf(record[1]);
                marks.put(recipe, score);
            }
        } catch (FileNotFoundException ignored) {}
        HashMap<Recipe, Integer> recipesMarks = new HashMap<>();
        for (Recipe recipe : recipes)
            recipesMarks.put(recipe, marks.get(recipe.getName()));

        ArrayList<Map.Entry<Recipe, Integer>> arrayMarks = new ArrayList<>(recipesMarks.entrySet());
        arrayMarks.sort(Map.Entry.comparingByValue());
        Collections.reverse(arrayMarks);
        List<Recipe> res = new ArrayList<>();
        for (Map.Entry<Recipe, Integer> record : arrayMarks)
            res.add(record.getKey());
        return res;
    }

    @Override
    public void showRecipes(List<Recipe> recipes) {
        ArrayList<JButton> recipeButtons = new ArrayList<>();
        for (Recipe recipe : recipes){
            JButton button = new JButton(recipe.getName());
            button.addActionListener(e -> showOneRecipe(recipe));
            recipeButtons.add(button);
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(recipeButtons.size(), 1));
            for (JButton button : recipeButtons)
                entryField.add(button);
            manager.showPane();
        });
    }

    @Override
    public void showOneRecipe(Recipe recipe){
        currentRecipe = recipe;
        JLabel name = new JLabel(recipe.getName());
        name.setHorizontalAlignment(JLabel.CENTER);

        JLabel time = new JLabel(String.format("Time for preparing: %d minutes", recipe.getCookTimeMins()));
        time.setHorizontalAlignment(JLabel.CENTER);

        String ingredientsString = "";
        for (Ingredient ingredient : recipe.getIngredients())
            ingredientsString += ingredient.getInfo();
        JLabel ingredients = new JLabel(ingredientsString);
        ingredients.setHorizontalAlignment(JLabel.CENTER);

        JButton mediaButton = new JButton("Show picture");
        mediaButton.addActionListener(e -> showMedia(recipe.getMedia()));
        currentMediaButton = mediaButton;

        JButton stepButton = new JButton("Show step by step");
        stepButton.addActionListener(e -> showStep(recipe.getSteps(), 0));

        JButton likeButton = new JButton("Like");
        likeButton.addActionListener(e -> likeRecipe(recipe));

        JButton reviewButton = new JButton("Review");
        reviewButton.addActionListener(e -> writeComment());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            if (user.isAuth())
                manager.authPage();
            else
                manager.startPage();
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(8, 1));
            entryField.add(name);
            entryField.add(time);
            entryField.add(ingredients);
            entryField.add(mediaButton);
            entryField.add(stepButton);
            entryField.add(likeButton);
            entryField.add(reviewButton);
            entryField.add(exitButton);
            manager.showPane();
        });
    }

    @Override
    public void writeComment() {
        JTextField textField = new JTextField();
        textField.setToolTipText("Enter comment");
        textField.setHorizontalAlignment(JTextField.CENTER);

        JTextField gradeField = new JTextField();
        gradeField.setToolTipText("Rate from 0 to 10");
        gradeField.setHorizontalAlignment(JTextField.CENTER);

        JButton button = new JButton("Send");
        button.setMnemonic(KeyEvent.VK_ENTER);
        button.addActionListener(e -> {
            int grade;
            try{
                grade = Integer.parseInt(gradeField.getText());
            } catch (NumberFormatException exc){
                grade = 0;
            }
            if (grade < 0 || grade > 10)
                grade = 0;
            String body = JsonConverter.makeJsonReview(currentRecipe.getId(), textField.getText(), grade, user);
            requester.sendComment(body);
            manager.authPage();
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(3, 1));
            entryField.add(textField);
            entryField.add(gradeField);
            entryField.add(button);
            manager.showPane();
        });
    }

    private void likeRecipe(Recipe recipe){
        HashMap<String, Integer> marks = new HashMap<>();
        try {
            Scanner scanner = new Scanner(new File(Config.marksFile));
            while (scanner.hasNext()){
                String mark = scanner.nextLine();
                String[] record = mark.split(",");
                String rec = record[0];
                Integer score = Integer.valueOf(record[1]);
                marks.put(rec, score);
            }
            scanner.close();
        } catch (FileNotFoundException ignored) {}
        String rec = recipe.getName();
        Integer score = marks.get(rec) != null? marks.get(rec) + 1 : 1;
        marks.put(rec, score);
        try {
            FileWriter writer = new FileWriter(Config.marksFile);
            for (String name : marks.keySet()){
                writer.write(name + ", " + marks.get(name) + "\n");
            }
            writer.close();
        } catch (IOException ignored) {}
    }

    @Override
    public void showStep(Step[] steps, int num){
        Step step = steps[num];
        JButton prevButton, nextButton;
        if (num == 0){
            prevButton = new JButton("end");
            prevButton.addActionListener(e -> showOneRecipe(currentRecipe));
        }
        else{
            prevButton = new JButton("Previous step");
            prevButton.addActionListener(e -> showStep(steps, num - 1));
        }
        if (num == steps.length - 1){
            nextButton = new JButton("end");
            nextButton.addActionListener(e -> showOneRecipe(currentRecipe));
        }
        else{
            nextButton = new JButton("Next step");
            nextButton.addActionListener(e -> showStep(steps, num + 1));
        }
        JLabel description = new JLabel(step.getDescription());
        description.setHorizontalAlignment(JLabel.CENTER);

        JLabel wait = new JLabel("Wait " + step.getWaitTimeMins() + "minutes, please");
        wait.setHorizontalAlignment(JLabel.CENTER);

        JButton mediaButton = new JButton("Show picture");
        mediaButton.addActionListener(e -> showMedia(step.getMedia()));
        currentMediaButton = mediaButton;

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(5, 1));
            entryField.add(description);
            entryField.add(wait);
            entryField.add(mediaButton);
            entryField.add(prevButton);
            entryField.add(nextButton);
            manager.showPane();
        });
    }

    private void showMedia(int id){
        String mediaFile = requester.requestMedia(id);
        JLabel picture;
        if (mediaFile == null){
            picture = new JLabel("Picture not found");
            picture.setHorizontalAlignment(JLabel.CENTER);
        }
        else{
            picture = new JLabel(new ImageIcon(mediaFile));
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.remove(currentMediaButton);
            entryField.add(picture);
            manager.showPane();
        });
    }
}
