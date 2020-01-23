package com.hanafn.openapi.portal.views.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class SettlementRequest {

    private MultipartFile fileData;
    private String hfnCd;
    private String userKey;
    private String bilMonth;

    private String appKey;
    private String appNm;
    private String accCd;
    private String wdAmt;
    private String wdAcno;
    private String wdMemo;

    private String stDt;
    private String enDt;

    private String useorgCd;
    private String useorgNm;
    private String wdStatCd;
    private String regDttm;
    private String role;
    private String regUser;
    private String regUserId;

    private String seq;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;
}
