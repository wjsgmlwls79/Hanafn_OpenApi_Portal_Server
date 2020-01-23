package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class AppsScrRequest {

    private String seqNo;                  // 앱KEY
    private String appKey;                  // 앱KEY
    private String appClientId;             // 앱 Client ID
    private String appScr;                  // 앱 Secret
    private String appScrVldDttm;           // 앱 Secret 유효 일시
    private String regUser;                 // 등록 사용자
    private String regUserId;               // 등록 사용자 ID
    private String modUser;                 // 수정사용자
    private String modUserId;
    private String pubKey;
}