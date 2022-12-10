package dto;

public class Step {
    private final int media;
    private final String description;
    private final int waitTimeMins;

    public Step(int media, String description, int wait_time_mins){
        this.media = media;
        this.description = description;
        this.waitTimeMins = wait_time_mins;
    }

    public int getMedia() {
        return media;
    }

    public String getDescription() {
        return description;
    }

    public int getWaitTimeMins() {
        return waitTimeMins;
    }
}
