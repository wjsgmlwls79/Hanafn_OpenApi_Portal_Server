package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class ApiStatModRequest {
    private String seqNo;
    private String apiId;
    private String apiModDiv;		//REG, MOD, STATSCHG, BATCH, // DEL 추가
    private String dlyTermDiv;
    private String dlyTermDt;
    private String dlyTermTm;
    private String regDttm;
    private String regUser;
    private String regUserName;
    private String regUserId;
    private String appUseCnt;
}
