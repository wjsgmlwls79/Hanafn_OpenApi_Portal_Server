package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("apiPolicy")
public class ApiPolicyVO {
	private String apiId;
	private String apiNm;
	private String maxUser;
	private String maxSize;
	private String ltdTimeFm;
	private String ltdTime;
	private String ltdCnt;
	private String txRestrStart;
	private String txRestrEnd;
	private String txRestrValue;
	private String txRestrWeek;
	private String apiUrl;
	private String gubun;
	private String regDttm;
	private String regUser;
	private String modDttm;
	private String modUser;
}
