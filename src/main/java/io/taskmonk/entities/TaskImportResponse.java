package io.taskmonk.entities;


public class TaskImportResponse {
    public String batchId;
    public String excelJobId;
    public String jobId;

    public TaskImportResponse() {

    }
    public TaskImportResponse(String batchId, String excelJobId, String jobId) {
        this.batchId = batchId;
        this.excelJobId = excelJobId;
        this.jobId = jobId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getJobId() {
        return jobId;
    }

    public String getExcelJobId() {
        return excelJobId;
    }

    public void setExcelJobId(String excelJobId) {
        this.excelJobId = excelJobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
