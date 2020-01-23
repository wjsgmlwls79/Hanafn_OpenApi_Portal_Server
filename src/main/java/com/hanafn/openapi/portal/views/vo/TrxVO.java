package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("trx")
public class TrxVO {
    private String trxCd;
    private String trxNm;
    private String trxGrant;
    private String regUser;
    private String regDttm;
    private String modUser;
    private String modDttm;
}
