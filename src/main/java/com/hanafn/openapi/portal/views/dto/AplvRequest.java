package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.HfnUserVO;
import com.hanafn.openapi.portal.views.vo.RequestApiVO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AplvRequest {
    private String procUser;
    private String procId;

    private String searchNm;
    private String searchHfnCd;
    private String searchAplvDivCd;
    private String searchAplvStatCd;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;

    @Data
    public static class AplvDetailRequest{
        @NotNull
        private String aplvSeqNo;

        private String procId;
        private String procUser;

        private String hfnCd;
    }

    @Data
    public static class AplvRegistRequest{
        private String aplvSeqNo;

        @NotBlank
        private String aplvReqCd;

        @NotBlank
        private String aplvDivCd;

        @NotBlank
        private String aplvReqCtnt;

        private String regUserName;

        private String regUserId;
        private String hfnCd;
    }

    @Data
    public static class AplvHisRegistRequest{
        @NotNull
        private String aplvSeqNo;

        private String seqNo;

        @NotBlank
        private String aplvReqCd;

        @NotBlank
        private String regUserName;

        @NotBlank
        private String procUser;

        @NotBlank
        private String regUserId;

        @NotBlank
        private String procId;

        private String hfnCd;
    }

    @Data
    public static class AplvRejectRequest{

        @NotNull
        private String aplvSeqNo;

        private String seqNo;

        @NotBlank
        private String rejectCtnt;

        private String aplvStatCd;

        private String regUserName;

        private String procUser;

        private String procId;

        private String hfnCd;
    }

    @Data
    public static class AplvApprovalRequest{

        @NotNull
        private String aplvSeqNo;

        private String seqNo;

        private String rejectCtnt;

        private String aplvStatCd;

        private String regUserName;

        private String procUser;

        private String procId;

        private String hfnCd;
    }

    @Data
    public static class AplvHisDetailRequest{

        @NotNull
        private String aplvSeqNo;

        @NotBlank
        private String procUser;
    }

    @Data
    public static class AplvLineSetRequest {
        private List<HfnUserVO> aplvList;
        private String aplvSeqNo;
        private String hfnCd;
        private String aplvDivCd;
        private String aplvReqCd;
        private String regUser;
        private String regId;
        private String appSvcStDt;
        private String appSvcEnDt;
        private String officialDocNo;
        private List<RequestApiVO> requestApiList;
    }

}
