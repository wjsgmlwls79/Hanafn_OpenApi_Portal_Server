package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import java.util.List;

@Data
public class QnaRequest {
    private String seqNo;
    private String compNm;
    private String statCd;
    private String qnaType;
    private String userKey;
    private String userNm;
    private String userTel;
    private String apiNm;
    private String hfnCd;
    private String reqTitle;
    private String reqCtnt;
    private String uploadFile01;
    private String uploadFile02;
    private String userId;
    private String regDttm;
    private String answerYn;
    private String answer;
    private String modId;
    private String modDttm;

    private String searchQna;
    private int pageIdx;
    private int pageSize;
    private int pageOffset;

    private String siteCd;

    @Data
    public static class QnaDeleteRequest {
        private List<String> seqList;
    }
}
