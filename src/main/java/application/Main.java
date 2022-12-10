package application;

import view.View;
import view.ViewSwing;

public class Main {
    public static void main(String[] args){
        User user = new User(new Requester());
        View view = new ViewSwing(user);
        view.start();
    }
}
