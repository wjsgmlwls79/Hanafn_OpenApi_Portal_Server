package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserRequest {
    private String userKey;
    private String searchHfnCd;
    private String searchEntrCd;
    private String searchRoleCd;
    private String searchUserType;
    private String searchNm;
    private String searchStDt;
    private String searchEnDt;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;

    @Data
    public static class UserDetailRequest{
        @NotNull
        private String userKey;
        private String hfnCd;
        private String hfnId;
    }

    @Data
    public static class UserDupCheckRequest{
        private String userId;
        private String useorgId;
        private String userEmail;
    }

    @Data
    public static class UserStatCdChangeRequest{
        @NotNull
        private String userKey;

        private String userStatCd;
        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class UserRegistRequest{

        private String userKey;

        @NotBlank
        private String userId;

        @NotBlank
        private String userNm;

        @NotBlank
        private String userPwd;

        private String tmpPwd;

        private String userTel;

        private String entrCd;

        private String roleCd;
        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class UserUpdateRequest{
        private String userPwd;
        private String newUserPwd;
        private String newUseorgPwd;
        private String userId;
        private String roleCd;
        private String userTel;
        private String userEmail;
        private String entrCd;
        private String regUserName;
        private String modUserName;
        private String modUserId;
        private String userGb;
        private String userCompany;
        private String authKey;

        @NotNull
        private String userKey;

        @NotBlank
        private String userNm;

        @NotBlank
        private String userStatCd;

        private String userDi;  // 개인중복번호 (본인인증용)
        private String userResSeq; // 본인인증 결과코드
    }

    @Data
    public static class UserAuthRequest{
        private String userGb;
        private String authKey;
    }

    @Data
    public static class UserIdUpdateRequest{
        @NotBlank
        private String userKey;
    }

    @Data
    public static class UserPwdUpdateRequest{
        private String userKey;

        @NotBlank
        private String userPwd;

        private String regUserName;
        private String userId;
        private String seq;
        private String authNum;
    }

    @Data
    public static class OrgPwdUpdateRequest{
        private String userKey;

        @NotBlank
        private String useorgPwd;

        private String modUser;
        private String useorgId;
        private String seq;
    }

    @Data
    public static class UserPwdAndTosUpdateRequest{
        private String userKey;

        @NotBlank
        private String userPwd;
        @NotBlank
        private String portalTosYn;
        @NotBlank
        private String privacyTosYn;

        private String regUserName;
    }

    @Data
    public static class UserSecedeRequest{
        @NotNull
        private String userKey;
        private String userId;
        private String userNm;

        private String reasonGb;
        private String reasonDetail;
    }

    @Data
    public static class UserTmpPwdUpdateRequest{
        @NotNull
        private String userKey;

        private String userPwd;
        private String tmpPwd;

        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class RegistDeveloperRequest{
        @NotNull
        private String userId;
        private String userKey;
        private String userEmail;
        private String userTel;
        private String userCompany;
        private String modUser;
        private String modDttm;
        private String entrCd;
    }

    @Data
    public static class searchUserRequest{
        private String userNm;
        private String userEmail;
        private String userId;
        private String sendNum;
    }

    @Data
    public static class searchUserorgRequest{
        private String useorgNm;
        private String brn;
        private String useorgUserEmail;
        private String useorgId;
        private String sendNum;
    }

}
