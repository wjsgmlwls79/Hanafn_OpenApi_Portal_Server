package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiColumnVO;
import com.hanafn.openapi.portal.views.vo.ApiVO;
import com.hanafn.openapi.portal.views.vo.EchoVO;
import lombok.Data;

import java.util.List;

@Data
public class EchoResponse {
    private EchoVO echo;

    @Data
    public static class EchoListResponse {
        private List<EchoVO> echoList;
        private int totCnt;
        private int selCnt;
    }

    @Data
    public static class ApisResponse {
        private List<ApiVO> apis;
    }

    @Data
    public static class ApiDetailResponse {
        private List<ApiColumnVO> columns;
    }

}
