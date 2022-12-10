package dto;

public class LogPass {
    private String login;
    private String pass;

    public LogPass(String login, String pass){
        this.login = login;
        this.pass = pass;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }
}
