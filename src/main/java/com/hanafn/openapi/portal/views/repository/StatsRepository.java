package com.hanafn.openapi.portal.views.repository;

import com.hanafn.openapi.portal.views.dto.DashBoardRequest;
import com.hanafn.openapi.portal.views.dto.StatsRequest;
import com.hanafn.openapi.portal.views.vo.DashBoardVO;
import com.hanafn.openapi.portal.views.vo.StatsVO;
import com.hanafn.openapi.portal.views.vo.UseorgDashBoardVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StatsRepository {
	/* 대시 보드 */
	DashBoardVO dashBoardCount(DashBoardRequest dashBoardRequest);
	DashBoardVO dashBoardThreeDayTotal(DashBoardRequest dashBoardRequest);

	List<DashBoardVO> dashBoardThreeDayTopApiUse(DashBoardRequest dashBoardRequest);
	List<DashBoardVO> dashBoardThreeDayTopProcTerm(DashBoardRequest dashBoardRequest);
	List<DashBoardVO> dashBoardThreeDayTopApiError(DashBoardRequest dashBoardRequest);

	List<DashBoardVO> dashBoardThreeDayList(DashBoardRequest dashBoardRequest);

	/* 이용기관 대시 보드 */
	UseorgDashBoardVO useorgDashBoardCount(DashBoardRequest.UseorgDashBoardRequest useorgDashBoardRequest);
	List<UseorgDashBoardVO> useorgApiTrxMonthInfo(DashBoardRequest.UseorgDashBoardRequest useorgDashBoardRequest);
	List<UseorgDashBoardVO> useorgApiTrxMonthMod(DashBoardRequest.UseorgDashBoardRequest useorgDashBoardRequest);

	List<UseorgDashBoardVO> useorgDashBoardAppApiTrxList(DashBoardRequest.UseorgDashBoardRequest dashBoardRequest);
	List<UseorgDashBoardVO> useorgDashBoardApiTrxList(DashBoardRequest.UseorgDashBoardRequest dashBoardRequest);

	List<UseorgDashBoardVO> useorgDashBoardThreeDayList(DashBoardRequest.UseorgDashBoardRequest dashBoardRequest);

	/* API 통계 */
	StatsVO apiDayTotal(StatsRequest apiStatsRequest);
	List<StatsVO> apiTrxDayList(StatsRequest apiStatsRequest);

	List<StatsVO> apiStatsTopApiUse(StatsRequest apiStatsRequest);
	List<StatsVO> apiStatsTopProcTerm(StatsRequest apiStatsRequest);
	List<StatsVO> apiStatsTopApiError(StatsRequest apiStatsRequest);

	/* 이용관리 통계 */
	List<StatsVO> appTrxList(StatsRequest apiStatsRequest);
	List<StatsVO> useorgAppApiDetailStatsList(StatsRequest.AppDetailStatsRequest appDetailStatsRequest);

	// 최종 업데이트 일자
	String getLastUpdateDate();

}
