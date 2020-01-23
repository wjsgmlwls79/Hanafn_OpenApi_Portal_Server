package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ApiSubCtgrRequest {
    private String searchNm;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;
    private String ctgrCd;

    @Data
    public static class ApiSubCtgrDetilRequest{

        @NotBlank
        private String subCtgrCd;
    }

    @Data
    public static class ApiSubCtgrRegistRequest{

        @NotNull
        private String ctgrCd;

        private String subCtgrCd;

        @NotBlank
        private String subCtgrNm;

        private String subCtgrCtnt;

        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class ApiSubCtgrUpdateRequest{

        @NotNull
        private String ctgrCd;

        private String subCtgrCd;

        @NotBlank
        private String subCtgrNm;

        private String subCtgrCtnt;

        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class ApiSubCtgrDeleteRequest{

        @NotBlank
        private String subCtgrCd;

        private String regUserId;
        private String regUserName;
    }

}
