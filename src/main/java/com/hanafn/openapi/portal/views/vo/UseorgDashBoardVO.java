package com.hanafn.openapi.portal.views.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("useorgDashBoard")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UseorgDashBoardVO {

	private String day;
	private String tm;

	private String appKey;
	private String appNm;
	private String apiId;
	private String apiNm;
	private String useorgNm;

	private String appTotalCnt;					//앱 전체 건수
	private String appWaitCnt;					//앱 대기 건수
	private String appOkCnt;					//앱 정상 건수
	private String appCloseCnt;					//앱 종료 건수
	private String appExpireCnt;				//앱 기간만료 건수
	private String appExpireAplvWaitCnt;		//앱 기간만료 승인 대기 건수
	private String appExpireExpectCnt;			//앱 기간만료 예정 건수

	private String apiTrxTotalCnt;				//API 전체 건수
	private String apiTrxCnt;					//API 거래 건수
	private String procTerm;					//처리시간
	private String apiError;					//API 에러
	private String rnum;
	private String regHour;
	private String regCnt;
	private String hfnNm;

}
