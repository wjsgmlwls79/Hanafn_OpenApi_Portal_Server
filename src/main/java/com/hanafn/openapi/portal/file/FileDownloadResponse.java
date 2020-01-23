package com.hanafn.openapi.portal.file;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class FileDownloadResponse {
    Resource data;
    private String contentType = "application/octet-stream";
    private long size;
}
