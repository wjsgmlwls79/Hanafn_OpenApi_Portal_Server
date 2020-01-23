package com.hanafn.openapi.portal.views.dto;


import lombok.Data;

import java.util.List;

@Data
public class ApiRegistRequest {
    private String apiId;
    private String apiNm;
    private String apiStatCd;
    private String ctgrCd;
    private String apiSvc;
    private String apiVer;
    private String apiUri;
    private String apiUrl;
    private String apiMthd;
    private String apiCtnt;
    private String apiPubYn;
    private String userKey;
    private String regUserId;
    private String regUserName;

    private String gwType;
    private String apiProcType;
    private String apiProcUrl;
    private String apiTosUrl;
    private String subCtgrCd;
    private int feeAmount;
    private String hfnCd;
    private String hfnSvcCd;

    private String dlyTermDiv;
    private String dlyTermDt;
    private String dlyTermTm;

    private int minimumUseNumber;
    private int minimumCharges;

    private List<ApiTagRequest> apiTagList;
    private List<ApiColumnRequest> apiRequestList;
    private List<ApiColumnRequest> apiResponseList;

}
