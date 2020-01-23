package com.hanafn.openapi.portal.views.service;

import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.LogRepository;
import com.hanafn.openapi.portal.views.vo.ApiLogVO;
import com.hanafn.openapi.portal.views.vo.PortalLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class LogService {

	private final LogRepository logRepository;

	public PortalLogVO selectPortalLog(PortalLogRequest.PortalLogDetailRequest portalLogRequest){
		return logRepository.selectPortalLog(portalLogRequest);
	}
	public PortalLogRsponsePaging selectPortalLogListPaging(PortalLogRequest portalLogRequest){
		if(portalLogRequest.getPageIdx() == 0)
			portalLogRequest.setPageIdx(portalLogRequest.getPageIdx() + 1);

		if(portalLogRequest.getPageSize() == 0){
			portalLogRequest.setPageSize(20);
		}

		portalLogRequest.setPageOffset((portalLogRequest.getPageIdx()-1)*portalLogRequest.getPageSize());

		int totCnt = logRepository.countPortalLogList(portalLogRequest);
		List<PortalLogVO> list = logRepository.selectPortalLogList(portalLogRequest);

		PortalLogRsponsePaging pagingData = new PortalLogRsponsePaging();
		pagingData.setPageIdx(portalLogRequest.getPageIdx());
		pagingData.setPageSize(portalLogRequest.getPageSize());
		pagingData.setList(list);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	public void insertPortalLog(LogRequest logRequest){
		logRepository.insertPortalLog(logRequest);
	}

	public ApiLogVO selectApiLog(ApiLogRequest.ApiLogDetailRequest apiLogDetailRequest){
		return logRepository.selectApiLog(apiLogDetailRequest);
	}
	public ApiLogRsponsePaging selectApiLogListPaging(ApiLogRequest apiLogRequest){
		if(apiLogRequest.getPageIdx() == 0)
			apiLogRequest.setPageIdx(apiLogRequest.getPageIdx() + 1);

		if(apiLogRequest.getPageSize() == 0){
			apiLogRequest.setPageSize(20);
		}

		apiLogRequest.setPageOffset((apiLogRequest.getPageIdx()-1)*apiLogRequest.getPageSize());

		int totCnt = logRepository.countApiLogList(apiLogRequest);
		List<ApiLogVO> list = logRepository.selectApiLogList(apiLogRequest);

		ApiLogRsponsePaging pagingData = new ApiLogRsponsePaging();
		pagingData.setPageIdx(apiLogRequest.getPageIdx());
		pagingData.setPageSize(apiLogRequest.getPageSize());
		pagingData.setList(list);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

}
