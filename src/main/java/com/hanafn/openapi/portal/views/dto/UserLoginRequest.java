package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

public class UserLoginRequest {

    @Data
    public static class UpdatePwdRequest{
        @NotNull
        private String userKey;

        @NotNull
        private String newPwd;

        private String seq;
        private String userId;
    }

    @Data
    public static class HfnLoginLockCheckRequest{

        @NotNull
        private String hfnCd;
        @NotNull
        private String hfnId;

        private String  loginDttm;
        private String  loginLock;
        private String  loginLockTime;
        private int     loginFailCnt;
    }

    @Data
    public static class UserLoginLockCheckRequest{

        @NotNull
        private String userId;

        private String  loginDttm;
        private String  loginLock;
        private String  loginLockTime;
        private int     loginFailCnt;
    }
    @Data
    public static class UpdateTypeRequest {
        @NotNull
        private String userKey;
        @NotNull
        private String userType;
    }

}
