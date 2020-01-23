package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.HfnLineVO;
import com.hanafn.openapi.portal.views.vo.HfnUserVO;
import lombok.Data;
import java.util.List;

@Data
public class HfnUserRsponse {
    private HfnUserVO hfnUserVO;
    private HfnLineVO hfnLineVO;
    private List<HfnUserVO> list;

    @Data
    public static class HfnAltUsersResponse {
        private List<HfnLineVO> hfnUserAvaliableAltList;
    }
}
