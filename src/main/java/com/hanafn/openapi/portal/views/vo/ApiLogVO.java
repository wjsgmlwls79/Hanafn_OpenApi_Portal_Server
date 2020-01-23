package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("apiLog")
public class ApiLogVO {
	private String trxId;
	private String regDttm;
	private String apiId;
	private String apiNm;
	private String apiUri;
	private String procTerm;
	private String gwProcStatCd;
	private String apiProcStatCd;
	private String appKey;
	private String appNm;
	private String useorgNm;
	private String inputCtnt;
	private String outputCtnt;
	private String gwType;

}
