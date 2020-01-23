package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class DashBoardRequest {
    private String hfnCd;
    private String userKey;

    @Data
    public static class UseorgDashBoardRequest {
        private String userKey;
        private String entrCd;
    }
}
