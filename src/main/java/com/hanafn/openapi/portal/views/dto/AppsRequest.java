package com.hanafn.openapi.portal.views.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AppsRequest {

    private int pageIdx     = 0;
    private int pageSize    = 20;
    private int pageOffset  = 0;

    private String searchNm;        // 앱명
    private String searchOrg;       // 이용기관명
    private String searchStat;      // 앱상태코드  ('WAIT','OK','CLOSE','EXPIRE')
    private String searchAplvStat;  // 앱승상태코드 ('REQ','APLV','CANCEL','REJECT')
    private String appKey;
    private String appClientId;
    private String newAppClientId;
    private String appStatCd;
    private String appAplvStatCd;
    private String appSvcEnDt;
    private String reqCancelCtnt;
    private String modUserId;
    private String modUser;
    private String seqNo;
    private String AplvSeqNo;

    private String searchHfnCd;     // 관계사코드
    private String searchUserKey;

    private String useFl;           // appAPI, ip정보 리스트 사용여부('Y', 'N', 'WAIT')

    private String apiId;

    private String pubKey;
    private String page;

    @Data
    public static class AppsIssueRequest{
        private String appKey;
        private String pubKey;
        private String userId;
        private String userKey;
        private String reqServerIp;
        private String appClientId;
        private String newAppClientId;
        private String appScr;
        private String newAppScr;
    }

    @Data
    public static class AppApiRequest {
        private String statisticYear;
        private String statisticMnt;
        private String statisticDay;
        private String statisticDate;

        private String apiSvc;
        private String apiVer;
        private String apiUri;
        private String appKey;
        private String clientId;
        private String apiId;
        private String userKey;
    }

    @Data
    public static class AppDefresRequest {
        private String appKey;
        private String useDefresYn;
    }

    @Data
    public static class AppCryptoKeyRequest {
        private String searchNm;
        private String searchHfnCd;
        private int pageIdx;
        private int pageSize;
        private int pageOffset;
    }

    @Data
    public static class CryptoKeyFileRequest {
        private String appKey;
        private String keyPath;
        private String regUser;
        private String regDttm;
        private String modUser;
        private String modDttm;
    }
}
