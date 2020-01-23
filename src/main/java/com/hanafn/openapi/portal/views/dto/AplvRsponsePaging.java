package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.AplvVO;
import lombok.Data;

import java.util.List;

@Data
public class AplvRsponsePaging {
    private int pageIdx;
    private int pageSize;
    
    private int totCnt;
    private int selCnt;
    private List<AplvVO> list;
}
