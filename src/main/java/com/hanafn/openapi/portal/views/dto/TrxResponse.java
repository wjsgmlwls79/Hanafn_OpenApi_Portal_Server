package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.TrxVO;
import lombok.Data;

import java.util.List;

@Data
public class TrxResponse {
    private List<TrxVO> trxList;
    private int totCnt;
    private int selCnt;
}
