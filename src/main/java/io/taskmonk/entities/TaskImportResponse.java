package io.taskmonk.entities;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskImportResponse {
    public String job_id;
    public String batch_id;
    // For backward compatibility
    public String batchId;

    public String getBatch_id() {
        return batch_id;
    }

    public void setBatch_id(String batch_id) {
        this.batch_id = batch_id;
        this.batchId = batch_id;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public TaskImportResponse() {

    }
    public TaskImportResponse(String job_id) {
        this.job_id = job_id;
    }
    public TaskImportResponse(String job_id, String batch_id) {
        this.job_id = job_id;
        this.batchId = batch_id;
        this.batch_id = batch_id;
    }

    @Override
    public String toString() {
        return "job_id = " + job_id + "; batchId = {}" + batchId + "; batch_id = " + batch_id;
    }
}
