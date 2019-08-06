package io.taskmonk.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Notification {
    String notificationType;
    Map<String, String> metaData;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public Notification(String notificationType, Map<String, String> metaData) {
        this.notificationType = notificationType;
        this.metaData = metaData;
    }

}
