package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("notice")
public class NoticeVO {
	private int seqNo;
	private String noticeSubject;
	private String noticeCtnt;
	private int viewCnt;
	private String regUser;
	private String regId;
	private String regDttm;
	private String modUser;
	private String modId;
	private String modDttm;
	private String statCd;
}
