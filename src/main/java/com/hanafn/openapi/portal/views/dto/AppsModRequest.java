package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class AppsModRequest {

    private String seqNo;                             // 앱KEY
    private String appKey;                          // 앱KEY
    private String appStatCd;                       // 앱 상태 코드
    private String appAplvStatCd;                   // 앱 승인 상태 코드
    private String appClientId;                     // 앱 client id
    private String appScrVldDttm;                   // 앱 Secret 유효 일시
    private String termEtdYn;                       // 앱 기간 연장 여부
    private String appScr;                          // 앱Secret
    private String modUser;                         // 수정사용자
    private String modUserId;                       // 수정사용자ID
}