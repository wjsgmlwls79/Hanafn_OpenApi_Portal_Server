package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("apiTag")
public class ApiTagVO {

	private Long tagOrd;
	private String apiId;
	private String tagCd;
	private String regDttm;
	private String regUser;
	private String modDttm;
	private String modUser;

}
