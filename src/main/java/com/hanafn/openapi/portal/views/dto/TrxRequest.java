package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class TrxRequest {
    private String search;
    private String trxCd;
    private String trxNm;
    private String trxGrant;
    private String regUser;
    private String regDttm;
    private String modUser;
    private String modDttm;

    private int pageIdx;
    private int pageSize;
    private int pageOffset;
}
