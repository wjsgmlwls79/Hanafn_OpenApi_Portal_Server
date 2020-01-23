package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.AppsVO;
import java.util.List;
import lombok.Data;

@Data
public class AppsRsponsePaging {

    private int pageIdx;
    private int pageSize;
    private int totCnt;
    private int selCnt;

    private List<AppsVO> list;
}
