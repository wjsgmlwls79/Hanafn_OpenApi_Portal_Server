package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("aplv")
public class AplvVO {

	private String aplvSeqNo;
	private String aplvReqCd;
	private String aplvStatCd;
	private String aplvDivCd;
	private String aplvReqCtnt;
	private String regDttm;
	private String regUser;
	private String regId;
	private String procDttm;
	private String procUser;
	private String modId;
	private String aplvBtnYn;
	private String userId;
	private String useorgNm;
	private String hfnCd;
	private String hfnNm;
	private String procId;
	private String useorgUserEmail;
	private String userNmEncrypted;
	private String procUserNmEncrypted;
}
