package com.hanafn.openapi.portal.views.repository;

import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.vo.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ApiRepository {
	/*
	 * ******************************API CATEGORY******************************
	 * */
	ApiCtgrVO selectApiCtgr(ApiCtgrRequest.ApiCtgrDetilRequest apiCtgrDetilRequest);
//	ApiCtgrVO selectApiCtgrWithHfn(ApiCtgrRequest.ApiCtgrDetilRequestWithHfn apiCtgrDetilRequest);
	int countApiCtgrList(ApiCtgrRequest apiCtgrequest);
	List<ApiCtgrVO> selectApiCtgrList(ApiCtgrRequest apiCtgrRequest);
	void insertApiCtgr(ApiCtgrRequest.ApiCtgrRegistRequest apiCtgrRegistRequest);
	void updateApiCtgr(ApiCtgrRequest.ApiCtgrUpdateRequest apiCtgrUpdateRequest);
	int countApiCtgrUse(ApiCtgrRequest.ApiCtgrDeleteRequest apiCtgrDeleteRequest);
	void apiCtgrDelete(ApiCtgrRequest.ApiCtgrDeleteRequest apiCtgrDeleteRequest);

	// API과금집계목록조회
	List<RequestApiVO> getApiChargeList(ApiRequest request);

	// API과금집계목록조회
	String getApiChargeTotCnt(ApiRequest request);

	// API과금집계할인율 다건조회
	List<RequestApiVO> getApiDetailList(ApiRequest request);


	List<ApiCtgrVO> selectApiCtgrAllList(ApiCtgrRequest apiCtgrRequest);

	// API 관리 페이지용 관계사별 카테고리 세팅
	List<ApiCtgrVO> selectApiListCtgrs(ApiDevGuideRequest apiDevGuideRequest);

	/*
	 * ******************************API SUB CATEGORY******************************
	 * */

	ApiSubCtgrVO selectApiSubCtgr(ApiSubCtgrRequest.ApiSubCtgrDetilRequest apiSubCtgrDetilRequest);

	int countApiSubCtgrList(ApiSubCtgrRequest apiSubCtgrRequest);
	List<ApiSubCtgrVO> selectApiSubCtgrList(ApiSubCtgrRequest apiSubCtgrRequest);
	void insertApiSubCtgr(ApiSubCtgrRequest.ApiSubCtgrRegistRequest apiSubCtgrRegistRequest);
	void updateApiSubCtgr(ApiSubCtgrRequest.ApiSubCtgrUpdateRequest apiSubCtgrUpdateRequest);
	int countApiSubCtgrUse(ApiSubCtgrRequest.ApiSubCtgrDeleteRequest apiSubCtgrDeleteRequest);
	void apiSubCtgrDelete(ApiSubCtgrRequest.ApiSubCtgrDeleteRequest apiSubCtgrDeleteRequest);

	List<ApiSubCtgrVO> selectApiSubCtgrs(ApiSubCtgrRequest apiCtgeRequest);
	List<ApiSubCtgrVO> selectApiSubCtgrAllList(ApiSubCtgrRequest apiCtgeRequest);

	/*
	 * ******************************API******************************
	 * */

	int countApiList(ApiRequest apiRequest);
	List<ApiVO> selectApiList(ApiRequest apiRequest);
	List<ApiVO> selectApiListNoPaging(ApiRequest apiRequest);
	List<ApiCtgrVO> selectCtgrApiList(ApiRequest apiRequest);

	ApiVO selectApi(ApiRequest apiRequest);

	List<ApiTagVO> selectApiTagList(ApiRequest apiRequest);
	List<ApiColumnVO> selectApiColumnList(ApiColumnRequest apiColumnRequest);
	List<ApiColumnListVO> selectApiColumnDetailList(ApiRequest.ApiColumnListRequest apiColumnListRequest);
	List<ApiStatModHisVO> selectApiStatModHisList(ApiRequest apiRequest);

	int apiDupCheck(ApiRegistRequest apiRequest);
	void insertApi(ApiRegistRequest apiRequest);
	void insertApiTag(ApiTagRequest apiTagRequest);
	void insertApiColumn(ApiColumnRequest apiColumnRequest);
	void insertApiColumnList(ApiColumnListRequest apiColumnListRequest);

	void updateApi(ApiRegistRequest apiRequest);
	void deleteApi(ApiRequest apiRequest);
	void deleteApiTag(ApiRegistRequest apiRequest);
	void deleteApiColumn(ApiRegistRequest apiRequest);
	void deleteApiColumnList(ApiRegistRequest apiRequest);

	void apiStatCdChange(ApiRequest apiRequest);
	void insertApiStatModHis(ApiStatModRequest apiStatModRequest);
	void updateApiDlyTerm(ApiStatModRequest apiStatModRequest);
	ApiVO selectApiDetalInfo(ApiRequest apiRequest);
	List<ApiVO> selectApiAllList(ApiRequest.ApiAllListRequest apiAllListRequest);
	List<ApiVO> searchCtgrApiAll(ApiRequest.CtgrApiAllListRequest apiAllListRequest);

	/*
	 *******************************API 정책*******************************
	 */

	ApiPolicyVO selectApiPolicy(ApiPolicyRequest apiPolicyRequest);
	void insertApiPolicy(ApiPolicyRequest apiPolicyRequest);
	void updateApiPolicy(ApiPolicyRequest apiPolicyRequest);

	/*
	 *******************************NOTICE 관련******************************
	 */
	HashMap<String,Object> selectUseorgNoticeData(UseorgRequest.UseorgDetailRequest useorgRequest);
	HashMap<String,Object> selectAppNoticeData(AppsRequest appsRequest);
	HashMap<String,Object> selectAppApiAplvNoticeWait(AppsRequest.AppApiRequest request);
	HashMap<String,Object> selectAppApiAplvNoticeOriginal(AppsRequest.AppApiRequest request);

	/** 대응답 정보 처리 **/
	List<EchoVO> selectApiEcho(EchoRequest request);
	List<ApiVO> selectApisForEcho(EchoRequest.ApisRequest request);
	void regApiEcho(EchoRequest.RegEchoRequest request);
	void updateApiEcho(EchoRequest.RegEchoRequest request);
	ApiVO getApiInfo(EchoRequest.RegEchoRequest request);
	int checkApiEchoDup(EchoRequest.RegEchoRequest request);
	EchoVO detailApiEcho(EchoRequest.RegEchoRequest request);
	List<EchoVO> apiEchos(EchoRequest.RegEchoRequest request);
	void deleteApiEcho(EchoRequest.RegEchoRequest request);
	void deleteApiEchoAll(EchoRequest.RegSearchKeyRequest request);
	void regApiSearchKey(EchoRequest.RegSearchKeyRequest request);
	void updateApiSearchKey(EchoRequest.RegSearchKeyRequest request);
	EchoVO getApiSearchKey(EchoRequest.RegSearchKeyRequest request);
}