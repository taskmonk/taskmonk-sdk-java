package io.taskmonk.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportUrl {

    private String file_url;
    private String file_type;

    /**
     * set the import settings
     * @param fileUrl - path to url with the input file
     * @param fileType - file type - CSV or Excel
     */
    public ImportUrl(String fileUrl, String fileType) {
        this.file_url = fileUrl;
        this.file_type = fileType;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getFile_type() {
        return file_type;
    }
}
