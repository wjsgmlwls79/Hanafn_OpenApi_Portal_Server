package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.HfnUserVO;
import lombok.Data;

import java.util.List;

@Data
public class HfnUserRsponsePaging {
    private int pageIdx;
    private int pageSize;

    private int totCnt;
    private int selCnt;
    private List<HfnUserVO> list;
}