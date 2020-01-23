package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("aplvHis")
public class AplvHisVO {

	private String seqNo;
	private String aplvSeqNo;
	private String aplvStatCd;
	private String rejectCtnt;
	private String regDttm;
	private String regUser;
	private String regId;
	private String procDttm;
	private String procUser;
	private String procId;
	private String signLevel;
}
