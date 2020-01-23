package com.hanafn.openapi.portal.views.dto;

import java.util.List;

import com.hanafn.openapi.portal.views.vo.UseorgVO;

import lombok.Data;

@Data
public class UseorgRsponsePaging {
    private int pageIdx;
    private int pageSize;
    
    private int totCnt;
    private int selCnt;
    private List<UseorgVO> list;
}
