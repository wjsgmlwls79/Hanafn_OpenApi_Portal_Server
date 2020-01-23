package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApiLogRequest {
    private String searchNm;
    private String searchStDt;
    private String searchEnDt;
    private String searchGwProcStatCd;
    private String searchApiProcStatCd;
    private String searchTrxId;
    private String searchGwType;
    private String hfnCd;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;

    @Data
    public static class ApiLogDetailRequest {
        @NotNull
        private String trxId;
    }

}
