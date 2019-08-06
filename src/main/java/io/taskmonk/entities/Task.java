package io.taskmonk.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
    public String externalId;
    public String project_id;
    public String batch_id;
    public Map<String, String> input;
    public Map<String, String> output = new HashMap<String, String>();

    public Task(String externalId, String project_id, String batch_id,
                Map<String, String> input) {
        this.externalId = externalId;
        this.project_id = project_id;
        this.batch_id = batch_id;
        this.input = input;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getproject_id() {
        return project_id;
    }

    public void setproject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getbatch_id() {
        return batch_id;
    }

    public void setbatch_id(String batch_id) {
        this.batch_id = batch_id;
    }

    public Map<String, String> getInput() {
        return input;
    }

    public void setInput(Map<String, String> input) {
        this.input = input;
    }

    public Map<String, String> getOutput() {
        return output;
    }

    public void setOutput(Map<String, String> output) {
        this.output = output;
    }





}
