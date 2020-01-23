package com.hanafn.openapi.portal.views.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanafn.openapi.portal.cmct.RedisCommunicater;
import com.hanafn.openapi.portal.cmct.SwaggerCommunicater;
import com.hanafn.openapi.portal.cmct.dto.SwaggerResponse;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.util.ExcelUtil;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.ApiRepository;
import com.hanafn.openapi.portal.views.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ApiService {
	private final String apiStatCdDel="DEL";
	private final String apiStatCdClose="CLOSE";
	private final String apiStatCdOk="OK";
	private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ApiRepository apiRepository;

	@Autowired
	MessageSourceAccessor messageSource;

	@Autowired
    ExcelUtil excelUtil;

	/*
	 * ******************************API CATEGORY******************************
	 * */
	public ApiCtgrVO selectApiCtgr(ApiCtgrRequest.ApiCtgrDetilRequest apiCtgrDetilRequest){
		return apiRepository.selectApiCtgr(apiCtgrDetilRequest);
	}

	public ApiCtgrRsponsePaging selectApiCtgrListPaging(ApiCtgrRequest apiCtgrRequest){
		if(apiCtgrRequest.getPageIdx() == 0)
			apiCtgrRequest.setPageIdx(apiCtgrRequest.getPageIdx() + 1);

		if(apiCtgrRequest.getPageSize() == 0){
			apiCtgrRequest.setPageSize(20);
		}

		apiCtgrRequest.setPageOffset((apiCtgrRequest.getPageIdx()-1)*apiCtgrRequest.getPageSize());

		int totCnt = apiRepository.countApiCtgrList(apiCtgrRequest);
		List<ApiCtgrVO> list = apiRepository.selectApiCtgrList(apiCtgrRequest);

		ApiCtgrRsponsePaging pagingData = new ApiCtgrRsponsePaging();
		pagingData.setPageIdx(apiCtgrRequest.getPageIdx());
		pagingData.setPageSize(apiCtgrRequest.getPageSize());
		pagingData.setList(list);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	public void insertApiCtgr(ApiCtgrRequest.ApiCtgrRegistRequest apiCtgrRegistRequest){
		apiRepository.insertApiCtgr(apiCtgrRegistRequest);
	}

	public void updateApiCtgr(ApiCtgrRequest.ApiCtgrUpdateRequest apiCtgrUpdateRequest){
		apiRepository.updateApiCtgr(apiCtgrUpdateRequest);
	}

	public void apiCtgrDelete(ApiCtgrRequest.ApiCtgrDeleteRequest apiCtgrDeleteRequest){
		//사용이 없는 카테고리만 삭제 가능
		int apiUseCnt = apiRepository.countApiCtgrUse(apiCtgrDeleteRequest);

		if(apiUseCnt > 0){
			log.error("API 카테고리 삭제 불가처리 : " + messageSource.getMessage("E012"));
            throw new BusinessException("E012",messageSource.getMessage("E012"));
		}

		try {
			apiRepository.apiCtgrDelete(apiCtgrDeleteRequest);
		} catch ( Exception e ) {
			log.error("API 카테고리 삭제 불가처리 : " + messageSource.getMessage("E012"));
			throw new BusinessException("E012",messageSource.getMessage("E012"));
		}
	}

	public ApiCtgrRsponse selectApiCtgrAllList(ApiCtgrRequest apiCtgrRequest){

		List<ApiCtgrVO> list = apiRepository.selectApiCtgrAllList(apiCtgrRequest);

		ApiCtgrRsponse data = new ApiCtgrRsponse();
		data.setList(list);

		return data;
	}

	/*
	 * ******************************API SUB CATEGORY******************************
	 * */
	public ApiSubCtgrVO selectApiSubCtgr(ApiSubCtgrRequest.ApiSubCtgrDetilRequest apiSubCtgrDetilRequest){
		return apiRepository.selectApiSubCtgr(apiSubCtgrDetilRequest);
	}
	public ApiSubCtgrRsponsePaging selectApiSubCtgrListPaging(ApiSubCtgrRequest apiSubCtgrRequest){
		if(apiSubCtgrRequest.getPageIdx() == 0)
			apiSubCtgrRequest.setPageIdx(apiSubCtgrRequest.getPageIdx() + 1);

		if(apiSubCtgrRequest.getPageSize() == 0){
			apiSubCtgrRequest.setPageSize(20);
		}

		apiSubCtgrRequest.setPageOffset((apiSubCtgrRequest.getPageIdx()-1)*apiSubCtgrRequest.getPageSize());

		int totCnt = apiRepository.countApiSubCtgrList(apiSubCtgrRequest);
		List<ApiSubCtgrVO> list = apiRepository.selectApiSubCtgrList(apiSubCtgrRequest);

		ApiSubCtgrRsponsePaging pagingData = new ApiSubCtgrRsponsePaging();
		pagingData.setPageIdx(apiSubCtgrRequest.getPageIdx());
		pagingData.setPageSize(apiSubCtgrRequest.getPageSize());
		pagingData.setList(list);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	public void insertApiSubCtgr(ApiSubCtgrRequest.ApiSubCtgrRegistRequest apiSubCtgrRegistRequest){
		apiRepository.insertApiSubCtgr(apiSubCtgrRegistRequest);
	}

	public void updateApiSubCtgr(ApiSubCtgrRequest.ApiSubCtgrUpdateRequest apiSubCtgrUpdateRequest){
		apiRepository.updateApiSubCtgr(apiSubCtgrUpdateRequest);
	}

	public void apiSubCtgrDelete(ApiSubCtgrRequest.ApiSubCtgrDeleteRequest apiSubCtgrDeleteRequest){
		//사용이 없는 하위 카테고리만 삭제 가능
		int apiUseCnt = apiRepository.countApiSubCtgrUse(apiSubCtgrDeleteRequest);

		if(apiUseCnt > 0){
			log.error(messageSource.getMessage("E052"));
            throw new BusinessException("E052",messageSource.getMessage("E052"));
		}
		apiRepository.apiSubCtgrDelete(apiSubCtgrDeleteRequest);
	}

	public ApiSubCtgrRsponse selectApiSubCtgrs(ApiSubCtgrRequest apiSubCtgrRequest){

		List<ApiSubCtgrVO> list = apiRepository.selectApiSubCtgrs(apiSubCtgrRequest);

		ApiSubCtgrRsponse data = new ApiSubCtgrRsponse();
		data.setList(list);

		return data;
	}

	public ApiSubCtgrRsponse selectApiSubCtgrAllList(ApiSubCtgrRequest apiSubCtgrRequest){

		List<ApiSubCtgrVO> list = apiRepository.selectApiSubCtgrAllList(apiSubCtgrRequest);

		ApiSubCtgrRsponse data = new ApiSubCtgrRsponse();
		data.setList(list);

		return data;
	}

	/*
	 * ******************************API******************************
	 * */

	public ApiDetailRsponse selectApi(ApiRequest apiRequest){
		ApiVO apiInfo = apiRepository.selectApi(apiRequest);

		List<ApiTagVO> apiTagList = apiRepository.selectApiTagList(apiRequest);

		ApiColumnRequest apiColumnRequest = new ApiColumnRequest();
		apiColumnRequest.setApiId(apiRequest.getApiId());

		apiColumnRequest.setClmReqDiv("REQUEST");
		List<ApiColumnVO> apiColumnRequestList = apiRepository.selectApiColumnList(apiColumnRequest);
		settingApiColumn(apiColumnRequestList);	//컬럼 타입이 LIST일 경우 하위 리스트 셋팅

		apiColumnRequest.setClmReqDiv("RESPONSE");
		List<ApiColumnVO> apiColumnResponseList = apiRepository.selectApiColumnList(apiColumnRequest);
		settingApiColumn(apiColumnResponseList); //컬럼 타입이 LIST일 경우 하위 리스트 셋팅

		List<ApiStatModHisVO> apiStatModHisList = apiRepository.selectApiStatModHisList(apiRequest);

		ApiDetailRsponse apiDetailResponse = new ApiDetailRsponse();
		apiDetailResponse.setApiId(apiInfo.getApiId());
		apiDetailResponse.setApiNm(apiInfo.getApiNm());
		apiDetailResponse.setApiStatCd(apiInfo.getApiStatCd());
		apiDetailResponse.setApiSvc(apiInfo.getApiSvc());
		apiDetailResponse.setApiVer(apiInfo.getApiVer());
		apiDetailResponse.setApiUri(apiInfo.getApiUri());
		apiDetailResponse.setApiUrl(apiInfo.getApiUrl());
		apiDetailResponse.setApiPubYn(apiInfo.getApiPubYn());
		apiDetailResponse.setDlyTermDiv(apiInfo.getDlyTermDiv());
		apiDetailResponse.setDlyTermDt(apiInfo.getDlyTermDt());
		apiDetailResponse.setDlyTermTm(apiInfo.getDlyTermTm());
		apiDetailResponse.setRegDttm(apiInfo.getRegDttm());
		apiDetailResponse.setRegUser(apiInfo.getRegUser());
		apiDetailResponse.setProcDttm(apiInfo.getProcDttm());
		apiDetailResponse.setProcUser(apiInfo.getProcUser());
		apiDetailResponse.setProcUser(apiInfo.getProcUser());
		apiDetailResponse.setProcUser(apiInfo.getProcUser());
		apiDetailResponse.setCtgrCd(apiInfo.getCtgrCd());
		apiDetailResponse.setSubCtgrCd(apiInfo.getSubCtgrCd());
		apiDetailResponse.setCtgrNm(apiInfo.getCtgrNm());
		apiDetailResponse.setApiMthd(apiInfo.getApiMthd());
		apiDetailResponse.setApiCtnt(apiInfo.getApiCtnt());
		apiDetailResponse.setUserKey(apiInfo.getUserKey());
		apiDetailResponse.setApiTosUrl(apiInfo.getApiTosUrl());
		apiDetailResponse.setHfnCd(apiInfo.getHfnCd());
		apiDetailResponse.setHfnSvcCd(apiInfo.getHfnSvcCd());
		apiDetailResponse.setSubCtgrCd(apiInfo.getSubCtgrCd());
		apiDetailResponse.setFeeAmount(apiInfo.getFeeAmount());
		apiDetailResponse.setApiProcType(apiInfo.getApiProcType());
		apiDetailResponse.setApiProcUrl(apiInfo.getApiProcUrl());
		apiDetailResponse.setGwType(apiInfo.getGwType());
		apiDetailResponse.setApiTagList(apiTagList);
		apiDetailResponse.setApiRequestList(apiColumnRequestList);
		apiDetailResponse.setApiResponseList(apiColumnResponseList);
		apiDetailResponse.setApiStatModHisList(apiStatModHisList);
		apiDetailResponse.setMinimumCharges(apiInfo.getMinimumCharges());
		apiDetailResponse.setMinimumUseNumber(apiInfo.getMinimumUseNumber());

		return apiDetailResponse;
	}

	public void settingApiColumn(List<ApiColumnVO> columnList) {
		for(ApiColumnVO apiColumn : columnList){

			if(StringUtils.equals(apiColumn.getClmType(), "LIST")){
				ApiRequest.ApiColumnListRequest apiColumnListRequest = new ApiRequest.ApiColumnListRequest();
				apiColumnListRequest.setApiId(apiColumn.getApiId());
				apiColumnListRequest.setClmCd(apiColumn.getClmCd());
				apiColumnListRequest.setClmReqDiv(apiColumn.getClmReqDiv());

				List<ApiColumnListVO> ApiColumnList = apiRepository.selectApiColumnDetailList(apiColumnListRequest);
				apiColumn.setApiColumnList(ApiColumnList);
			}
		}
	}

	public List<ApiCtgrVO> selectCtgrApiList(ApiRequest apiRequest){
		return apiRepository.selectCtgrApiList(apiRequest);
	}

	public ApiRsponsePaging selectApiListNoPaging(ApiRequest apiRequest){
		List<ApiVO> list = apiRepository.selectApiListNoPaging(apiRequest);
		List<ApiCtgrVO> ctgrList = apiRepository.selectCtgrApiList(apiRequest);

		for(ApiVO apiInfo : list){
			apiRequest.setApiId(apiInfo.getApiId());

			List<ApiTagVO> ApiTagList = apiRepository.selectApiTagList(apiRequest);

			apiInfo.setApiTagList(ApiTagList);
		}

		ApiRsponsePaging pagingData = new ApiRsponsePaging();
		pagingData.setList(list);
		pagingData.setCtgrList(ctgrList);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	public ApiRsponsePaging selectApiListPaging(ApiRequest apiRequest){
		if(apiRequest.getPageIdx() == 0)
			apiRequest.setPageIdx(apiRequest.getPageIdx() + 1);

		if(apiRequest.getPageSize() == 0){
			apiRequest.setPageSize(20);
		}

		apiRequest.setPageOffset((apiRequest.getPageIdx()-1)*apiRequest.getPageSize());

		int totCnt = apiRepository.countApiList(apiRequest);
			List<ApiVO> list = apiRepository.selectApiList(apiRequest);
		List<ApiCtgrVO> ctgrList = apiRepository.selectCtgrApiList(apiRequest);

		for(ApiVO apiInfo : list){
			apiRequest.setApiId(apiInfo.getApiId());

			List<ApiTagVO> ApiTagList = apiRepository.selectApiTagList(apiRequest);

			apiInfo.setApiTagList(ApiTagList);
		}

		ApiRsponsePaging pagingData = new ApiRsponsePaging();
		pagingData.setPageIdx(apiRequest.getPageIdx());
		pagingData.setPageSize(apiRequest.getPageSize());
		pagingData.setList(list);
		pagingData.setCtgrList(ctgrList);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	// API 관리 페이지용 관계사 기준 CTGR 리스트 세팅
	public ApiCtgrRsponse selectApiListCtgrs(ApiDevGuideRequest apiDevGuideRequest) {
		List<ApiCtgrVO> ctgrList = apiRepository.selectApiListCtgrs(apiDevGuideRequest);

		ApiCtgrRsponse apiCtgrRsponse = new ApiCtgrRsponse();
		apiCtgrRsponse.setList(ctgrList);

		return apiCtgrRsponse;
	}

	/*
		URL 주소기준 중복체크
	 */
	public void ApiDupCheck(ApiRegistRequest apiRegistRequest){

		int apiDupCheck = apiRepository.apiDupCheck(apiRegistRequest);

		if(apiDupCheck > 0){
			log.error(messageSource.getMessage("E013"));
			throw new BusinessException("E013",messageSource.getMessage("E013"));
		}
	}

	public void insertApi(ApiRegistRequest apiRegistRequest){
		apiRepository.insertApi(apiRegistRequest);
		insertTag(apiRegistRequest);
		insertApiColumn(apiRegistRequest);

		RedisCommunicater.apiRedisSet(apiRegistRequest.getApiUrl(), "true");
	}

	public void updateApi(ApiRegistRequest apiRegistRequest){

		apiRepository.updateApi(apiRegistRequest);

		ApiRequest apiRequest = new ApiRequest();
		apiRequest.setApiId(apiRegistRequest.getApiId());
		apiRequest.setRegUserName(apiRegistRequest.getRegUserName());
		ApiVO apiInfo = apiRepository.selectApiDetalInfo(apiRequest);

		ApiStatModRequest apiStatModRequest = new ApiStatModRequest();
		apiStatModRequest.setApiId(apiRegistRequest.getApiId());
		apiStatModRequest.setApiModDiv("MOD");
		apiStatModRequest.setRegUserName(apiRegistRequest.getRegUserName());
		apiStatModRequest.setRegUserId(apiRegistRequest.getRegUserId());

		apiRepository.insertApiStatModHis(apiStatModRequest);

		//tag, column, columnList 삭제
		apiRepository.deleteApiTag(apiRegistRequest);
		apiRepository.deleteApiColumn(apiRegistRequest);
		apiRepository.deleteApiColumnList(apiRegistRequest);

		insertTag(apiRegistRequest);
		insertApiColumn(apiRegistRequest);
	}

	public void insertTag(ApiRegistRequest apiRegistRequest){
		for(ApiTagRequest apiTag : apiRegistRequest.getApiTagList()){
			apiTag.setApiId(apiRegistRequest.getApiId());
			apiTag.setRegUserName(apiRegistRequest.getRegUserName());
			apiTag.setRegUserId(apiRegistRequest.getRegUserId());
			apiRepository.insertApiTag(apiTag);
		}
	}

	public void insertApiColumn(ApiRegistRequest apiRegistRequest){
		for(ApiColumnRequest apiColumn : apiRegistRequest.getApiRequestList()){
			apiColumn.setApiId(apiRegistRequest.getApiId());
			apiColumn.setClmReqDiv("REQUEST");
			apiRepository.insertApiColumn(apiColumn);

			if(StringUtils.equals(apiColumn.getClmType(), "LIST")){
				for(ApiColumnListRequest apiColumnListInfo : apiColumn.getApiColumnList()){
					apiColumnListInfo.setClmListCd(apiColumn.getClmCd());
					apiColumnListInfo.setApiId(apiRegistRequest.getApiId());
					apiColumnListInfo.setClmReqDiv("REQUEST");
					apiRepository.insertApiColumnList(apiColumnListInfo);
				}
			}
		}

		for(ApiColumnRequest apiColumn : apiRegistRequest.getApiResponseList()){
			apiColumn.setApiId(apiRegistRequest.getApiId());
			apiColumn.setClmReqDiv("RESPONSE");
			apiColumn.setClmNcsrYn("N");
			apiRepository.insertApiColumn(apiColumn);

			if(StringUtils.equals(apiColumn.getClmType(), "LIST")){
				for(ApiColumnListRequest apiColumnListInfo : apiColumn.getApiColumnList()){
					apiColumnListInfo.setClmListCd(apiColumn.getClmCd());
					apiColumnListInfo.setApiId(apiRegistRequest.getApiId());
					apiColumnListInfo.setClmReqDiv("RESPONSE");
					apiColumn.setClmNcsrYn("N");
					apiRepository.insertApiColumnList(apiColumnListInfo);
				}
			}
		}
	}

	public void apiStatCdChange(ApiStatModRequest apiStatModRequest){

		if(apiStatModRequest.getDlyTermDt() != null){
			apiStatModRequest.setDlyTermDt(StringUtils.replace(apiStatModRequest.getDlyTermDt(), "-", ""));
			apiStatModRequest.setDlyTermTm(StringUtils.replace(apiStatModRequest.getDlyTermTm(), ":", "")+ "00");
		}

		ApiRequest apiRequest = new ApiRequest();
		apiRequest.setApiId(apiStatModRequest.getApiId());
		apiRequest.setRegUserId(apiStatModRequest.getRegUserId());
		apiRequest.setRegUserName(apiStatModRequest.getRegUserName());
		apiStatModRequest.setApiModDiv("STATSCHG");

		apiRepository.insertApiStatModHis(apiStatModRequest);

		ApiVO apiInfo = apiRepository.selectApiDetalInfo(apiRequest);

		if(StringUtils.equals(apiInfo.getApiStatCd(), "OK")) {
			//API 만료 날짜 업데이트
			apiRepository.updateApiDlyTerm(apiStatModRequest);

			if (StringUtils.equals(apiStatModRequest.getDlyTermDiv(), "IM")) {

				apiRequest.setApiStatCd("CLOSE");
				apiRepository.apiStatCdChange(apiRequest);

				RedisCommunicater.apiRedisSet(apiInfo.getApiUrl(), "false");
			}
		} else if (StringUtils.equals(apiInfo.getApiStatCd(), "CLOSE")){
			//API 만료 날짜 초기화
			apiStatModRequest.setDlyTermDiv(null);
			apiStatModRequest.setDlyTermDt(null);
			apiStatModRequest.setDlyTermTm(null);
			apiRepository.updateApiDlyTerm(apiStatModRequest);

			apiRequest.setApiStatCd("OK");
			apiRepository.apiStatCdChange(apiRequest);

			RedisCommunicater.apiRedisSet(apiInfo.getApiUrl(), "true");
		} else {
			log.error("잘못된 API STATCD 입니다 : " + apiInfo.getApiStatCd());
		}
	}

	public void apiDelete(ApiStatModRequest apiStatModRequest){

		// ModTime : Delete Time
		if(apiStatModRequest.getDlyTermDt() != null){
			apiStatModRequest.setDlyTermDt(StringUtils.replace(apiStatModRequest.getDlyTermDt(), "-", ""));
			apiStatModRequest.setDlyTermTm(StringUtils.replace(apiStatModRequest.getDlyTermTm(), ":", "")+ "00");
		}

		// 삭제 요청 객체 세팅
		ApiRequest apiRequest = new ApiRequest();
		apiRequest.setApiId(apiStatModRequest.getApiId());
		apiRequest.setRegUserName(apiStatModRequest.getRegUserName());
		apiRequest.setRegUserId(apiStatModRequest.getRegUserId());
		apiRequest.setApiStatCd(apiStatModRequest.getApiModDiv());

		// 삭제할 api 정보 get
		ApiVO apiInfo = apiRepository.selectApiDetalInfo(apiRequest);

//		// 삭제검증 1. [ 삭제요청자이름 = 등록자이름 이거나, 삭제요청자가 관리자 권한을 가지고 있을 때. ]
//		if(!StringUtils.equals(apiInfo, apiRequest.getRegUserName())) {
//			throw new BadRequestException("해당API 등록자와 요청자의 아이디가 같지 않고, 관리자도 아닙니다.");
//		}

		// 삭제검증 2. [ 해당 API가 앱에 등록되어있는지 검증 ]
		if(!StringUtils.equals(apiStatModRequest.getAppUseCnt(), "0")) {
			log.error(messageSource.getMessage("E051"));
			throw new BusinessException("E051", messageSource.getMessage("E051"));
		}

		// 삭제검증 3. [ 해당 API의 상태가 현재 중지중이어야 한다. ]
		if(!StringUtils.equals(apiInfo.getApiStatCd(), apiStatCdClose)) {
			log.error(messageSource.getMessage("E050"));
			throw new BusinessException("E050", messageSource.getMessage("E050"));
		}

		// 삭제로직
		apiRepository.deleteApi(apiRequest);

		// 로그남기기
		apiStatModRequest.setApiModDiv(apiStatCdDel);
		apiRepository.insertApiStatModHis(apiStatModRequest);

		RedisCommunicater.apiRedisDel(apiInfo.getApiUrl());
	}

	public ApiResponse selectApiAllList(ApiRequest.ApiAllListRequest apiAllListRequest){

		List<ApiVO> list = apiRepository.selectApiAllList(apiAllListRequest);

		ApiResponse data = new ApiResponse();
		data.setList(list);

		return data;
	}

	public ApiResponse searchCtgrApiAll(ApiRequest.CtgrApiAllListRequest apiAllListRequest){

		List<ApiVO> list = apiRepository.searchCtgrApiAll(apiAllListRequest);

		ApiResponse data = new ApiResponse();
		data.setList(list);

		return data;
	}

	public ApiResponse swaggerList(ApiRequest.SwaggerRequest swaggerRequest) throws BusinessException {

		ApiResponse apiResponse = new ApiResponse();
		List<ApiVO> apiList = new ArrayList<>();

		SwaggerResponse swaggerResponse = SwaggerCommunicater.swaggerInfo(swaggerRequest.getApiSvc());

		Map<String, Object> paths = swaggerResponse.getPaths();
		Set pathsSet = paths.keySet();

		Iterator pathsIterator = pathsSet.iterator();
		while(pathsIterator.hasNext()){
			String key = (String)pathsIterator.next();
			//API 중복 체크

			String[] urlList = StringUtils.split(key, "/");
			if(urlList.length >= 2){

				String apiVer = urlList[0];
				String apiUrl = "";

				ApiRegistRequest apiRegistRequest = new ApiRegistRequest();
				apiRegistRequest.setApiSvc(swaggerRequest.getApiSvc());
				apiRegistRequest.setApiVer(apiVer);

				for(int i=1; i < urlList.length; i++){
					if(i == 1){
						apiUrl = urlList[i];
					}else{
						apiUrl += "/" + urlList[i];
					}
				}

				apiRegistRequest.setApiUri(apiUrl);

				int apiDupCheck = apiRepository.apiDupCheck(apiRegistRequest);
				Map<String, Object> uri = (Map<String, Object>)paths.get(key);

				Set uriSet = uri.keySet();
				Iterator uriIterator = uriSet.iterator();

				while (uriIterator.hasNext()) {
					String uriKey = (String) uriIterator.next();
					Map<String, Object> method = (Map<String, Object>)uri.get(uriKey);
					if (apiDupCheck == 0) {
						ApiVO apiInfo = new ApiVO();
						apiInfo.setApiNm((String)method.get("summary"));
						apiInfo.setApiSvc(swaggerRequest.getApiSvc());
						apiInfo.setApiVer(apiVer);
						apiInfo.setApiUri(apiUrl);
						apiInfo.setApiMthd(StringUtils.upperCase(uriKey));
						apiList.add(apiInfo);
					}
				}
			}
		}
		apiResponse.setList(apiList);
		return apiResponse;
	}

	public ApiDetailRsponse  swaggerDetailInfo(ApiRequest.SwaggerInfoRequest swaggerInfoRequest){
		ApiDetailRsponse apiDetailRsponse = new ApiDetailRsponse();
		ApiVO ApiInfo = new ApiVO();

		SwaggerResponse swaggerResponse = SwaggerCommunicater.swaggerInfo(swaggerInfoRequest.getApiSvc());

		Map<String, Object> paths = swaggerResponse.getPaths();

		paths.keySet();

		String key = "/" + swaggerInfoRequest.getApiVer() + "/" + swaggerInfoRequest.getApiUri();

		Map<String, Object> uri = (Map<String, Object>)paths.get(key);
		String apiMthd = StringUtils.lowerCase(swaggerInfoRequest.getApiMthd());
		Map<String, Object> method = (Map<String, Object>) uri.get(apiMthd);
		List<Map<String, Object>> parameters = (List<Map<String, Object>>) method.get("parameters");
		Map<String, Object> definitions = swaggerResponse.getDefinitions();

		apiDetailRsponse.setApiNm((String)method.get("summary"));
		apiDetailRsponse.setApiSvc(swaggerInfoRequest.getApiSvc());
		apiDetailRsponse.setApiVer(swaggerInfoRequest.getApiVer());
		apiDetailRsponse.setApiUri(swaggerInfoRequest.getApiUri());
		apiDetailRsponse.setApiMthd(StringUtils.upperCase(apiMthd));

		List<ApiColumnVO> apiRequestList = settingParameter(parameters, definitions);
		List<ApiColumnVO> apiResponseList = settingResponse(method, definitions);

		apiDetailRsponse.setApiRequestList(apiRequestList);
		apiDetailRsponse.setApiResponseList(apiResponseList);

		return apiDetailRsponse;
	}

	public List<ApiColumnVO> settingParameter(List<Map<String, Object>> parameters, Map<String, Object> definitions){

		List<ApiColumnVO> apiRequestList = new ArrayList<>();

		for (Map<String, Object> parameter : parameters) {
			if (StringUtils.equals((String) parameter.get("in"), "body") || StringUtils.equals((String) parameter.get("in"), "query")) {
				if (parameter.containsKey("type")) {
					ApiColumnVO apiColumnInfo = new ApiColumnVO();
					apiColumnInfo.setClmCd((String) parameter.get("name"));
					apiColumnInfo.setClmReqDiv("REQUEST");

					apiColumnInfo.setClmType(colnumTypeCheck((String) parameter.get("type")));

					String clmNcsrYn = "N";

					if ((boolean) parameter.get("required") == true) {
						clmNcsrYn = "Y";
					}

					apiColumnInfo.setClmNcsrYn(clmNcsrYn);
					apiColumnInfo.setClmCtnt((String) parameter.get("description"));

					apiRequestList.add(apiColumnInfo);

				} else if (parameter.containsKey("schema")) {
					Map<String, Object> schema = (Map<String, Object>) parameter.get("schema");
					String ref = (String) schema.get("$ref");
					String definitionsKey = StringUtils.replace(ref, "#/definitions/", "");
					Map<String, Object> definition = (Map<String, Object>) definitions.get(definitionsKey);

					Map<String, Object> properties = (Map<String, Object>) definition.get("properties");
					Set propertiesSet = properties.keySet();

					Iterator propertiesIterator = propertiesSet.iterator();

					while (propertiesIterator.hasNext()) {
						String propertiesKey = (String) propertiesIterator.next();

						Map<String, Object> propertie = (Map<String, Object>) properties.get(propertiesKey);

						if (propertie.containsKey("type")) {
							ApiColumnVO apiColumnInfo = new ApiColumnVO();
							apiColumnInfo.setClmCd(propertiesKey);
							apiColumnInfo.setClmReqDiv("REQUEST");

							apiColumnInfo.setClmType(colnumTypeCheck((String) propertie.get("type")));

							if (StringUtils.equals((String) propertie.get("type"), "array")) {
								apiColumnInfo.setClmType("LIST");

								Map<String, Object> items = (Map<String, Object>) propertie.get("items");
								if (items.containsKey("type")) {
									List<ApiColumnListVO> apiColumnList = new ArrayList<>();
									ApiColumnListVO apiColumnListInfo = new ApiColumnListVO();

									apiColumnListInfo.setClmType(colnumTypeCheck((String) items.get("type")));

									apiColumnList.add(apiColumnListInfo);
									apiColumnInfo.setApiColumnList(apiColumnList);
								} else if (items.containsKey("$ref")) {
									String itemRef = (String) items.get("$ref");
									List<ApiColumnListVO> apiColumnList = settingDefinition(definitions, itemRef);
									apiColumnInfo.setApiColumnList(apiColumnList);
								}
							}

							if(definition.containsKey("required")){
								List<String> requiredList = (List<String>) definition.get("required");
								String RequiredCheck = colnumRequiredCheck(requiredList, propertiesKey);
								apiColumnInfo.setClmNcsrYn(RequiredCheck);
							}else{
								apiColumnInfo.setClmNcsrYn("N");
							}

							apiColumnInfo.setClmCtnt((String) propertie.get("description"));
							apiRequestList.add(apiColumnInfo);
						}else if(propertie.containsKey("$ref")){
							String propertieRefDetail = (String) propertie.get("$ref");
							String definitionsRefKey = StringUtils.replace(propertieRefDetail, "#/definitions/", "");
							Map<String, Object> definitionRef = (Map<String, Object>) definitions.get(definitionsRefKey);
							Map<String, Object> propertiesRef = (Map<String, Object>) definitionRef.get("properties");
							Set propertiesRefSet = propertiesRef.keySet();

							Iterator propertiesRefSetIterator = propertiesRefSet.iterator();

							while (propertiesRefSetIterator.hasNext()) {
								String propertiesRefKey = (String) propertiesRefSetIterator.next();
								Map<String, Object> propertieRef = (Map<String, Object>) propertiesRef.get(propertiesRefKey);
								ApiColumnVO apiColumnInfo = new ApiColumnVO();
								apiColumnInfo.setClmCd(propertiesRefKey);
								apiColumnInfo.setClmReqDiv("REQUEST");

								apiColumnInfo.setClmType(colnumTypeCheck((String) propertieRef.get("type")));

								if (StringUtils.equals((String) propertieRef.get("type"), "array")) {

									Map<String, Object> items = (Map<String, Object>) propertieRef.get("items");
									if (items.containsKey("type")) {
										List<ApiColumnListVO> apiColumnList = new ArrayList<>();
										ApiColumnListVO apiColumnListInfo = new ApiColumnListVO();

										apiColumnListInfo.setClmType(colnumTypeCheck((String) items.get("type")));

										apiColumnList.add(apiColumnListInfo);
										apiColumnInfo.setApiColumnList(apiColumnList);
									} else if (items.containsKey("$ref")) {
										String itemRef = (String) items.get("$ref");
										List<ApiColumnListVO> apiColumnList = settingDefinition(definitions, itemRef);

										apiColumnInfo.setApiColumnList(apiColumnList);
									}
								}

								if(definitionRef.containsKey("required")){
									List<String> requiredList = (List<String>) definitionRef.get("required");
									String RequiredCheck = colnumRequiredCheck(requiredList, propertiesRefKey);
									apiColumnInfo.setClmNcsrYn(RequiredCheck);
								}else{
									apiColumnInfo.setClmNcsrYn("N");
								}

								apiColumnInfo.setClmCtnt((String) propertieRef.get("description"));

								apiRequestList.add(apiColumnInfo);
							}
						}
					}
				}
			}
		}

		return apiRequestList;
	}

	public List<ApiColumnVO> settingResponse(Map<String, Object> method, Map<String, Object> definitions){
		List<ApiColumnVO> apiResponseList = new ArrayList<>();

		Map<String, Object> responses = (Map<String, Object>) method.get("responses");
		Map<String, Object> response = (Map<String, Object>) responses.get("200");

		Map<String, Object> schema = (Map<String, Object>) response.get("schema");

		if (schema.containsKey("type")) {
			ApiColumnVO apiColumnInfo = new ApiColumnVO();
			apiColumnInfo.setClmType(colnumTypeCheck((String) schema.get("type")));
			apiColumnInfo.setClmCtnt((String)response.get("description"));

			apiResponseList.add(apiColumnInfo);
		} else if (schema.containsKey("$ref")) {
			String reg = (String) schema.get("$ref");
			String definitionsKey = StringUtils.replace(reg, "#/definitions/", "");

			Map<String, Object> definition = (Map<String, Object>) definitions.get(definitionsKey);
			Map<String, Object> properties = (Map<String, Object>) definition.get("properties");
			Set propertiesSet = properties.keySet();

			Iterator propertiesIterator = propertiesSet.iterator();

			while (propertiesIterator.hasNext()) {
				String propertiesKey = (String) propertiesIterator.next();

				Map<String, Object> propertie = (Map<String, Object>) properties.get(propertiesKey);
				if (propertie.containsKey("type")) {
					ApiColumnVO apiColumnInfo = new ApiColumnVO();
					apiColumnInfo.setClmCd(propertiesKey);
					apiColumnInfo.setClmReqDiv("RESPONSE");

					apiColumnInfo.setClmType(colnumTypeCheck((String) propertie.get("type")));

					if (StringUtils.equals((String) propertie.get("type"), "array")) {
						Map<String, Object> items = (Map<String, Object>) propertie.get("items");
						if (items.containsKey("type")) {
							List<ApiColumnListVO> apiColumnList = new ArrayList<>();
							ApiColumnListVO apiColumnListInfo = new ApiColumnListVO();

							apiColumnListInfo.setClmType(colnumTypeCheck((String) items.get("type")));

							apiColumnList.add(apiColumnListInfo);
							apiColumnInfo.setApiColumnList(apiColumnList);
						} else if (items.containsKey("$ref")) {
							String itemRef = (String) items.get("$ref");
							List<ApiColumnListVO> apiColumnList = settingDefinition(definitions, itemRef);
							apiColumnInfo.setApiColumnList(apiColumnList);
						}
					}

					apiColumnInfo.setClmCtnt((String) propertie.get("description"));

					apiResponseList.add(apiColumnInfo);
				} else if (propertie.containsKey("$ref")) {
					String propertieRefDetail = (String) propertie.get("$ref");
					String definitionsRefKey = StringUtils.replace(propertieRefDetail, "#/definitions/", "");
					Map<String, Object> definitionRef = (Map<String, Object>) definitions.get(definitionsRefKey);

					Map<String, Object> propertiesRef = (Map<String, Object>) definitionRef.get("properties");
					Set propertiesRefSet = propertiesRef.keySet();

					Iterator propertiesRefSetIterator = propertiesRefSet.iterator();

					while (propertiesRefSetIterator.hasNext()) {
						String propertiesRefKey = (String) propertiesRefSetIterator.next();
						Map<String, Object> propertieRef = (Map<String, Object>) propertiesRef.get(propertiesRefKey);
						ApiColumnVO apiColumnInfo = new ApiColumnVO();
						apiColumnInfo.setClmCd(propertiesRefKey);
						apiColumnInfo.setClmReqDiv("REQUEST");
						apiColumnInfo.setClmType(colnumTypeCheck((String) propertieRef.get("type")));

						if (StringUtils.equals((String) propertieRef.get("type"), "array")) {

							Map<String, Object> items = (Map<String, Object>) propertieRef.get("items");
							if (items.containsKey("type")) {
								List<ApiColumnListVO> apiColumnList = new ArrayList<>();
								ApiColumnListVO apiColumnListInfo = new ApiColumnListVO();

								apiColumnListInfo.setClmType(colnumTypeCheck((String) items.get("type")));

								apiColumnList.add(apiColumnListInfo);
								apiColumnInfo.setApiColumnList(apiColumnList);
							} else if (items.containsKey("$ref")) {
								String itemRef = (String) items.get("$ref");
								List<ApiColumnListVO> apiColumnList = settingDefinition(definitions, itemRef);
								apiColumnInfo.setApiColumnList(apiColumnList);
							}
						}

						apiColumnInfo.setClmCtnt((String) propertieRef.get("description"));

						apiResponseList.add(apiColumnInfo);
					}
				}
			}
		}
		return apiResponseList;
	}

	public List<ApiColumnListVO> settingDefinition(Map<String, Object> definitions, String ref){
		List<ApiColumnListVO> apiColumnList = new ArrayList<>();

		String definitionsKey = StringUtils.replace(ref, "#/definitions/", "");
		Map<String, Object> definition = (Map<String, Object>) definitions.get(definitionsKey);

		Map<String, Object> properties = (Map<String, Object>) definition.get("properties");
		Set propertiesSet = properties.keySet();

		Iterator propertiesIterator = propertiesSet.iterator();

		while (propertiesIterator.hasNext()) {
			String propertiesKey = (String) propertiesIterator.next();

			Map<String, Object> propertie = (Map<String, Object>) properties.get(propertiesKey);
			ApiColumnListVO ApiColumnListInfo = new ApiColumnListVO();
			ApiColumnListInfo.setClmCd(propertiesKey);
			ApiColumnListInfo.setClmReqDiv("REQUEST");

			ApiColumnListInfo.setClmType(colnumTypeCheck((String) propertie.get("type")));

			if(definition.containsKey("required")){
				List<String> requiredList = (List<String>) definition.get("required");
				String RequiredCheck = colnumRequiredCheck(requiredList, definitionsKey);
				ApiColumnListInfo.setClmNcsrYn(RequiredCheck);
			}else{
				ApiColumnListInfo.setClmNcsrYn("N");
			}

			ApiColumnListInfo.setClmCtnt((String) propertie.get("description"));

			apiColumnList.add(ApiColumnListInfo);
		}

		return apiColumnList;
	}

	// API과금집계목록조회
	public List<RequestApiVO> getApiChargeList(ApiRequest request){
		List<RequestApiVO> apiList = apiRepository.getApiChargeList(request);

		if (apiList.size() > 0) {
			String totcnt = apiRepository.getApiChargeTotCnt(request);
			for (RequestApiVO api : apiList) {
				api.setTotCnt(totcnt);
			}
		}

		return apiList;
	}

	// API과금집계할인율 다건조회
	public List<RequestApiVO> getApiDetailList(ApiRequest request){
		List<RequestApiVO> apiList = apiRepository.getApiDetailList(request);

		int finalCost = 0;		// 누적요금
		int totalCost = 0;		// 최종요금
		int finalCnt = 0;

		for (RequestApiVO data : apiList) {

			int rate = Integer.parseInt(data.getDiscountRate());  // 할인율

			int minimumUseNumber = 0;

			if (finalCnt == 0) {
				minimumUseNumber = Integer.parseInt(data.getMinimumUseNumber());
			}

			minimumUseNumber = minimumUseNumber - Integer.parseInt(data.getUseCnt());


			if ("0".equals(data.getUseCnt())) {
				data.setTotalCost("0");
				data.setFinalCost("0");
			} else {

				double rateCal = 1.0 - (double)rate / 100;

				if (minimumUseNumber > 0) {
					double finalCalCost = 0;
					double minimumCarge = Double.parseDouble(data.getMiniMumCharges());

					finalCalCost = minimumCarge * rateCal;

					data.setTotalCost(String.valueOf(finalCalCost));
					data.setFinalCost(String.valueOf(finalCalCost));
				} else {
					finalCnt++;
					int overCost = (int)(((double)(-minimumUseNumber) * Double.parseDouble(data.getFeeAmount())) * rateCal);
					if (finalCnt == 1) {
						int minimumCharges = (int)(Double.parseDouble(data.getMiniMumCharges()) * rateCal); // 최소부과요금
						finalCost += minimumCharges;
						totalCost = minimumCharges;
						totalCost += overCost;
					} else {
						totalCost = (int)(((double)(-minimumUseNumber) * Double.parseDouble(data.getFeeAmount())) * rateCal);
					}

					finalCost += overCost;

					data.setTotalCost(String.valueOf(totalCost));
					data.setFinalCost(String.valueOf(finalCost));
				}
			}
		}

		return apiList;
	}

	// API과금집계엑셀다운로드
	public void excelDownload(ApiRequest request, HttpServletResponse response) throws Exception {
        excelUtil.excelDown(request, response);
	}

	public String colnumRequiredCheck(List<String> requiredList, String key){

		String requiredCheck = "N";

		for(String requiredInfo : requiredList){
			if(StringUtils.equals(requiredInfo, key)){
				requiredCheck = "Y";
				break;
			}
		}
		return requiredCheck;
	}

	public String colnumTypeCheck(String type){

		if (StringUtils.equals(type, "string")) {
			return "STRING";
		} else if (StringUtils.equals(type, "integer") || StringUtils.equals(type, "number")) {
			return "NUMBER";
		} else if (StringUtils.equals(type, "boolean")) {
			return "BOOLEAN";
		} else if (StringUtils.equals(type, "array")) {
			return "LIST";
		}else{
			return null;
		}
	}

	/*
	 *******************************API 정책*******************************
	 */

	public ApiPolicyVO selectApiPolicy(ApiPolicyRequest request){

		ApiPolicyVO data = apiRepository.selectApiPolicy(request);

		if (data.getRegDttm() != null && !StringUtils.equals(data.getRegDttm(), "")) {
			data.setGubun("upd");
		}
		return data;
	}

	public void insertApiPolicy(ApiPolicyRequest request) {
		apiRepository.insertApiPolicy(request);
		policyRedisSet(request);
	}

	public void updateApiPolicy(ApiPolicyRequest request) {
		apiRepository.updateApiPolicy(request);
		policyRedisSet(request);
	}

	public void policyRedisSet(ApiPolicyRequest request) {

		if (StringUtils.isBlank(request.getMaxUser())) {
			RedisCommunicater.apiFileCntRedisDel(request.getApiUrl());
		} else {
			RedisCommunicater.apiFileCntRedisSet(request.getApiUrl(), request.getMaxUser());
		}

		if (StringUtils.isBlank(request.getMaxSize())) {
			RedisCommunicater.apiFileSizeRedisDel(request.getApiUrl());
		} else {
			RedisCommunicater.apiFileSizeRedisSet(request.getApiUrl(), request.getMaxSize());
		}

		if (StringUtils.isBlank(request.getLimitedValue())) {
			RedisCommunicater.apiTimeLimitedRedisDel(request.getApiUrl());
		} else {
			RedisCommunicater.apiTimeLimitedRedisSet(request.getApiUrl(), request.getLimitedValue());
		}

		if (StringUtils.isBlank(request.getTxRestrValue())) {
			RedisCommunicater.apiTxRedisDel(request.getApiUrl());
		} else {
			RedisCommunicater.apiTxRedisSet(request.getApiUrl(), request.getTxRestrValue());
		}
	}
}