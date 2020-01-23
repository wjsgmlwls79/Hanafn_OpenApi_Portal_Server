package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("apiColumnList")
public class ApiColumnListVO {

	private String clmListCd;
	private String clmCd;
	private String apiId;
	private String clmNm;
	private String clmReqDiv;
	private Long clmOrd;
	private String clmType;
	private String clmNcsrYn;
	private String clmCtnt;
	private String clmDefRes;

}
