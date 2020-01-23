package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ApiCtgrRequest {
    private String searchNm;
    private String searchHfnCd;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;

    @Data
    public static class ApiCtgrDetilRequest{

        @NotBlank
        private String ctgrCd;
    }

    @Data
    public static class ApiCtgrDetilRequestWithHfn{

        @NotBlank
        private String ctgrCd;
        private String hfnCd;
    }

    @Data
    public static class ApiCtgrRegistRequest{

        private String ctgrCd;

        @NotBlank
        private String ctgrNm;

        private String ctgrCtnt;

        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class ApiCtgrUpdateRequest{

        private String ctgrCd;

        @NotBlank
        private String ctgrNm;

        private String ctgrCtnt;

        private String regUserName;
        private String regUserId;
    }

    @Data
    public static class ApiCtgrDeleteRequest{

        @NotBlank
        private String ctgrCd;

        private String regUserId;
        private String regUserName;
    }

}
