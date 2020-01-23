package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class AppsCnlKeyRequest {

    private String seqNo;
    private String cnlKey;
    private String appKey;
    private String regDttm;
    private String regUser;
    private String regUserId;
    private String modDttm;
    private String modUser;
    private String modUserId;
    private String useFl;
}
