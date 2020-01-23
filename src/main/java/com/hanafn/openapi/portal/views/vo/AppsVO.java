package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("apps")
public class AppsVO {

    /**
     * OPENAPI_PORTAL_APP_INFO 앱정보
     */

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
    private String useorgNm;
    private String useorgId;
    private String userKey;
    private String dlDttm;
    private String termEtdYn;
    private String appScrReisuYn;
    private String reqCancelCtnt;
    private String regDttm;
    private String regUser;
    private String regUserNm;
    private String regId;
    private String modDttm;
    private String modUser;
    private String modUserNm;
    private String modId;
    private String appScrVldDttm;
    private String officialDocNo;

    private String hfnCd;
    private String hfnNm;
    private String entrCd;
    private String encKey;
    private String regUserKey;

    private String userNmEncrypted;
    private String regUserNmEncrypted;
    private String modUserNmEncrypted;
}