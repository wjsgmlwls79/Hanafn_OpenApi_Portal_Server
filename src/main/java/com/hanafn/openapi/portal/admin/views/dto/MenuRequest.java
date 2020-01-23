package com.hanafn.openapi.portal.admin.views.dto;

import lombok.Data;

@Data
public class MenuRequest {
    private String idx;
    private String pageUrl;
    private String portalType;
    private String userIp;
    private String userKey;
}
