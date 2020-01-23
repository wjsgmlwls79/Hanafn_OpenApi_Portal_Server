package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiCtgrVO;
import lombok.Data;

import java.util.List;

@Data
public class ApiCtgrRsponsePaging {
    private int pageIdx;
    private int pageSize;
    
    private int totCnt;
    private int selCnt;
    private List<ApiCtgrVO> list;
}
