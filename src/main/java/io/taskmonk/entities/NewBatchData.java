package io.taskmonk.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewBatchData {
    String batch_name;
    Short priority = 1;
    String comments = "";
    public List<Notification> notifications = new ArrayList<Notification>();

    public NewBatchData(String batch_name) {
        this.batch_name = batch_name;
    }

    public String getBatchName() {
        return batch_name;
    }

    public NewBatchData setBatchName(String batch_name) {
        this.batch_name = batch_name;
        return this;
    }

    public Short getPriority() {
        return priority;
    }

    public NewBatchData setPriority(Integer priority) {
        this.priority = priority.shortValue();
        return this;
    }

    public String getComments() {
        return comments;
    }

    public NewBatchData setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public NewBatchData setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        return this;
    }
}
