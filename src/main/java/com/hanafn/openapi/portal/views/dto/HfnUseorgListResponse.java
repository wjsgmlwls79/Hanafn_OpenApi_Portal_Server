package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.UseorgVO;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.util.List;

@Data
public class HfnUseorgListResponse {
    private List<UseorgVO> list;
}
