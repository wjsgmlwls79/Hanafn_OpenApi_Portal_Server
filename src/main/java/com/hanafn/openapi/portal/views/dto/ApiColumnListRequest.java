package com.hanafn.openapi.portal.views.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Data
public class ApiColumnListRequest {

    private String clmListCd;
    private String clmCd;
    private String apiId;
    private String clmNm;
    private String clmReqDiv;
    private Long clmOrd;
    private String clmType;
    private String clmNcsrYn;
    private String clmCtnt;
    private String clmDefRes;
}
