package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Alias("apiColumn")
public class ApiColumnVO {

	private String clmCd;
	private String apiId;
	private String clmNm;
	private String clmReqDiv;
	private Long clmOrd;
	private String clmType;
	private String clmNcsrYn;
	private String clmCtnt;
	private String clmDefRes;
	private List<ApiColumnListVO> apiColumnList;

}
