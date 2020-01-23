package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("echo")
public class EchoVO {
    private String seq;
    private String apiId;
    private String apiNm;
    private String statCd;
    private String apiUrl;
    private String apiEcho;
    private String searchKey;
    private String searchValue;
    private String regUser;
    private String regDttm;
    private String modUser;
    private String modDttm;
}
