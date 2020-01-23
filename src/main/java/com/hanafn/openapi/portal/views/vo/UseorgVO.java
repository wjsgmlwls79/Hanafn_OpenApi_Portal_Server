package com.hanafn.openapi.portal.views.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

import java.util.List;

@Data
@Alias("useorg")
public class UseorgVO {

	private String userKey;
	private String useorgId;
	private String useorgNm;
	private String brn;
	private String encKey;
	private String useorgStatCd;
	private String entrCd;
	private String useorgGb;
	private String useorgTel;
	private String useorgUserNm;
	private String useorgUserEmail;
	private String useorgUserTel;
	private String useorgSelApi;
	private String useorgDomain;
	private String useorgBank;
	private String useorgBankNo;
	private String useorgUpload;
	private String appCount;
	private String develCount;
	private String totalCount;
	private String seq;

	private String useorgCtnt;
	private String errorMsg;
	private String regDttm;
	private String regUser;

	private String regId;
	private String modDttm;
	private String modUser;
	private String modId;

	private String hfnCd;
	private String hfnNm;

	private String hbnUseYn;
	private String hnwUseYn;
	private String hlfUseYn;
	private String hcpUseYn;
	private String hcdUseYn;
	private String hsvUseYn;
	private String hmbUseYn;

	private String reasonGb;
	private String reasonDetail;

	private String orgAuthInputNum;
	private String sendNo;
	private String appNm;
	private String expireDttm;

	private String approvalStatus;
}
