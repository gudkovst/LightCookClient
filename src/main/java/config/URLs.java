package config;

public class URLs {
    private static final String addrServer = "localhost:8080/";
    private static final String logpass = "login=%s&pass=%s";
    public static String auth = addrServer + "authorization/?" + logpass;
    public static String reg = addrServer + "registration/?" + logpass;
    public static String setTools = addrServer + "settings/tools/?" + logpass;
    public static String setIngred = addrServer + "settings/ingredients/?" + logpass;
    public static String recipes = addrServer + "recipes";
    public static String media = addrServer + "media/%s";
    public static String getReview = addrServer + "recipe/%s/review";
    public static String sendReview = addrServer + "review";
}
