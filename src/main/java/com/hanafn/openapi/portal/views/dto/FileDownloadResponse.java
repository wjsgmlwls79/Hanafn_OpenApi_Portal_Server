package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class FileDownloadResponse {
    private String url;
    private String name;
    private String context;
}

