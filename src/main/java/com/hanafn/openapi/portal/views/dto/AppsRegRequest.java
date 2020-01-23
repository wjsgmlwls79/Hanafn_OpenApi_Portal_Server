package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiVO;
import lombok.Data;
import java.util.List;

@Data
public class AppsRegRequest {

    private String seqNo; // 시퀀스 넘버 - backup 시에만 자료를 담음

    private String appKey;                          // 앱KEY
    private String appNm;                           // 앱명
    private String accCd;                           // 출금은행 코드
    private String accNo;                           // 출금은행 계좌
    private String appScr;                          // 앱Secret
    private String newAppScr;                       // 신규 앱Secret
    private String appStatCd;                       // 앱상태코드
    private String appAplvStatCd;                   // 앱 승인상태 코드
    private String appClientId;                     // 앱 CLIENT 아이디
    private String newAppClientId;                  // 신규 앱 CLIENT 아이디
    private String appSvcStDt;                      // 앱서비스시작일자
    private String appSvcEnDt;                      // 앱서비스종료일자
    private String appCtnt;                         // 앱설명
    private String hfnCd;                           // 관계사 코드
    private String userKey;                         // 이용기관코드
    private String regUser;                         // 등록사용자
    private String regUserId;                       // 등록사용자id
    private String modUser;                         // 변경사용자
    private String modUserId;                       // 변경사용자id
    private String appScrVldDttm;                   // 앱 Secret 유효 일시
    private String appScrReisuYn;                   // 앱 Secret 재발급 여부
    private String officialDocNo;                   // 공문번호
    private String aplvSeqNo;

    private String termEtdYn;                       // 기간 연장 여부
    private String reqCancelCtnt;                   // 요청 취소 설명

    private String regDttm;                         // 등록 일자
    private String modDttm;                         // 변경 일자

    private List<AppsCnlKeyRequest> cnlKeyList;     // 앱 채널 정보
    private List<AppsCnlKeyRequest> exCnlKeyList;   // 앱 채널 정보 (기존에 등록된 IP정보.업데이트시 필요)

    private List<AppsApiInfoRequest> apiList;       // 앱 API 정보
    private List<AppsApiInfoRequest> exApiList;     // 앱 API 정보 (기존에 등록된 API정보.업데이트시 필요)
    private List<ApiVO> redisApiList;
    private List<ApiVO> redisExApiList;

    private String useFl;                           // 앱 채널정보, api정보 사용여부 플래그  ('Y','N','WAIT')
}