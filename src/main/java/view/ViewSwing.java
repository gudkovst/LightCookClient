package view;

import application.JsonConverter;
import application.User;
import config.Config;
import dto.Ingredient;
import dto.Recipe;
import dto.Standard;
import dto.Step;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ViewSwing extends JFrame implements View, Runnable {
    private final User user;
    private final JPanel entryField;
    private Recipe currentRecipe;
    private JButton currentMediaButton;

    public ViewSwing(User user){
        this.user = user;
        entryField = new JPanel();
    }

    @Override
    public void start(){
        javax.swing.SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        init();
        startPage();
    }

    private void init(){
        setSize(Config.WIDTH, Config.HEIGHT);
        getContentPane().setBackground(Config.color); // Set background color
        setDefaultCloseOperation(EXIT_ON_CLOSE); // When "(X)" clicked, process is being killed
        setTitle("LightCook"); // Set title
        setResizable(true);
        setVisible(true); // Show everything
    }

    @Override
    public void startPage() {
        JButton buttonAuth = new JButton("Authorization");
        buttonAuth.addActionListener(e -> authorization());
        JButton buttonFind = new JButton("Find recipes");
        buttonFind.addActionListener(e -> findRecipes());

        entryField.removeAll();
        entryField.setLayout(new GridLayout(2, 1));
        entryField.add(buttonAuth);
        entryField.add(buttonFind);
        setContentPane(entryField);
    }

    @Override
    public void authPage(){
        JButton find = new JButton("Find recipes");
        find.addActionListener(e -> findRecipes());
        JButton changeTools = new JButton("Change tools");
        changeTools.addActionListener(e -> setTools());
        JButton changeIngred = new JButton("Change ingredients");
        changeIngred.addActionListener(e -> setIngredients());

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(3, 1));
            entryField.add(find);
            entryField.add(changeTools);
            entryField.add(changeIngred);
            setContentPane(entryField);
        });
    }

    @Override
    public void authorization() {
        JTextField loginField = new JTextField();
        loginField.setToolTipText("login");
        loginField.setHorizontalAlignment(JTextField.CENTER);

        JTextField passField = new JTextField();
        passField.setToolTipText("password");
        passField.setHorizontalAlignment(JTextField.CENTER);

        JButton authButton = new JButton("Sign in");
        authButton.setMnemonic(KeyEvent.VK_ENTER);
        authButton.addActionListener(e -> {
            if (user.isAuth())
                return;
            String login = loginField.getText();
            String pass = passField.getText();
            CompletableFuture<HttpResponse<String>> auth = user.getRequester().requestAuth(login, pass);
            try {
                Integer status = auth.thenApply(HttpResponse::statusCode).get();
                if (status == 200){
                    auth.thenApply(HttpResponse::body).thenApply(JsonConverter::parseAuth).
                            thenAccept(this::setUserStandard);
                    authPage();
                }
                else {
                    errorSign("Error of authorization");
                }
            } catch (InterruptedException | ExecutionException ignored) {
                errorSign("Error of authorization");
            }
        });

        JButton regButton = new JButton("Registration");
        regButton.addActionListener(e -> registration());

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(4, 1));
            entryField.add(loginField);
            entryField.add(passField);
            entryField.add(authButton);
            entryField.add(regButton);
            setContentPane(entryField);
        });
    }

    private void errorSign(String msg){
        JLabel label = new JLabel(msg);
        label.setHorizontalAlignment(JLabel.CENTER);
        JButton button = new JButton("OK");
        button.setMnemonic(KeyEvent.VK_ENTER);
        button.addActionListener(event -> startPage());

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(2, 1));
            entryField.add(label);
            entryField.add(button);
        });
    }

    private void setUserStandard(Standard standard){
        user.setStandard(standard);
    }

    @Override
    public void registration() {
        JTextField loginField = new JTextField();
        loginField.setToolTipText("login");
        loginField.setHorizontalAlignment(JTextField.CENTER);

        JTextField passField = new JTextField();
        passField.setToolTipText("password");
        passField.setHorizontalAlignment(JTextField.CENTER);

        JButton regButton = new JButton("Sign up");
        regButton.setMnemonic(KeyEvent.VK_ENTER);
        regButton.addActionListener(e -> {
            if (user.isAuth())
                return;
            String login = loginField.getText();
            String pass = passField.getText();
            CompletableFuture<Integer> status = user.getRequester().requestReg(login, pass);
            status.thenAccept(this::handleRegistration);
            user.authorization(login, pass);
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(3, 1));
            entryField.add(loginField);
            entryField.add(passField);
            entryField.add(regButton);
            setContentPane(entryField);
        });
    }

    private void handleRegistration(Integer status){
        if (status == 200){
            authPage();
            return;
        }
        user.authorization(null, null);
        errorSign("This user already exists");
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
            CompletableFuture<List<Recipe>> recipes = user.getRequester().requestRecipes(body);
            if (sortFlag)
                recipes = recipes.thenApply(this::sortRecipes);
            recipes.thenAccept(this::showRecipes);
            JLabel label = new JLabel("Wait, please. There is a search.");
            label.setHorizontalAlignment(JLabel.CENTER);
            javax.swing.SwingUtilities.invokeLater(() -> entryField.add(label));
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(4, 1));
            entryField.add(ingredientsField);
            entryField.add(toolsField);
            entryField.add(sort);
            entryField.add(find);
            setContentPane(entryField);
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
            setContentPane(entryField);
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
                authPage();
            else
                startPage();
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
            setContentPane(entryField);
        });
    }

    private void showMedia(int id){
        String mediaFile = user.getRequester().requestMedia(id);
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
            setContentPane(entryField);
        });
    }

    @Override
    public void setTools() {
        JLabel label = new JLabel(user.getStandardTools());
        label.setHorizontalAlignment(JLabel.CENTER);

        JTextField textField = new JTextField();
        textField.setToolTipText("Enter a new set of standard tools separated by commas");
        textField.setHorizontalAlignment(JTextField.CENTER);

        JButton button = new JButton("OK");
        button.setMnemonic(KeyEvent.VK_ENTER);
        button.addActionListener(e -> {
            String[] tools = textField.getText().split(", ");
            String[] ingredients = new String[0];
            String body = JsonConverter.makeJsonStandard(ingredients, tools);
            user.getRequester().requestSetTools(body, user.getLogin(), user.getPass());
            user.setStandardTools(tools);
            authPage();
        });

        JButton retButton = new JButton("return");
        retButton.addActionListener(e -> authPage());

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(4, 1));
            entryField.add(label);
            entryField.add(textField);
            entryField.add(button);
            entryField.add(retButton);
            setContentPane(entryField);
        });
    }

    @Override
    public void setIngredients() {
        JLabel label = new JLabel(user.getStandardIngredients());
        label.setHorizontalAlignment(JLabel.CENTER);

        JTextField textField = new JTextField();
        textField.setToolTipText("Enter a new set of standard ingredients separated by commas");
        textField.setHorizontalAlignment(JTextField.CENTER);

        JButton button = new JButton("OK");
        button.setMnemonic(KeyEvent.VK_ENTER);
        button.addActionListener(e -> {
            String[] ingredients = textField.getText().split(", ");
            String[] tools = new String[0];
            String body = JsonConverter.makeJsonStandard(ingredients, tools);
            user.getRequester().requestSetIngredients(body, user.getLogin(), user.getPass());
            user.setStandardIngredients(ingredients);
            authPage();
        });

        JButton retButton = new JButton("return");
        retButton.addActionListener(e -> authPage());

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(4, 1));
            entryField.add(label);
            entryField.add(textField);
            entryField.add(button);
            entryField.add(retButton);
            setContentPane(entryField);
        });
    }

    @Override
    public void writeComment() {
        JTextField textField = new JTextField();
        textField.setToolTipText("Enter comment");
        textField.setHorizontalAlignment(JTextField.CENTER);

        JButton button = new JButton("Send");
        button.setMnemonic(KeyEvent.VK_ENTER);
        button.addActionListener(e -> {
            String body = JsonConverter.makeJsonReview(currentRecipe.getId(), textField.getText(), user);
            user.getRequester().sendComment(body);
            authPage();
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(2, 1));
            entryField.add(textField);
            entryField.add(button);
            setContentPane(entryField);
        });
    }
}
