package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApiRequest {

    private String apiId;
    private String apiStatCd;
    private String regUserName;
    private String regUserId;
    private String roleCd;

    private String searchHfnCd;
    private String searchNm;
    private String searchCtgrCd;
    private String searchSubCtgrCd;
    private String searchApiMthd;
    private String searchApiStatCd;
    private String searchApiPubYn;
    private String searchUserKey;
    private String appUseCnt;
    private String stDt;
    private String edDt;
    private String msDate;
    private String appKey;
    private String enDt;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;

    @Data
    public static class ApiColumnListRequest{
        private String clmCd;
        private String apiId;
        private String clmReqDiv;
    }

    @Data
    public static class ApiAllListRequest{
    }
    @Data
    public static class ApiDetailRequest{

        private String appKey;
        private String useFl;
        private String apiId;
        private String aplvSeqNo;
    }

    @Data
    public static class CtgrApiAllListRequest{
        private String searchCtgrCd;
        private String searchHfnCd;
    }

    @Data
    public static class SwaggerRequest{
        @NotNull
        private String apiSvc;
    }

    @Data
    public static class SwaggerInfoRequest{
        @NotNull
        private String apiSvc;
        @NotNull
        private String apiVer;
        @NotNull
        private String apiUri;
        @NotNull
        private String apiMthd;
    }
}
