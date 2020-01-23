package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class SndCertMgntRequest {
    private String seq;     // SEQ
    private String userKey;     // 유저key
    private String recvInfo;    // 수신자 정보(이메일)
    private String sendCd;      // 발송유형
    private String sendCtnt;    // 발송 내용
    private String sendNo;      // 발송 인증번호
    private String certValidTm; // 인증유효시간
    private String resultCd;    // 결과코드
    private String retryCnt;    // 재전송 시도횟수

    private String userId;
    private int pageIdx;
    private int pageSize;
    private int pageOffset;
}
