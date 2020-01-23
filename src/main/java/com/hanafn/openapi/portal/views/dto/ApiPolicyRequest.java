package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class ApiPolicyRequest {
    private String apiId;
    private String userKey;
    private String maxUser;
    private String maxSize;
    private String ltdTimeFm;
    private String ltdTime;
    private String ltdCnt;
    private String limitedValue;
    private String txRestrStart;
    private String txRestrEnd;
    private String txRestrValue;
    private String txRestrWeek;
    private String apiUrl;
    private String gubun;
    private String regUser;
    private String regDttm;
    private String modUser;
    private String modDttm;
}
