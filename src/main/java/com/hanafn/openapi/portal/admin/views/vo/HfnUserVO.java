package com.hanafn.openapi.portal.admin.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("hfnuser")
public class HfnUserVO {
	private String userKey;
	private String hfnId;
	private String userNm;
	private String userPwd;
	private String hfnCd;
	private String deptNm;
	private String jobNm;
	private String userStatCd;
	private String accessCd;
	private String signUserYn;
	private String signLevel;
	private String tmpPwdYn;
	private String tmpPwd;
	private String userTel;
	private String regUser;
	private String regDttm;
	private String modDttm;
	private String modUser;
	private String roleCd;
	private String altYn;
}
