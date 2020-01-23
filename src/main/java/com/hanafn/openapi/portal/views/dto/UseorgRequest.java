package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UseorgRequest {
    private String searchNm;
    private String searchUseorgStatCd;
    private String searchHfnCd;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;

    @Data
    public static class UseorgDetailRequest{
        @NotBlank
        private String userKey;
    }

    @Data
    public static class UseorgRegistRequest{
        private String userKey;

        @NotBlank
        private String useorgNm;

        private String useorgStatCd;

        @NotBlank
        private String brn;

        @NotBlank
        private String entrCd;

        @NotBlank
        private String useorgCtnt;
        private String regUserName;
        private String regUserId;
        private String encKey;
    }

    @Data
    public static class UseorgUpdateRequest{
        @NotBlank
        private String userKey;
        private String useorgId;
        @NotBlank
        private String useorgNm;

        private String newUseorgPwd;
        private String useorgStatCd;
        private String entrCd;
        private String errorMsg;
        private String useorgCtnt;

        private String regUserName;
        private String regUserId;

        private String modUser;
        private String modUserId;

        private String useorgUserEmail;
        private String useorgUserTel;
        private String useorgBank;
        private String useorgBankNo;
        private String useorgGb;
        private String useorgTel;
        private String useorgUserNm;
        private String useorgUpload;
        private String pwChange;
        private String seq;

        // 관계사 포탈, 이용자 포탈 판단 및 수정 유저의 관계사 기반으로 승인정보 API 전송용도
        private String hfnCd;

        // 본인인증 관련
        private String userDi;  // 개인중복번호
        private String userResSeq;
    }

    @Data
    public static class UseorgDupCheckRequest{
        @NotBlank
        private String brn;
    }

    @Data
    public static class UseorgIdDupCheckRequest{
        @NotBlank
        private String useorgId;
    }

    @Data
    public static class UseorgStatCdChangeRequest {
        @NotBlank
        private String userKey;

        private String useorgStatCd;

        private String errorMsg;

        private String regUserName;

        private String regUserId;

        private String hbnUseYn;
        private String hnwUseYn;
        private String hlfUseYn;
        private String hcpUseYn;
        private String hcdUseYn;
        private String hsvUseYn;
        private String hmbUseYn;

        private String hfnCd;
    }

    @Data
    public static class UseorgSecedeRequest {
        @NotBlank
        private String userKey;
        private String userId;
        private String userNm;
        private String reasonGb;
        private String reasonDetail;
        private String aplvSeqNo;
        private String hbnUseYn;
        private String hnwUseYn;
        private String hlfUseYn;
        private String hcpUseYn;
        private String hcdUseYn;
        private String hsvUseYn;
        private String hmbUseYn;
    }

    @Data
    public static class HfnAplvRequest {
        private String userKey;
        private String userId;
        private String useorgNm;
        private String useorgUserNm;
        private String useorgCtnt;
        private String useorgId;
        private String userNm;
        private String hfnCd;
        private String useorgUpload;
        private String statCd;
    }

    @Data
    public static class HfnAplvRejectRequest {
        private String userKey;
        private String userId;
        private String useorgNm;
        private String useorgUserNm;
        private String useorgCtnt;
        private String useorgId;
        private String userNm;
        private String hfnCd;
        private String useorgUpload;
    }

    @Data
    public static class UseorgUploadRequest {
        @NotBlank
        private String userKey;
        private String useorgUpload;
    }
}
