package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("appCryptoKey")
public class AppCryptoKeyVO {
    private String hfnCd;
    private String appNm;
    private String appKey;
    private String keyPath;
    private String regUser;
    private String regDttm;
    private String modUser;
    private String modDttm;
}
