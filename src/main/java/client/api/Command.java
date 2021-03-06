package client.api;

import java.util.Arrays;
import java.util.List;

public class Command<T> {

    public static enum HandleType {
        ACCOUNT, FILE_SEND, FILE_GET, PRODUCT, GENERAL, SALE, CHAT
    }

    private HandleType type;
    private String authToken;
    private String relic;
    private String message;
    private List<T> data;

    public Command() {
    }

    @SafeVarargs
    public Command(String message, HandleType type, T... data) {
        this.message = message;
        this.type = type;
        this.data = Arrays.asList(data);
    }

    public Command(String authToken, String message, List<T> data) {
        this.authToken = authToken;
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public List<T> getData() {
        return data;
    }

    public T getData(int i) {
        return data.get(i);
    }

    public T getDatum() {
        return data.get(0);
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public HandleType getType() {
        return type;
    }

    public String getRelic() {
        return relic;
    }

    public void setRelic(String relic) {
        this.relic = relic;
    }

}
