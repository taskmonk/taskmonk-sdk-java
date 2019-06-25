package io.taskmonk.entities;

public class ImportUrl {

    private String file_url;

    public ImportUrl(String fileUrl) {
        this.file_url = fileUrl;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }
}
