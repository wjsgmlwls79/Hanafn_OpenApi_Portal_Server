package com.hanafn.openapi.portal.views.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("stats")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatsVO {

	private String day;
	private String tm;

	private String apiId;
	private String apiNm;

	private String appKey;
	private String appNm;
	private String ctgrNm;

	private String apiTrxCnt;			//API 총 건수
	private String avgProcTerm;			//응답시간 평균
	private String maxProcTerm;			//최고 응답시간
	private String procTerm;			// 응답시간
	private String gwError;				//G/W 에러율
	private String apiError;			//API 에러율

}
