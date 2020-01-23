package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("apiSubCtgr")
public class ApiSubCtgrVO {

	private String ctgrCd;
	private String subCtgrCd;
	private String subCtgrNm;
	private String subCtgrStatCd;
	private String subCtgrCtnt;
	private String apiCnt;
	private String regDttm;
	private String regUser;
	private String modDttm;
	private String modUser;
}
