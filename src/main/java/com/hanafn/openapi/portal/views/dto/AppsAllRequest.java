package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class AppsAllRequest {
    private String searchUserKey;    // 이용기관 코드
    private String searchHfnCd;      // 관계사코드
}
