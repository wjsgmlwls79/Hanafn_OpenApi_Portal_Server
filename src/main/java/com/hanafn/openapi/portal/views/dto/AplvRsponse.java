package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.*;
import lombok.Data;

import java.util.List;

@Data
public class AplvRsponse{

    @Data
    public static class AplvDetilResponse {
        private String aplvSeqNo;
        private String aplvReqCd;
        private String aplvStatCd;
        private String aplvDivCd;
        private String aplvReqCtnt;
        private String regDttm;
        private String regUser;
        private String regId;
        private String procDttm;
        private String procUser;
        private String rejectCtnt;
        private String aplvBtnYn;
        private String appSvcAddDt;
        private String procId;


        //private UserVO useorgInfo;
        private UseorgVO useorgInfo;
        private AppsVO appInfo;
        private List<ApiVO> ApiList;
        private List<ApiVO> ipList;
        private List<AplvHisVO> AplvHisList;
        private List<RequestApiVO> requestApiList;
        private List<FeeCollectionInfoVO> feeRegList;
        private FeeCollectionInfoVO feeRegInfo;
    }

}
