package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiVO;
import lombok.Data;

import java.util.List;

@Data
public class ApisResponse {
    private String ctgrCd;
    private String ctgrNm;
    private String ctgrStatCd;
    private String ctgrCtnt;
    private String regDttm;
    private String regUser;
    private String modDttm;
    private String modUser;
    private String apiCnt;
    private String subCtgrCnt;
    private List<ApiVO> list;
}
