package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class AppsApiInfoRequest {

    private String seqNo;
    private String appKey;
    private String apiId;
    private String hfnCd;
    private String regDttm;
    private String regUser;
    private String regUserId;
    private String modDttm;
    private String modUser;
    private String modUserId;
    private String useFl;
    private String isExist;
}
