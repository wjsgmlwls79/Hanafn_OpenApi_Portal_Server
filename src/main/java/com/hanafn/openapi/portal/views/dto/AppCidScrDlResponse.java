package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class AppCidScrDlResponse {
    private String token;
    private String userId;
    private String userKey;
    private String reqServerIp;
    private String procDttm;
}
