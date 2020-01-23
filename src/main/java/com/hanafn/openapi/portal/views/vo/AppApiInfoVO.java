package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("appApiInfo")
public class AppApiInfoVO {

	private String seqNo;
	private String appKey;
	private String apiId;
	private String hfnId;
	private String regDttm;
	private String regUser;
	private String modDttm;
	private String modUser;
	private String useFl;
	private String isExist;
}
