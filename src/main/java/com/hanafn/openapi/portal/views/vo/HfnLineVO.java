package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

@Data
@Alias("hfnline")
@EqualsAndHashCode(callSuper = false)
public class HfnLineVO {

	private String id;
	private String hfnCd;
	private String hfnId;
	private String userNm;
	private String signUserYn;
	private String signLevel;
	private String altYn;
	private String altId;
	private String altUserNm;
	private String altStDate;
	private String altEnDate;
	private String regUser;
	private String regDttm;
	private String modDttm;
	private String modUser;
}