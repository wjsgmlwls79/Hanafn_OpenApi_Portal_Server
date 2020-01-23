package com.hanafn.openapi.portal.file;


import org.springframework.boot.context.properties.ConfigurationProperties;

// camel-case based link

@ConfigurationProperties(prefix="file")
public class FileUploadProperties {
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
