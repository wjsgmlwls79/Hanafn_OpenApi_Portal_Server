package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("appClnInfo")
public class AppCnlInfoVO {

	private String seqNo;
	private String cnlKey;
	private String appKey;
	private String regDttm;
	private String regUser;
	private String modDttm;
	private String modUser;
	private String useFl;
}
