package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("portalLog")
public class PortalLogVO {
	private String trxId;
	private String regDttm;
	private String trxCd;
	private String trxNm;
	private String userId;
	private String roleCd;
	private String procStatCd;
	private String inputCtnt;
	private String outputCtnt;
	private String regUser;
}
