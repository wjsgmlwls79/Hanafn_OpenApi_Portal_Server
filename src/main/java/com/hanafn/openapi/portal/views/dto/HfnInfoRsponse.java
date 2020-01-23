package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.ApiCtgrVO;
import com.hanafn.openapi.portal.views.vo.HfnInfoVO;
import lombok.Data;

import java.util.List;

@Data
public class HfnInfoRsponse {
    private List<HfnInfoVO> list;
}
