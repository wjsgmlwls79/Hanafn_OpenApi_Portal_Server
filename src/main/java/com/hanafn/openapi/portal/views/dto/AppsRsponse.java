package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.*;
import lombok.Data;

import java.util.List;

@Data
public class AppsRsponse {

    private String appKey;
    private String appClientId;
    private String newAppClientId;
    private String appNm;
    private String accCd;
    private String accNo;
    private String appStatCd;
    private String appAplvStatCd;
    private String appScr;
    private String newAppScr;
    private String appSvcStDt;
    private String appSvcEnDt;
    private String appCtnt;
    private String userKey;
    private String useorgNm;
    private String termEtdYn;
    private String dlDttm;
    private String appScrReisuYn;
    private String regDttm;
    private String regUser;
    private String modDttm;
    private String modUser;
    private String appScrVldDttm;

    private String regUserNm;
    private String modUserNm;

    private List<AppCnlInfoVO> cnlKeyList;  // 앱 채널 정보  //appCnlInfo
    private List<AppApiInfoVO> appApiInfo;  // 앱 API 정보
    private List<ApiVO> apiList;            // 등록된 앱 API의 API정보+API카테고리정보

    @Data
    public static class AppCryptoKeyResponse {
        private List<AppCryptoKeyVO> keyList;
        private List<AppsVO> appList;
        private int totCnt;
        private int selCnt;
    }
}
