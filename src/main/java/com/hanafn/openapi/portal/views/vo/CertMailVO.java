package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("certMail")
public class CertMailVO {
    /**
     * PORTAL_SND_CERT_MGNT     메일/메시지 발송 관리
     */

    private String seq;
    private String userKey;
    private String recvInfo;
    private String sendCd;
    private String sendCtnt;
    private String sendNo;
    private String regDttm;
    private String certValidTm;
    private String expireDttm;
    private String certDttm;
    private String resultCd;
    private String retryCnt;

    private String userId;

}
