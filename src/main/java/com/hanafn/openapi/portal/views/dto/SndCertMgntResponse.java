package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.CertMailVO;
import lombok.Data;

import java.util.List;

@Data
public class SndCertMgntResponse {
    private CertMailVO certMail;
    private List<CertMailVO> certMailList;
    private int totCnt;
    private int selCnt;
}
