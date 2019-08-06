package io.taskmonk.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Bean providing job response details
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobProgressResponse {
    public Integer completed;
    public Integer total;
    public Integer percentage;

    public JobProgressResponse() {

    }
    public JobProgressResponse(Integer completed, Integer total, Integer percentage) {
        this.completed = completed;
        this.total = total;
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "completed = " + completed + "; total = " + total + "; percentage = " + percentage;
    }

    public Boolean isCompleted() {
        return total.equals(completed);
    }
}
