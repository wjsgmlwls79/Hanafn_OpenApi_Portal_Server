package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class LogRequest {
    private String trxId;
    private String trxCd;
    private String userId;
    private String roleCd;
    private String procStatCd;
    private String inputCtnt;
    private String outputCtnt;
}
