package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiColumnRequest {

    private String clmCd;
    private String apiId;
    private String clmNm;
    private String clmReqDiv;
    private Long clmOrd;
    private String clmType;
    private String clmNcsrYn;
    private String clmCtnt;
    private String clmDefRes;
    private List<ApiColumnListRequest> apiColumnList;

}
