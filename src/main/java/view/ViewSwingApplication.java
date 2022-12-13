package view;

import application.JsonConverter;
import application.Requester;
import application.User;
import dto.Standard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ViewSwingApplication extends JFrame implements IViewApplication {
    private final User user;
    private final JPanel entryField;
    private final Requester requester;
    private final IView manager;

    public ViewSwingApplication(User user, JPanel field, IView view){
        requester = new Requester();
        this.user = user;
        entryField = field;
        manager = view;
    }

    @Override
    public void startPage() {
        JButton buttonAuth = new JButton("Authorization");
        buttonAuth.addActionListener(e -> authorization());
        JButton buttonFind = new JButton("Find recipes");
        buttonFind.addActionListener(e -> manager.findRecipes());

        entryField.removeAll();
        entryField.setLayout(new GridLayout(2, 1));
        entryField.add(buttonAuth);
        entryField.add(buttonFind);
        manager.showPane();
    }

    @Override
    public void authPage(){
        JButton find = new JButton("Find recipes");
        find.addActionListener(e -> manager.findRecipes());
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
            manager.showPane();
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
            CompletableFuture<HttpResponse<String>> auth = requester.requestAuth(login, pass);
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
            manager.showPane();
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
            manager.showPane();
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
            CompletableFuture<Integer> status = requester.requestReg(login, pass);
            user.authorization(login, pass);
            status.thenAccept(this::handleRegistration);
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(3, 1));
            entryField.add(loginField);
            entryField.add(passField);
            entryField.add(regButton);
            manager.showPane();
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
            requester.requestSetTools(body, user.getLogin(), user.getPass());
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
            manager.showPane();
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
            requester.requestSetIngredients(body, user.getLogin(), user.getPass());
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
            manager.showPane();
        });
    }
}
