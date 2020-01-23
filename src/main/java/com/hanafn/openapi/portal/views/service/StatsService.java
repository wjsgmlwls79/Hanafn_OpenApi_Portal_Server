package com.hanafn.openapi.portal.views.service;

import com.hanafn.openapi.portal.util.DateUtil;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.repository.StatsRepository;
import com.hanafn.openapi.portal.views.vo.AppsVO;
import com.hanafn.openapi.portal.views.vo.DashBoardVO;
import com.hanafn.openapi.portal.views.vo.StatsVO;
import com.hanafn.openapi.portal.views.vo.UseorgDashBoardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;
	private final SettingRepository settingRepository;
	private final AppsService appsService;

	public DashBoardRsponse dashBoard(DashBoardRequest dashBoardRequest){

		DashBoardVO dashBoardInfo = statsRepository.dashBoardCount(dashBoardRequest);
		DashBoardVO dashBoardThreeDayTotalInfo = statsRepository.dashBoardThreeDayTotal(dashBoardRequest);

		List<DashBoardVO> topApiUseList = statsRepository.dashBoardThreeDayTopApiUse(dashBoardRequest);
		List<DashBoardVO> topResTmList = statsRepository.dashBoardThreeDayTopProcTerm(dashBoardRequest);
		List<DashBoardVO> topApiErrorList = statsRepository.dashBoardThreeDayTopApiError(dashBoardRequest);

		List<DashBoardVO> dashBoardThreeDayList = statsRepository.dashBoardThreeDayList(dashBoardRequest);

		DashBoardRsponse dashBoardRsponse = new DashBoardRsponse();

		dashBoardRsponse.setApiUseCnt(dashBoardInfo.getApiUseCnt());
		dashBoardRsponse.setApiCloseCnt(dashBoardInfo.getApiCloseCnt());
		dashBoardRsponse.setUseorgOkCnt(dashBoardInfo.getUseorgOkCnt());
		dashBoardRsponse.setUseorgCloseCnt(dashBoardInfo.getUseorgCloseCnt());
		dashBoardRsponse.setAppOkCnt(dashBoardInfo.getAppOkCnt());
		dashBoardRsponse.setAppCloseCnt(dashBoardInfo.getAppCloseCnt());
		dashBoardRsponse.setAppExpireCnt(dashBoardInfo.getAppExpireCnt());
		dashBoardRsponse.setAplvWaitCnt(dashBoardInfo.getAplvWaitCnt());

		dashBoardRsponse.setApiTrxCnt(dashBoardThreeDayTotalInfo.getApiTrxCnt());
		dashBoardRsponse.setAvgProcTerm(dashBoardThreeDayTotalInfo.getAvgProcTerm());
		dashBoardRsponse.setGwError(dashBoardThreeDayTotalInfo.getGwError());
		dashBoardRsponse.setApiError(dashBoardThreeDayTotalInfo.getApiError());
		dashBoardRsponse.setMaxProcTerm(dashBoardThreeDayTotalInfo.getMaxProcTerm());

		dashBoardRsponse.setTopApiUseList(topApiUseList);
		dashBoardRsponse.setTopProcTermList(topResTmList);
		dashBoardRsponse.setTopApiErrorList(topApiErrorList);
		dashBoardRsponse.setDashBoardDayList(dashBoardThreeDayList);

		return dashBoardRsponse;
	}

	public DashBoardRsponse.UseorgDashBoardRsponse useorgDashBoard(DashBoardRequest.UseorgDashBoardRequest useorgDashBoardRequest){

		AppsRequest request = new AppsRequest();

		request.setSearchUserKey(useorgDashBoardRequest.getUserKey());

		List<AppsVO> appModList = appsService.selectUserPortalAppList(request);
	    // 상태별 앱 ,기간만료
		UseorgDashBoardVO useorgDashBoardInfo = statsRepository.useorgDashBoardCount(useorgDashBoardRequest);
        // 당월 앱 사용정보
		List<UseorgDashBoardVO> appApiTrxList = statsRepository.useorgDashBoardAppApiTrxList(useorgDashBoardRequest);
		// 당월 API 사용정보
		List<UseorgDashBoardVO> apiTrxList = statsRepository.useorgDashBoardApiTrxList(useorgDashBoardRequest);
        // 당일 API 사용정보
		List<UseorgDashBoardVO> useorgDashBoardThreeDayList = statsRepository.useorgDashBoardThreeDayList(useorgDashBoardRequest);

		DashBoardRsponse.UseorgDashBoardRsponse useorgDashBoardRsponse = new DashBoardRsponse.UseorgDashBoardRsponse();

		useorgDashBoardRsponse.setUseorgNm(useorgDashBoardInfo.getUseorgNm());
		useorgDashBoardRsponse.setAppTotalCnt(useorgDashBoardInfo.getAppTotalCnt());
		useorgDashBoardRsponse.setAppWaitCnt(useorgDashBoardInfo.getAppWaitCnt());
		useorgDashBoardRsponse.setAppOkCnt(useorgDashBoardInfo.getAppOkCnt());
		useorgDashBoardRsponse.setAppCloseCnt(useorgDashBoardInfo.getAppCloseCnt());
		useorgDashBoardRsponse.setAppExpireCnt(useorgDashBoardInfo.getAppExpireCnt());
		useorgDashBoardRsponse.setAppExpireAplvWaitCnt(useorgDashBoardInfo.getAppExpireAplvWaitCnt());
		useorgDashBoardRsponse.setAppExpireExpectCnt(useorgDashBoardInfo.getAppExpireExpectCnt());


		// My apps
		List<UseorgDashBoardVO> useorgApiTrxMonthInfo = statsRepository.useorgApiTrxMonthInfo(useorgDashBoardRequest);

		List<UseorgDashBoardVO> useorgApiTrxMonthMod = statsRepository.useorgApiTrxMonthMod(useorgDashBoardRequest);

		boolean dupCheck = false;
		for(UseorgDashBoardVO appVO : useorgApiTrxMonthInfo){
			for(UseorgDashBoardVO appModVO : useorgApiTrxMonthMod){
				if(StringUtils.equals(appVO.getAppKey(),appModVO.getAppKey())) {
					dupCheck = true;
					break;
				}
				dupCheck = false;
			}
			if(dupCheck != true) {
				useorgApiTrxMonthMod.add(appVO);
			}
		}

		if (useorgApiTrxMonthMod.size() > 0) {
			useorgApiTrxMonthMod.get(0).setRnum(String.valueOf(useorgApiTrxMonthMod.size()));
		}

		useorgDashBoardRsponse.setMyAppList(useorgApiTrxMonthMod);

		useorgDashBoardRsponse.setAppApiTrxList(appApiTrxList);
		useorgDashBoardRsponse.setApiTrxList(apiTrxList);
		useorgDashBoardRsponse.setDashBoardDayList(useorgDashBoardThreeDayList);
		useorgDashBoardRsponse.setAppModList(appModList);

		return useorgDashBoardRsponse;
	}

	public List<UseorgDashBoardVO> defaultUseorgDashBoardDay(){
		List<UseorgDashBoardVO> threeDayList = new ArrayList<>();

		for(int day=-2; day<=0; day++){
			String date = DateUtil.getDate(day);
			for(int tm=0; tm<24; tm++) {
				// 시간별 정보 초기화
				UseorgDashBoardVO useorgDashBoardInfo = new UseorgDashBoardVO();
				useorgDashBoardInfo.setDay(date);
				useorgDashBoardInfo.setTm(String.valueOf(tm + 1));
				useorgDashBoardInfo.setApiTrxCnt("0");
				useorgDashBoardInfo.setProcTerm("0.0");
				useorgDashBoardInfo.setApiError("0");

				threeDayList.add(useorgDashBoardInfo);
			}
		}

		return threeDayList;
	}

	public StatsRsponse apiStats(StatsRequest apiStatsRequest){

		StatsVO dayTotalInfo = statsRepository.apiDayTotal(apiStatsRequest);
		List<StatsVO> apiTrxList = statsRepository.apiTrxDayList(apiStatsRequest);

		List<StatsVO> apiTrxDayList = defaultApiTrxDay(apiStatsRequest);

		for(StatsVO apiTrxInfo : apiTrxList){
			for(StatsVO apiTrxDayInfo :apiTrxDayList){
				if(StringUtils.equals(apiTrxInfo.getDay(), apiTrxDayInfo.getDay())){
					apiTrxDayInfo.setApiTrxCnt(apiTrxInfo.getApiTrxCnt());
					apiTrxDayInfo.setProcTerm(apiTrxInfo.getProcTerm());
					apiTrxDayInfo.setApiError(apiTrxInfo.getApiError());
				}
			}
		}

		List<StatsVO> topApiUseList = statsRepository.apiStatsTopApiUse(apiStatsRequest);
		List<StatsVO> topProcTermList = statsRepository.apiStatsTopProcTerm(apiStatsRequest);
		List<StatsVO> topApiErrorList = statsRepository.apiStatsTopApiError(apiStatsRequest);

		StatsRsponse apiStatsRsponse = new StatsRsponse();
		apiStatsRsponse.setApiTrxCnt(dayTotalInfo.getApiTrxCnt());
		apiStatsRsponse.setAvgProcTerm(dayTotalInfo.getAvgProcTerm());
		apiStatsRsponse.setGwError(dayTotalInfo.getGwError());
		apiStatsRsponse.setApiError(dayTotalInfo.getApiError());
		apiStatsRsponse.setMaxProcTerm(dayTotalInfo.getMaxProcTerm());

		apiStatsRsponse.setDayList(apiTrxDayList);

		apiStatsRsponse.setTopApiUseList(topApiUseList);
		apiStatsRsponse.setTopProcTermList(topProcTermList);
		apiStatsRsponse.setTopApiError(topApiErrorList);

		return apiStatsRsponse;
	}

	public StatsRsponse.UseorgStatsRsponse useorgStats(StatsRequest apiStatsRequest){

		String lastUpdateDate = statsRepository.getLastUpdateDate();
		StatsVO dayTotalInfo = statsRepository.apiDayTotal(apiStatsRequest);
		List<StatsVO> apiTrxList = statsRepository.apiTrxDayList(apiStatsRequest);

		List<StatsVO> apiTrxDayList = defaultApiTrxDay(apiStatsRequest);

		for(StatsVO apiTrxInfo : apiTrxList){
			for(StatsVO apiTrxDayInfo :apiTrxDayList){
				if(StringUtils.equals(apiTrxInfo.getDay(), apiTrxDayInfo.getDay())){
					apiTrxDayInfo.setApiTrxCnt(apiTrxInfo.getApiTrxCnt());
					apiTrxDayInfo.setProcTerm(apiTrxInfo.getProcTerm());
					apiTrxDayInfo.setApiError(apiTrxInfo.getApiError());
				}
			}
		}

		List<StatsVO> appTrxList = statsRepository.appTrxList(apiStatsRequest);

		StatsRsponse.UseorgStatsRsponse useorgStatsRsponse = new StatsRsponse.UseorgStatsRsponse();
		useorgStatsRsponse.setApiTrxCnt(dayTotalInfo.getApiTrxCnt());
		useorgStatsRsponse.setAvgProcTerm(dayTotalInfo.getAvgProcTerm());
		useorgStatsRsponse.setGwError(dayTotalInfo.getGwError());
		useorgStatsRsponse.setApiError(dayTotalInfo.getApiError());
		useorgStatsRsponse.setMaxProcTerm(dayTotalInfo.getMaxProcTerm());

		useorgStatsRsponse.setDayList(apiTrxDayList);
		useorgStatsRsponse.setAppTrxList(appTrxList);
		useorgStatsRsponse.setLastUpdateDate(lastUpdateDate);

		return useorgStatsRsponse;
	}

	public List<StatsVO> defaultApiTrxDay(StatsRequest apiStatsRequest){
		List<StatsVO> dayList = new ArrayList<>();

		int dayDiff = DateUtil.getDayDiff(apiStatsRequest.getSearchStDt(), apiStatsRequest.getSearchEnDt());

		for(int i=0; i<=dayDiff; i++){
			String date = DateUtil.addDay(apiStatsRequest.getSearchStDt(), i);

			StatsVO dayInfo = new StatsVO();
			dayInfo.setDay(DateUtil.formatDateTime(date, "yyyyMMdd"));
			dayInfo.setApiTrxCnt("0");
			dayInfo.setProcTerm("0.0");
			dayInfo.setApiError("0");

			dayList.add(dayInfo);
		}

		return dayList;
	}

	public StatsRsponse.AppApiDetailStatsRsponse useorgAppDetailStats(StatsRequest.AppDetailStatsRequest appDetailStatsRequest){

		AppsRequest appsRequest = new AppsRequest();
		appsRequest.setAppKey(appDetailStatsRequest.getAppKey());

		AppsVO appInfo = settingRepository.selectAppDetailInfo(appsRequest);
		//int appApiCnt = settingRepository.selectAppApiCnt(appsRequest);

		List<StatsVO> AppApiDetailStatsList = statsRepository.useorgAppApiDetailStatsList(appDetailStatsRequest);
		StatsRsponse.AppApiDetailStatsRsponse appApiDetailStatsRsponse = new StatsRsponse.AppApiDetailStatsRsponse();
		appApiDetailStatsRsponse.setAppKey(appInfo.getAppKey());
		appApiDetailStatsRsponse.setAppNm(appInfo.getAppNm());
		appApiDetailStatsRsponse.setAppApiCnt(AppApiDetailStatsList.size());
		appApiDetailStatsRsponse.setApiStatsList(AppApiDetailStatsList);

		return appApiDetailStatsRsponse;
	}

}
