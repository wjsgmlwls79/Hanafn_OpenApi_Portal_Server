package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiDevGuideVO;
import lombok.Data;

import java.util.List;

@Data
public class ApiDevGuideRsponse {

    @Data
    public static class AplvDetilResponse {
        private List<ApiDevGuideVO> ApiDevGuideList;
    }

}
