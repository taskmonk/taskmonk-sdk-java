package io.taskmonk.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchOutput {
    String file_url;
    String job_id;

    public BatchOutput() {

    }
    public String getfile_url() {
        return file_url;
    }

    public void setfile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getjob_id() {
        return job_id;
    }

    public void setjob_id(String job_id) {
        this.job_id = job_id;
    }

    @Override
    public String toString() {
        return "file_url = " + file_url + "; job_id = " + job_id;
    }
}
