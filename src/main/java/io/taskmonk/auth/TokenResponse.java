package io.taskmonk.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class TokenResponse {
    private static final Logger logger = LoggerFactory.getLogger(TokenResponse.class);
    String token_type;
    String access_token;
    String refresh_token;
    Long expires_in;
    LocalDateTime expires_at = LocalDateTime.now().minusDays(1);

    public Boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expires_at)) {
            return true;
        }
        return false;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
        setExpiry();
    }

    public TokenResponse() {

    }
    public TokenResponse(String token_type,
                  String access_token,
                  String refresh_token,
                  Long expires_in) {
        this.token_type = token_type;
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.expires_in = expires_in;
        setExpiry();
    }
    private void setExpiry() {
        LocalDateTime now = LocalDateTime.now();
        expires_at = now.plusSeconds(expires_in - 30);
    }

    @Override
    public String toString() {
        return "access_token = " + access_token + "; refresh_token = {}" + refresh_token + "; expires_in = " + expires_in;
    }
}

