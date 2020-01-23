package com.hanafn.openapi.portal.views.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("dashBoard")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashBoardVO {

	private String day;
	private String tm;

	private String apiId;
	private String apiNm;

	private String apiUseCnt;			//API 운영 건수
	private String apiCloseCnt;			//API 정지 건수
	private String useorgOkCnt;			//이용기관 운영 건수
	private String useorgCloseCnt;		//이용기관 정지 건수
	private String appOkCnt;			//앱 활성화 건수
	private String appCloseCnt;			//앱 비활성화 건수
	private String appExpireCnt;		//앱 기간만료 도래 건수
	private String aplvWaitCnt;			//승인 대기 건수

	private String apiTrxCnt;			//API 총 건수
	private String avgProcTerm;			//응답시간 평균
	private String maxProcTerm;			//최고 응답시간
	private String procTerm;			// 응답시간
	private String gwError;				//G/W 에러율
	private String apiError;			//API 에러율

	private String apiUrl;

	private String regHour;
	private String regCnt;
}
