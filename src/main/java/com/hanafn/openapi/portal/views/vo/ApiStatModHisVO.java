package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("apiStatModHis")
public class ApiStatModHisVO {

	private String seqNo;
	private String apiId;
	private String apiModDiv;		//REG, MOD, STATSCHG, BATCH
	private String regDttm;
	private String regUser;

}
