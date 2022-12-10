package application;

import config.Config;
import config.URLs;
import dto.Recipe;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Requester {
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public CompletableFuture<HttpResponse<String>> requestAuth(String login, String pass){
        String uri = String.format(URLs.auth, login, pass);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<Integer> requestReg(String login, String pass){
        String uri = String.format(URLs.reg, login, pass);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).
                thenApply(HttpResponse::statusCode);
    }

    public void requestSetTools(String body, String login, String pass){
        String uri = String.format(URLs.setTools, login, pass);
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body)).
                uri(URI.create(uri)).build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public void requestSetIngredients(String body, String login, String pass){
        String uri = String.format(URLs.setIngred, login, pass);
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body)).
                uri(URI.create(uri)).build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<List<Recipe>> requestRecipes(String body){
        String uri = URLs.recipes;
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).
                method("GET", HttpRequest.BodyPublishers.ofString(body)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).
                thenApply(HttpResponse::body).
                thenApply(JsonConverter::parseRecipes);
    }

    public void sendComment(String body){
        String uri = URLs.sendReview;
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body)).
                uri(URI.create(uri)).build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public String requestMedia(int id){
        String uri = String.format(URLs.media, id);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();
        String mediaFile = Config.resourcesPath + id + Config.pictureFormat;
        try{
            byte[] media = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
            FileOutputStream writer = new FileOutputStream(mediaFile);
            writer.write(media);
            writer.close();
            return mediaFile;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
