package com.hanafn.openapi.portal.event;

import com.hanafn.openapi.portal.views.dto.AppsRsponse;
import com.hanafn.openapi.portal.views.vo.ApiVO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class AppApiChangeDTO {
    List<ApiVO> newApiList;
    List<ApiVO> oldApiList;
    AppsRsponse newAppsRsponse;
}
