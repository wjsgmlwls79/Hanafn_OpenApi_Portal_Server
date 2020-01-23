package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Alias("apiDevGuide")
public class ApiDevGuideVO {

	private String apiId;
	private String apiNm;
	private String apiStatCd;
	private String ctgrCd;
	private String ctgrNm;
	private String apiSvc;
	private String apiVer;
	private String apiUri;
	private String apiMthd;
	private String userKey;
	private String apiCtnt;

	private List<ApiTagVO> ApiTagList;

}
