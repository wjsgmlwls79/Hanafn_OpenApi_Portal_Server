package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class PrivacyReleaseLogRequest {

    private long idx;
    private String menuNm;
    private String userKey;
    private String regId;
}
