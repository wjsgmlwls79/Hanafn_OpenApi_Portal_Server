package com.hanafn.openapi.portal.views.repository;

import com.hanafn.openapi.portal.views.dto.ApiLogRequest;
import com.hanafn.openapi.portal.views.dto.LogRequest;
import com.hanafn.openapi.portal.views.dto.PortalLogRequest;
import com.hanafn.openapi.portal.views.vo.ApiLogVO;
import com.hanafn.openapi.portal.views.vo.PortalLogVO;
import com.hanafn.openapi.portal.views.vo.TrxInfoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LogRepository {
	PortalLogVO selectPortalLog(PortalLogRequest.PortalLogDetailRequest portalLogRequest);

	int countPortalLogList(PortalLogRequest portalLogRequest);
	List<PortalLogVO> selectPortalLogList(PortalLogRequest portalLogRequest);

	void insertPortalLog(LogRequest logRequest);

	ApiLogVO selectApiLog(ApiLogRequest.ApiLogDetailRequest apiLogDetailRequest);

	int countApiLogList(ApiLogRequest apiLogRequest);
	List<ApiLogVO> selectApiLogList(ApiLogRequest apiLogRequest);

	List<TrxInfoVO> selectTrxInfoAll();
}
