package com.hanafn.openapi.portal.security.dto;

import lombok.Data;

@Data
public class NiceIdResponse {
    private String sCipherTime = "";			// 복호화한 시간
    private String sRequestNumber = "";			// 요청 번호
    private String sResponseNumber = "";		// 인증 고유번호
    private String sAuthType = "";				// 인증 수단
    private String sName = "";					// 성명
    private String sDupInfo = "";				// 중복가입 확인값 (DI_64 byte)
    private String sConnInfo = "";				// 연계정보 확인값 (CI_88 byte)
    private String sBirthDate = "";				// 생년월일(YYYYMMDD)
    private String sGender = "";				// 성별
    private String sNationalInfo = "";			// 내/외국인정보 (개발가이드 참조)
    private String sMobileNo = "";				// 휴대폰번호
    private String sMobileCo = "";				// 통신사
    private String sMessage = "";
    private String sPlainData = "";

    @Data
    public static class NiceSuccessRequest {
        private String EncodeData;
        private String param_r1;
        private String param_r2;
        private String param_r3;
    }
}
