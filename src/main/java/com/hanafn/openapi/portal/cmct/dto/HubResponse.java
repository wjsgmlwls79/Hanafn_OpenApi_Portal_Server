package com.hanafn.openapi.portal.cmct.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HubResponse {
    private String code;
    private Map<String, Object> result;
}
