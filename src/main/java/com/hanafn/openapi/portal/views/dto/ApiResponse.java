package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiVO;
import lombok.Data;

import java.util.List;

@Data
public class ApiResponse {
    private List<ApiVO> list;
}
