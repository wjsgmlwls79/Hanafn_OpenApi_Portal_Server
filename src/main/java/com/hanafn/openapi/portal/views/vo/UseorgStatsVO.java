package com.hanafn.openapi.portal.views.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("useorgStats")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UseorgStatsVO {

	private String day;
	private String tm;

	private String apiId;
	private String apiNm;
	private String appKey;
	private String appNm;

	private String apiTrxCnt;			//API 거래 건수
	private String errorCnt;			//에러 건수
	private String successCnt;			//성공 건수
	private String authCnt;				//인증 건수
	private String authFailCnt;			//인증 실패 건수

}
