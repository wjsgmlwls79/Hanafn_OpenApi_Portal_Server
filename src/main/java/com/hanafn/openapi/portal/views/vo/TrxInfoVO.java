package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("trxInfo")
public class TrxInfoVO {
    private String trxCd;
    private String trxNm;
    private String trxGrant;
    private String regDttm;
    private String regUser;
    private String modDttm;
    private String modUser;
}
