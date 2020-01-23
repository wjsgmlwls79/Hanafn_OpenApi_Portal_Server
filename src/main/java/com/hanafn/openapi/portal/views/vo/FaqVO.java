package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("faq")
public class FaqVO {
    private String seqNo;
    private String faqType;
    private String statCd;
    private String faqSubject;
    private String faqCtnt;
    private String regId;
    private String regDttm;
    private String modId;
    private String modDttm;
}
