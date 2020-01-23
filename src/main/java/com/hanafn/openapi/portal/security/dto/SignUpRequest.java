package com.hanafn.openapi.portal.security.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

import java.util.List;

@Data
public class SignUpRequest {

	@NotBlank
    @Size(min = 2, max = 10)
    private String userId;

    @NotBlank
    @Size(min = 3, max = 50)
    private String userNm;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;
    
    @NotBlank
    @Size(min = 10, max = 15)
    private String userTel;

    @NotBlank
    @Size(min = 10, max = 12)
    private String brn;

    @NotBlank
    @Size(min = 3, max = 50)
    private String useorgNm;

    @NotBlank
    @Size(min = 6, max = 30)
    private String entrCd;

    @Size(min = 1, max = 30)
    private String userKey;

    @Size(max = 2000)
    private String useorgCtnt;

    @Size(min = 1, max = 10)
    private String portalTosYn;

    @Size(min = 1, max = 10)
    private String privacyTosYn;

    private String regUserId;

    @Data
    public static class UserSingnUpRequest {
        private String userKey;
        private String userGb;
        private String userId;
        private String userNm;
        private String userPwd;
        private String userCompany;
        private String userEmail;
        private String userJobNm;
        private String userTel;
        private String portalTosYn;
        private String privacyTosYn;
        private String telAuthNo;
        private String regUser;
        private String userDi;  // 개인 중복번호(nice)
        private String userResSeq;  // 본인인증 결과코드 (nice)
        private String userStatCd;
        private String seq;
    }

    @Data
    public static class UseorgSignUpRequest {
        private String userKey;
        private String useorgId;
        private String entrCd;
        private String useorgNm;
        private String brn;
        private String encKey;
        private String useorgPwd;
        private String useorgStatCd;
        private String useorgGb;
        private String userDi;  // 본인인증 이력관련(개인중복번호)
        private String userResSeq;  // 본인인증 결과코드 (nice)
        private String useorgUserNm;
        private String useorgUserEmail;
        private String useorgUserTel;
        private String useorgSelApi;
        private String useorgDomain;
        private String useorgBank;
        private String useorgBankNo;
        private String useorgOwnNm;
        private String useorgNo;
        private String useorgTel;
        private String useorgBussNm;
        private String useorgAddr;
        private String useorgHomepage;
        private String useorgCtnt;
        private String useorgUpload;
        private String errorMsg;

        private String hbnUseYn;
        private String hnwUseYn;
        private String hlfUseYn;
        private String hcpUseYn;
        private String hcdUseYn;
        private String hsvUseYn;
        private String hmbUseYn;

        private String regDttm;
        private String regUser;
        private String regId;
        private String modDttm;
        private String modUser;
        private String modId;

        private String seq;
    }
}
