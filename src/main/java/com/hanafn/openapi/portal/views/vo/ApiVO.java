package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Alias("api")
public class ApiVO {

	private String apiId;
	private String apiNm;
	private String apiStatCd;
	private String ctgrCd;
	private String subCtgrCd;
	private String ctgrNm;
	private String apiSvc;
	private String apiVer;
	private String apiUri;
	private String apiUrl;
	private String apiMthd;
	private String apiCtnt;
	private String apiPubYn;
	private String apiEncYn;
	private String apiProcType;
	private String gwType;
	private String hfnSvcCd;
	private String apiProcUrl;
	private String apiTosUrl;
	private String userKey;
	private String useorgNm;
	private String appUseCnt;
	private String useorgUseCnt;
	private String dlyTermDiv;
	private String dlyTermDt;
	private String dlyTermTm;
	private String useFl;
	private String cnlKey;
	private String hfnCd;
	private String regDttm;
	private String regUser;
	private String procDttm;
	private String procUser;

	private int feeAmount;
	private int minimumUseNumber;
	private int minimumCharges;

	private List<ApiTagVO> ApiTagList;
}
