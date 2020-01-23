package com.hanafn.openapi.portal.cmct.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SwaggerResponse {
    private String swagger;
    private Map<String, Object> info;
    private String host;
    private String basePath;
    private List<Map<String, Object>> tags;
    private Map<String, Object> paths;
    private Map<String, Object> definitions;
}
