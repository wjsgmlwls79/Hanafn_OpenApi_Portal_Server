package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("hfnInfo")
public class HfnInfoVO {
	private String hfnCd;
	private String hfnNm;
	private String hfnStsCd;
}
