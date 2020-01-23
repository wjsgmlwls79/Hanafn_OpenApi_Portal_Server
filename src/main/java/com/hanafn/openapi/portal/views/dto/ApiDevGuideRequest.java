package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiCtgrVO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;


@Data
public class ApiDevGuideRequest {

    private String searchHfnCd;
    private String searchUserKey;
    private String searchAppKey;
    private String searchNm;
    private List<ApiCtgrVO> searchApiCtgrList;
    private String searchApiCtgrs;
    private String searchCtgrCd;
    private String searchSubCtgrCd;

    @Data
    public static class ApiDevGuideUseorgAllRequest{
        @NotBlank
        private String searchApiId;
    }
    
    @Data
    public static class ApiDevGuideApiAllRequest{
        @NotBlank
        private String searchApiId;
        @NotBlank
        private String searchUserKey;
    }

}
