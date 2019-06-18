package io.taskmonk.auth;

public class OAuthClientCredentials {
    String clientId;
    String clientSecret;
    public OAuthClientCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
