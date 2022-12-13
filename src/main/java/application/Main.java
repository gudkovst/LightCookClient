package application;

import view.IView;
import view.ViewSwing;

public class Main {
    public static void main(String[] args){
        IView view = new ViewSwing();
        view.start();
    }
}
