package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.NoticeVO;
import lombok.Data;

import java.util.List;

@Data
public class NoticeResponse {

    private NoticeVO notice;


    @Data
    public static class NoticeListResponse{
        private List<NoticeVO> noticeList;
        private int totCnt;
        private int selCnt;
        private int pageIdx;
        private int pageSize;
    }

}
