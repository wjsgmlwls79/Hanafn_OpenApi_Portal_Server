package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.QnaVO;
import lombok.Data;

import java.util.List;

@Data
public class QnaResponse {
    private List<QnaVO> qnaList;
    private QnaVO qna;

    private int totCnt;
    private int selCnt;
    private int pageIdx;
    private int pageSize;
}
