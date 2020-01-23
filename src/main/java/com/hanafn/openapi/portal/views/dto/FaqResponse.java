package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.FaqVO;
import lombok.Data;

import java.util.List;

@Data
public class FaqResponse {
    private String seqNo;
    private String faqType;
    private String statCd;
    private String faqSubject;
    private String faqCtnt;
    private String regId;
    private String regDttm;
    private String modId;
    private String modDttm;

    private List<FaqVO> faqList;
    private FaqVO faq;

    private int totCnt;
    private int selCnt;
    private int pageIdx;
    private int pageSize;
}
