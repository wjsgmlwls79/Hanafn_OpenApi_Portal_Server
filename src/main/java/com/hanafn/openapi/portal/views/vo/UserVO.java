package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("user")
public class UserVO {
	private String userKey;
	private String userId;
	private String userNm;
	private String useorgNm;
	private String userPwd;
	private String userGb;
	private String userEmail;
	private String userCompany;
	private String userJobNm;
	private String userStatCd;
	private String tmpPwdYn;
	private String userTel;
	private String telAuthNo;
	private String entrCd;
	private String regDttm;
	private String regUser;
	private String modDttm;
	private String modUser;
	private String roleCd;
	private String userType;
	private String seq;
	private String authInputNum;
	private String sendNo;
	private String pwChangeDt;
	private String joinDate;
	private String expireDttm;
	private boolean expire;
}
