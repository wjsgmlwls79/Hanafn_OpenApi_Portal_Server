package com.hanafn.openapi.portal.file;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;
}
