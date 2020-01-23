package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiLogVO;
import lombok.Data;

import java.util.List;

@Data
public class ApiLogRsponsePaging {
    private int pageIdx;
    private int pageSize;
    
    private int totCnt;
    private int selCnt;
    private List<ApiLogVO> list;
}
