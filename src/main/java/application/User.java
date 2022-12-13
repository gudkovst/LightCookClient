package application;

import dto.Standard;

public class User {

    private String login;
    private String pass;
    private String[] standardTools;
    private String[] standardIngredients;

    public User(){
        login = null;
        pass = null;
    }

    public boolean authorization(String login, String pass){
        this.login = login;
        this.pass = pass;
        return isAuth();
    }

    public boolean isAuth(){
        return login != null && pass != null;
    }

    public void setStandard(Standard standard){
        this.setStandardIngredients(standard.getIngredients());
        this.setStandardTools(standard.getTools());
    }

    public void setStandardIngredients(String[] standardIngredients) {
        this.standardIngredients = standardIngredients;
    }

    public void setStandardTools(String[] standardTools) {
        this.standardTools = standardTools;
    }

    public String getStandardTools(){
        String res = "Current standard tools:\n";
        for (String tool : standardTools) {
            res += tool + ", ";
        }
        return res;
    }

    public String getStandardIngredients(){
        String res = "Current standard ingredients:\n";
        for (String ingredient : standardIngredients)
            res += ingredient + ", ";
        return res;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }
}
