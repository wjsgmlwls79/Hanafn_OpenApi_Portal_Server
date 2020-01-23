package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class HfnUserRequest {

    private String userKey;
    private String searchHfnCd;
    private String searchRoleCd;
    private String searchNm;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;

    @Data
    public static class HfnLineRequest {
        @NotNull
        private String userKey;
    }

    @Data
    public static class HfnUserDetailRequest{
        @NotNull
        private String userKey;
        private String userId;
    }

    @Data
    public static class HfnAltUserRequest{
        private String hfnCd;
        private String hfnId;
        private String searchText;
    }

    @Data
    public static class HfnUserDupCheckRequest{
        @NotBlank
        private String hfnId;
        @NotBlank
        private String hfnCd;
    }

    @Data
    public static class HfnUserStatCdChangeRequest{
        @NotNull
        private String userKey;
        private String userStatCd;
        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class HfnUserRegistRequest{

        private String userKey;

        @NotBlank
        private String hfnId;

        @NotBlank
        private String hfnCd;

        @NotBlank
        private String userNm;

        @NotBlank
        private String accessCd;

        @NotBlank
        private String userPwd;

        @NotBlank
        private String roleCd;    // 따로 관리필요 user_role_info 테이블(= roleCd)

        private String tmpPwd;

        private String jobNm;
        private String deptNm;

        private String signLevel;
        private String signUserYn;

        private String userTel;

        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class HfnUserUpdateRequest{
        @NotNull
        private String userKey;
        @NotBlank
        private String userNm;
        private String userPwd;
        private String newUserPwd;
        @NotBlank
        private String userStatCd;
        private String accessCd;
        private String deptNm;
        private String jobNm;
        private String signUserYn;
        private String userTel;
        private String regUserName;

        // 사용자 유형
        private String roleCd;

        //결재선정보
        private String hfnCd;
        private String hfnId;
        private String altYn;

        //대직정보
        private String altId;
        private String altUserNm;
        private String signLevel;
        private String altStDate;
        private String altEnDate;
        private String regUser;
        private String exAltId;
        private String exAltStDate;
        private String exAltEnDate;

        private String seq;
    }

    @Data
    public static class HfnUserIdUpdateRequest{
        @NotBlank
        private String userKey;
    }

    @Data
    public static class HfnUserPwdUpdateRequest{
        private String userKey;
        @NotBlank
        private String userPwd;
        private String regUserName;
        private String regUserId;

        private String seq;
        private String NewUserPwd;
        private String hfnId;
    }

    @Data
    public static class HfnUserPwdAndTosUpdateRequest{
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
    public static class HfnUserTmpPwdUpdateRequest{
        @NotNull
        private String userKey;
        private String userPwd;
        private String tmpPwd;
        private String regUserName;
    }

    @Data
    public static class HfnUserAplvLevelRequest {
        private String userKey;
        private String hfnCd;
    }
}
