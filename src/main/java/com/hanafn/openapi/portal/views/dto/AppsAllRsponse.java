package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.AppsVO;
import lombok.Data;

import java.util.List;

@Data
public class AppsAllRsponse {
    private List<AppsVO> list;

    @Data
    public static class devGuidesAppsAllRsponse{
        private String entrCd;
        private List<AppsVO> list;
    }
}
