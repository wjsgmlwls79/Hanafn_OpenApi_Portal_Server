package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class StatsRequest {

    private String searchApiId;
    private String searchUserKey;
    private String searchHfnCd;
    private String searchCtgrCd;
    private String searchAppKey;
    @NotBlank
    private String searchStDt;
    @NotBlank
    private String searchEnDt;

    @Data
    public static class AppDetailStatsRequest{
        @NotBlank
        private String appKey;
        private String searchUserKey;
        private String searchApiId;
        private String searchAppKey;
        @NotBlank
        private String searchStDt;
        @NotBlank
        private String searchEnDt;
    }
}
