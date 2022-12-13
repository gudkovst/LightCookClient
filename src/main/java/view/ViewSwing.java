package view;

import application.User;
import config.Config;

import javax.swing.*;

public class ViewSwing extends JFrame implements IView, Runnable{
    private final ViewSwingApplication applicationView;
    private final ViewSwingRecipes recipesView;
    private final JPanel entryField;
    private final User user;

    public ViewSwing(){
        user = new User();
        entryField = new JPanel();
        applicationView = new ViewSwingApplication(user, entryField, this);
        recipesView = new ViewSwingRecipes(user, entryField, this);
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
    public void run() {
        init();
        startPage();
    }

    @Override
    public void start() {
        javax.swing.SwingUtilities.invokeLater(this);
    }

    @Override
    public void findRecipes() {
        recipesView.findRecipes();
    }

    @Override
    public void startPage() {
        applicationView.startPage();
    }

    @Override
    public void authPage() {
        applicationView.authPage();
    }

    @Override
    public void showPane(){
        setContentPane(entryField);
    }
}
