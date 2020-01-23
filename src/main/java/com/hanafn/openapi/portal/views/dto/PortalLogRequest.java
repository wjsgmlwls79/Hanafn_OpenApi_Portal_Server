package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PortalLogRequest {
    private String searchNm;
    private String searchStDt;
    private String searchEnDt;
    private String searchProcStatCd;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;

    @Data
    public static class PortalLogDetailRequest {
        @NotNull
        private String trxId;
    }

}
