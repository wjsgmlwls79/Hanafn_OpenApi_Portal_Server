package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class ApiTagRequest {

    private Long tagOrd;
    private String apiId;
    private String tagCd;
    private String regDttm;
    private String regUser;
    private String modDttm;
    private String modUser;
    private String regUserName;
    private String regUserId;

}
