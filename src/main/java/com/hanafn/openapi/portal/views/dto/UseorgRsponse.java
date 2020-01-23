package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.UseorgVO;
import lombok.Data;

import java.util.List;

@Data
public class UseorgRsponse {
    private List<UseorgVO> list;

    @Data
    public static class UseorgDupCheckResponse{
        private String brnDupYn;
    }

    @Data
    public static class UseorgIdDupCheckResponse{
        private int idDupCheck;
    }
}
