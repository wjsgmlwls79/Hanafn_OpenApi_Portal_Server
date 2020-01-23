package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("FeeCollectionInfo")
public class FeeCollectionInfoVO {

	private String bilSeq;		// 청구일련번호
	private String bilMonth;	// 청구월
	private String hfnCd;		// 관계사코드
	private String useorgCd;	// 이용기관코드
	private String useorgNm;	// 이용기관명
	private String appKey;		// 앱키
	private String appNm;		// 앱명
	private String totAmt;		// 총금액
	private String wdAmt;		// 출금금액
	private String wdAcno;		// 출금계좌번호
	private String wdMemo;		// 출금계좌적요내용
	private String dipoDivCd;	// 입금구분코드
	private String dipoAcno;	// 입금계좌번호
	private String aplvYn;		// 승인여부
	private String fullpayYn;	// 완납여부
	private String unpaidAmt;	// 미납금액
	private String receiptPrintDttm;	// 영수증 출력일시
	private String regDttm;		// 등록일시
	private String regUser;		// 등록자
	private String modDttm;		// 수정일시
	private String modUser;		// 수정자
	private String totCnt;
	private String accNo;
	private String accCd;
	private String hisSeq;		// 이력일련번호
	private String userKey;
	private String wdStatCd;	// 출금상태
	private String wdRequestAmt; // 출금요청금액
}
