package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("qna")
public class QnaVO {
    private String seqNo;
    private String idIndex;
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
    private String regUser;
    private String modUser;

/*
    private String seqNo;           // 일렬번호
    private String useorgNm;        // 기업명
    private String qnaType;         // 문의 유형
    private String statCd;          // 상태코드
    private String userNm;          // 신청자명
    private String userTel;         // 신청자 연락처
    private String apiNm;           // API 명
    private String hfnCd;           // 관계사 코드
    private String qTitle;          // 문의 제목
    private String qCtnt;           // 문의 내용
    private String answerYn;        // 답변 여부
    private String aCtnt;           // 답변 내용
    private String uploadFile1;     // 첨부파일1 경로
    private String uploadFile2;     // 첨부파일2 경로
    private String regId;           // 신청자 ID
    private String regDttm;         // 신청 시간
    private String answerId;        // 답변자 ID
    private String answerDttm;      // 답변 시간
*/

}

