package com.hanafn.openapi.portal.views.repository;

import com.hanafn.openapi.portal.views.dto.ApiDevGuideRequest;
import com.hanafn.openapi.portal.views.dto.AppsAllRequest;
import com.hanafn.openapi.portal.views.vo.ApiCtgrVO;
import com.hanafn.openapi.portal.views.vo.ApiDevGuideVO;
import com.hanafn.openapi.portal.views.vo.AppsVO;
import com.hanafn.openapi.portal.views.vo.UseorgVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GuideRepository {
    /*
     * ******************************개발 가이드******************************
     * */
    List<ApiDevGuideVO> selectApiDevGuideList(ApiDevGuideRequest apiDevGuideRequest);
    List<ApiDevGuideVO> selectApiDevGuideAllList(ApiDevGuideRequest apiStatModRequest);
    List<AppsVO> selectAppsAll(AppsAllRequest appsAllRequest);

    List<UseorgVO> selectDevGuidesUseorgAll(ApiDevGuideRequest.ApiDevGuideUseorgAllRequest apiDevGuideUseorgAllRequest);

    List<ApiCtgrVO> selectDevGuidesApiCtgrForUseorgList(ApiDevGuideRequest apiDevGuideRequest);
    List<ApiCtgrVO> selectDevGuidesApiCtgrAll(ApiDevGuideRequest apiDevGuideRequest);

    List<AppsVO> selectDevGuidesAppsAll(ApiDevGuideRequest.ApiDevGuideApiAllRequest apiDevGuideApiAllRequest);
    String selectEnckeyByEntrCd(String value);
}
