package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.HfnUserRoleVO;
import lombok.Data;

import java.util.List;

@Data
public class HfnCompanyAllResponse {
    private List<HfnUserRoleVO> list;
}
