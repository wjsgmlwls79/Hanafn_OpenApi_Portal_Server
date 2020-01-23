package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiVO;
import lombok.Data;

import java.util.List;

@Data
public class EchoRequest {
    private String searchNm;
    private String searchHfnCd;

    private int pageIdx;
    private int pageSize;

    @Data
    public static class ApisRequest {
        private String searchNm;
        private String searchHfnCd;
    }

    @Data
    public static class ApiDetailRequest {
        private String apiId;
        private String searchHfnCd;
    }

    @Data
    public static class RegEchoRequest {
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

    @Data
    public static class RegSearchKeyRequest {
        private String apiId;
        private String searchKey;
        private String regUser;
        private String regDttm;
        private String modUser;
        private String modDttm;
    }
}
