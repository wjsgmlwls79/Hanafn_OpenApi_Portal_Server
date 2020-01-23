package com.hanafn.openapi.portal.views.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.ApiRepository;
import com.hanafn.openapi.portal.views.repository.GuideRepository;
import com.hanafn.openapi.portal.views.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class GuideService {

    private final ObjectMapper objectMapper;
    private final GuideRepository guideRepository;
    private final ApiRepository apiRepository;
    private final MessageSourceAccessor messageSource;

    /*
     * ******************************개발 가이드******************************
     * */
    public List<ApiDevGuideVO> selectApiDevGuideList(ApiDevGuideRequest apiDevGuideRequest){

        List<ApiDevGuideVO> ApiDevGuideList = guideRepository.selectApiDevGuideList(apiDevGuideRequest);

        for(ApiDevGuideVO ApiDevGuideInfo : ApiDevGuideList){
            ApiRequest apiRequest = new ApiRequest();
            apiRequest.setApiId(ApiDevGuideInfo.getApiId());

            List<ApiTagVO> ApiTagList = apiRepository.selectApiTagList(apiRequest);

            ApiDevGuideInfo.setApiTagList(ApiTagList);
        }

        return ApiDevGuideList;
    }
    public AppsAllRsponse selectAppsAll(AppsAllRequest appsAllRequest){

        List<AppsVO> appsList = guideRepository.selectAppsAll(appsAllRequest);

        AppsAllRsponse appsAllRsponse = new AppsAllRsponse();
        appsAllRsponse.setList(appsList);

        return appsAllRsponse;
    }

    public List<ApiDevGuideVO> selectApiDevGuideAllList(ApiDevGuideRequest apiDevGuideRequest){

        List<ApiDevGuideVO> ApiDevGuideList = guideRepository.selectApiDevGuideAllList(apiDevGuideRequest);

        for(ApiDevGuideVO ApiDevGuideInfo : ApiDevGuideList){
            ApiRequest apiRequest = new ApiRequest();
            apiRequest.setApiId(ApiDevGuideInfo.getApiId());

            List<ApiTagVO> ApiTagList = apiRepository.selectApiTagList(apiRequest);

            ApiDevGuideInfo.setApiTagList(ApiTagList);
        }

        return ApiDevGuideList;
    }

    public UseorgRsponse selectDevGuidesUseorgAll(ApiDevGuideRequest.ApiDevGuideUseorgAllRequest apiDevGuideUseorgAllRequest){

        List<UseorgVO> useorgList = guideRepository.selectDevGuidesUseorgAll(apiDevGuideUseorgAllRequest);

        UseorgRsponse useorgRsponse = new UseorgRsponse();
        useorgRsponse.setList(useorgList);

        return useorgRsponse;
    }

    public ApiCtgrRsponse selectDevGuidesApiCtgrForUseorgList(ApiDevGuideRequest apiDevGuideRequest){

        List<ApiCtgrVO> ctgrList = guideRepository.selectDevGuidesApiCtgrForUseorgList(apiDevGuideRequest);

        ApiCtgrRsponse apiCtgrRsponse = new ApiCtgrRsponse();
        apiCtgrRsponse.setList(ctgrList);

        return apiCtgrRsponse;
    }

    public ApiCtgrRsponse selectDevGuidesApiCtgrAll(ApiDevGuideRequest apiDevGuideRequest){

        List<ApiCtgrVO> ctgrList = guideRepository.selectDevGuidesApiCtgrAll(apiDevGuideRequest);

        ApiCtgrRsponse apiCtgrRsponse = new ApiCtgrRsponse();
        apiCtgrRsponse.setList(ctgrList);

        return apiCtgrRsponse;
    }

    public AppsAllRsponse.devGuidesAppsAllRsponse selectDevGuidesAppsAll(ApiDevGuideRequest.ApiDevGuideApiAllRequest apiDevGuideApiAllRequest){

        List<AppsVO> appsList = guideRepository.selectDevGuidesAppsAll(apiDevGuideApiAllRequest);

        AppsAllRsponse.devGuidesAppsAllRsponse devGuidesAppsAllRsponse = new AppsAllRsponse.devGuidesAppsAllRsponse();
        devGuidesAppsAllRsponse.setList(appsList);

        return devGuidesAppsAllRsponse;
    }

    // TODO: 필터링 값 추가 입력 필요
    public String ctgrCdReplace(String ctgrCd) {
        //String buf = ctgrCd;
        String buf;

        buf = ctgrCd.replaceAll("'", "");
        buf = buf.replaceAll("--", "");
        buf = buf.replaceAll("=", "");
        buf = buf.replaceAll(">", "");
        buf = buf.replaceAll("<", "");
        buf = buf.replaceAll("/*", "");
        //buf = buf.replaceAll("*/", "");
        //buf = buf.replaceAll("\\", "");
        //buf = buf.replaceAll("+", "");
        buf = buf.replaceAll("user_", "");
        buf = buf.replaceAll("table", "");
        buf = buf.replaceAll("tables", "");
        buf = buf.replaceAll("name", "");
        buf = buf.replaceAll("column", "");
        buf = buf.replaceAll("sysolums", "");
        buf = buf.replaceAll("union", "");
        buf = buf.replaceAll("select", "");
        buf = buf.replaceAll("insert", "");
        buf = buf.replaceAll("drop", "");
        buf = buf.replaceAll("update", "");
        buf = buf.replaceAll("and", "");
        buf = buf.replaceAll("or", "");
        buf = buf.replaceAll("join", "");
        buf = buf.replaceAll("substring", "");
        buf = buf.replaceAll("from", "");
        buf = buf.replaceAll("where", "");
        buf = buf.replaceAll("declare", "");
        buf = buf.replaceAll("substr", "");
        buf = buf.replaceAll("openrowset", "");
        buf = buf.replaceAll("xp_", "");
        buf = buf.replaceAll("sysobjects", "");

        return buf;
    }

    public String settingSearchApiCtgrs(List<ApiCtgrVO> searchApiCtgrList){
        //검색 API 카테고리 설정 EX) ('ctgr1','ctgr2')
        String qApiCtgrs = "";
        if(searchApiCtgrList != null && searchApiCtgrList.size() > 0){
            if(searchApiCtgrList.size() == 1){
                String ctgrCd = ctgrCdReplace(searchApiCtgrList.get(0).getCtgrCd());
                qApiCtgrs = "('" +ctgrCd + "')";
            }
            else{
                for(int i=0; i<searchApiCtgrList.size(); i++){
                    String ctgrCd = ctgrCdReplace(searchApiCtgrList.get(i).getCtgrCd());
                    if(i == 0){
                        qApiCtgrs += "('" + ctgrCd + "'";
                    }
                    else if((i+1) == searchApiCtgrList.size()){
                        qApiCtgrs += ",'" + ctgrCd + "')";
                    }
                    else{
                        qApiCtgrs += ",'" + ctgrCd + "'";
                    }
                }
            }
        }else{
            qApiCtgrs = "('')";
        }

        return qApiCtgrs;
    }

    public String selectEnckeyByEntrCd(String entrCd){
        return guideRepository.selectEnckeyByEntrCd(entrCd);
    }
}
