package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiCtgrVO;
import com.hanafn.openapi.portal.views.vo.ApiVO;
import lombok.Data;

import java.util.List;

@Data
public class ApiRsponsePaging {
    private int pageIdx;
    private int pageSize;
    
    private int totCnt;
    private int selCnt;
    private List<ApiVO> list;
    private List<ApiCtgrVO> ctgrList;
}
