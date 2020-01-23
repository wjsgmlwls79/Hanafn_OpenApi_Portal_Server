package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class FaqRequest {
    private String seqNo;
    private String faqType;
    private String statCd;
    private String faqSubject;
    private String faqCtnt;
    private String regId;
    private String regDttm;
    private String modId;
    private String modDttm;

    private int pageIdx;
    private int pageSize;
    private int pageOffset;
    private String searchFaq;
}
