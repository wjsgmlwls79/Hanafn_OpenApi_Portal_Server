package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.AppsVO;
import com.hanafn.openapi.portal.views.vo.DashBoardVO;
import com.hanafn.openapi.portal.views.vo.UseorgDashBoardVO;
import lombok.Data;

import java.util.List;

@Data
public class DashBoardRsponse {

    private String apiUseCnt;			//API 운영 건수
    private String apiCloseCnt;			//API 정지 건수
    private String useorgOkCnt;			//이용기관 운영 건수
    private String useorgCloseCnt;		//이용기관 정지 건수
    private String appOkCnt;			//앱 활성화 건수
    private String appCloseCnt;			//앱 비활성화 건수
    private String appExpireCnt;		//앱 기간만료 도래 건수
    private String aplvWaitCnt;			//승인 대기 건수

    private String apiTrxCnt;			//API 총 건수
    private String avgProcTerm;			//처리기간 평균
    private String maxProcTerm;			//최고 응답시간
    private String gwError;				//G/W 에러율
    private String apiError;			//API 에러율

    List<DashBoardVO> topApiUseList;
    List<DashBoardVO> topProcTermList;
    List<DashBoardVO> topApiErrorList;
    List<DashBoardVO> dashBoardDayList;

    @Data
    public static class UseorgDashBoardRsponse {

        private String useorgNm;

        private String appTotalCnt;					//앱 전체 건수
        private String appWaitCnt;					//앱 대기 건수
        private String appOkCnt;					//앱 정상 건수
        private String appCloseCnt;					//앱 종료 건수
        private String appExpireCnt;				//앱 기간만료 건수
        private String appExpireAplvWaitCnt;		//앱 기간만료 승인 대기 건수
        private String appExpireExpectCnt;			//앱 기간만료 예정 건수

        private String apiTrxTotalCnt;			//API 총 건수

        List<UseorgDashBoardVO> appApiTrxList;
        List<UseorgDashBoardVO> apiTrxList;
        List<UseorgDashBoardVO> dashBoardDayList;
        List<UseorgDashBoardVO> myAppList;
        List<AppsVO> appModList;
    }

}
