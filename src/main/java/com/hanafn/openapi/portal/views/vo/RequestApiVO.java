package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Alias("requestApi")
public class RequestApiVO {
	private String chargeDiscountRateId;
	private String appKey;
	private String apiId;
	private String stDt;
	private String enDt;
	private String discountRate;
	private String useFl;
	private String regUser;
	private String regUserId;
	private String modUser;
    private String modDttm;
	private String feeAmount;
	private String minimumUseNumber;
	private String miniMumCharges;
	private String apiNm;
	private String rnum;
	private String isEdited;


	private String month;
	private String msDate;
	private String mdDate;
	private String appNm;
	private String defaultCost;
	private String useCnt;
	private String totalCost;
	private String finalCost;
	private String comment;
	private String totCnt;
}
