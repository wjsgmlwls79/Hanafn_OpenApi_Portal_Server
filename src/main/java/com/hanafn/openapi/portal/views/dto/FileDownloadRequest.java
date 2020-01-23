package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class FileDownloadRequest {
    private String url;
    private String name;
    private String context;
}
