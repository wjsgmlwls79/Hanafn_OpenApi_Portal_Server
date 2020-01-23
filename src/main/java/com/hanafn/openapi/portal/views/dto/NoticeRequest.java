package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class NoticeRequest {
    private int seqNo;
    private int viewCnt;
    private String noticeSubject;
    private String noticeCtnt;
    private String regId;
    private String regUserId;
    private String redDttm;
    private String modUserId;
    private String modId;
    private String modDttm;

    @Data
    public static class NoticeListRequest {
        private String searchNotice;
        private String statCd;
        private int pageIdx;
        private int pageSize;
        private int pageOffset;
    }

}
