package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.RequestApiVO;
import lombok.Data;

import java.util.List;

@Data
public class ChargeDiscountRateListRequest {

    private String regUser;
    private String modUser;
    private List<RequestApiVO> list;
}
