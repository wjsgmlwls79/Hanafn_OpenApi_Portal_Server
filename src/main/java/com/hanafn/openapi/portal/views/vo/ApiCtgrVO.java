package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("apiCtgr")
public class ApiCtgrVO {

	private String ctgrCd;
	private String ctgrNm;
	private String ctgrStatCd;
	private String ctgrCtnt;
	private String regDttm;
	private String regUser;
	private String modDttm;
	private String modUser;
	private String apiCnt;
	private String subCtgrCnt;
}
