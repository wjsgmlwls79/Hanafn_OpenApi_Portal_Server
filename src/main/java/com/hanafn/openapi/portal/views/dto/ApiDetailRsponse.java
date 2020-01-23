package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiColumnVO;
import com.hanafn.openapi.portal.views.vo.ApiStatModHisVO;
import com.hanafn.openapi.portal.views.vo.ApiTagVO;
import lombok.Data;

import java.util.List;

@Data
public class ApiDetailRsponse {
    private String apiId;
    private String apiNm;
    private String apiStatCd;
    private String ctgrCd;
    private String ctgrNm;
    private String apiSvc;
    private String apiVer;
    private String apiUri;
    private String apiUrl;
    private String apiMthd;
    private String apiCtnt;
    private String apiPubYn;
    private String userKey;
    private String dlyTermDiv;
    private String dlyTermDt;
    private String dlyTermTm;
    private String regDttm;
    private String regUser;
    private String procDttm;
    private String procUser;
    private String regUserName;

    private String gwType;
    private String apiProcType;
    private String hfnCd;
    private String hfnSvcCd;
    private String apiProcUrl;
    private String apiTosUrl;
    private String subCtgrCd;
    private int feeAmount;
    private int minimumUseNumber;
    private int minimumCharges;

    private List<ApiTagVO> apiTagList;
    private List<ApiColumnVO> apiRequestList;
    private List<ApiColumnVO> apiResponseList;
    private List<ApiStatModHisVO> apiStatModHisList;
}
