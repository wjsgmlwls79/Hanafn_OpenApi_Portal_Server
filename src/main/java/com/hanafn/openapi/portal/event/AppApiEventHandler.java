package com.hanafn.openapi.portal.event;

import com.hanafn.openapi.portal.cmct.NoticeServerCommunicater;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.ApiRepository;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.vo.ApiVO;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*
    포털에서 관계사가 이용기관이 신청한 앱정보 승인할 경우 관계사로 전달(등록,변경,삭제)
 */
@Component
@Slf4j
@Data
public class AppApiEventHandler implements EventHandlerInterface {

    @Value("${spring.profiles.active}")
    private String profile;

    private static final String RSP_CD_OK = "0000";
    private static final String RSP_CD_API_DUP = "U910";
    private static final String[] allowedHfnCd = { HfnEnum.HCP.value(), HfnEnum.HBK.value() };
    private static final List<String> allowedHfnCdList = Arrays.asList(allowedHfnCd);

    private String useorgNoticeApiUrl;

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    NoticeServerCommunicater noticeServerCommunicater;

    @Autowired
    MessageSourceAccessor messageSource;

    private HashMap<String, Object> appApiNoticeData;

    @Override
    public void eventCreate(String eventType, Object request, String hfnCd){

        if(StringUtils.isNotBlank(hfnCd) && !allowedHfnCdList.contains(hfnCd)) {
            log.error("처리되지않는 관계사입니다 : "+HfnEnum.resolve(hfnCd).getName());
            return;
        }

        appApiNoticeData = new HashMap<String,Object>();
        if(StringUtils.equals(eventType,"create")){
            this.registerHandle((AppsRsponse) request, hfnCd);
        } else if(StringUtils.equals(eventType,"update")){
            this.changeHandle((AppApiChangeDTO) request, hfnCd);
        } else if(StringUtils.equals(eventType,"delete")){
            this.withdrawalHandle((AppsRsponse) request, hfnCd);
        } else {
            log.error("AppApiEventHandler 처리할수 없는 eventType :"+eventType);
            throw new BusinessException("E073",messageSource.getMessage("E073"));
        }
    }

    private void commonHandle(@NonNull AppsRsponse request, String hfnCd, String eventType){
        AppsRequest appsRequest = new AppsRequest();
        appsRequest.setAppKey(request.getAppKey());

        List<ApiVO> apiList = request.getApiList();
        AppsRequest.AppApiRequest appApiRequest = new AppsRequest.AppApiRequest();

        UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
        useorgDetailRequest.setUserKey(request.getUserKey());
        int count=0;
        // 건별로 보내는 API 적용
        for( ApiVO api : apiList) {
            appApiNoticeData.clear();
            appApiRequest.setAppKey(request.getAppKey());
            appApiRequest.setApiId(api.getApiId());
            HashMap<String, Object> originalMap = apiRepository.selectAppApiAplvNoticeWait(appApiRequest);
            log.debug("★originalMap:["+originalMap.toString()+"]");

            //dataHeader
            HashMap<String, Object> dataHeader = new HashMap<String, Object>();
            dataHeader.put("CNTY_CD", new String("kr"));
            dataHeader.put("ENTR_CD", originalMap.get("ENTR_CD"));
            dataHeader.put("CLNT_IP_ADDR", new String("127.0.0.1"));
            dataHeader.put("APP_KEY", originalMap.get("APP_KEY"));

            //dataBody
            HashMap<String, Object> dataBody = new HashMap<String, Object>();
            dataBody.put("C_ENC_YN",'Y'); // 필수
            dataBody.put("C_APP_ID", originalMap.get("APP_KEY"));
            dataBody.put("USEORG_CD", originalMap.get("ENTR_CD"));
            dataBody.put("API_APP_ID", originalMap.get("APP_KEY"));
            dataBody.put("API_APP_NM", (String) originalMap.get("APP_NM"));
            dataBody.put("REG_TELE_DIV_CD", StringUtils.substring((String)originalMap.get("HFN_SVC_CD"),0,4));
            dataBody.put("REG_ENTR_DIV_CD", StringUtils.substring((String)originalMap.get("HFN_SVC_CD"),4,7));
            dataBody.put("API_VER", originalMap.get("API_VER"));
            String apiUri = (String) originalMap.get("API_SVC") + "/" + (String) originalMap.get("API_VER") + "/" + (String)originalMap.get("API_URI");
            dataBody.put("API_URI", apiUri);
            dataBody.put("API_NM", originalMap.get("API_NM"));
            dataBody.put("FEE_ACCOUNT", originalMap.get("ACC_NO"));
            try {
                if (originalMap.get("ACC_NO").toString() != null) {
                    dataBody.replace("FEE_ACCOUNT", AES256Util.decrypt(originalMap.get("ACC_NO").toString()));
                }
            } catch ( Exception e ) {
                log.error("앱 API 승인( REGISTER ) 계좌번호 복호화 오류");
                throw new BusinessException("E078",messageSource.getMessage("E078"));
            }
            dataBody.put("CRUD",eventType);

            // 캐피탈 요건 반영
            if(StringUtils.equals(hfnCd, HfnEnum.HCP.value())) {
                dataBody.put("REG_SVC_ID", (String)originalMap.get("HFN_SVC_CD"));
            }

            // 세팅
            appApiNoticeData.put("dataHeader",dataHeader);
            appApiNoticeData.put("dataBody",dataBody);

            log.debug("★관계사 전송:" + HfnEnum.resolve(hfnCd).getName());
            log.debug("★data:"+appApiNoticeData);
            this.handleEvent(appApiNoticeData, HfnEnum.resolve(hfnCd).getAppApiaAplvUrl());
        }
        log.debug("★Notice 종료, 총 [ " + ++count + " ] 건 전송");
    }

    public void registerHandle(@NonNull AppsRsponse request, String hfnCd) {
        String eventType="C";

        log.debug("★ AppApi event Common handling 처리.");
        this.commonHandle(request, hfnCd, eventType);
    }

    public void changeHandle(@NonNull AppApiChangeDTO request, String hfnCd) {

        log.debug("★request:"+request);
        String eventType = "U";

        List<ApiVO> oldApiList = request.getOldApiList();
        List<ApiVO> newApiList = request.getNewApiList();
        List<ApiVO> delList = new ArrayList<ApiVO>();
        List<ApiVO> newList = new ArrayList<ApiVO>();

        AppsRsponse rsponse = request.getNewAppsRsponse();
        AppsRequest.AppApiRequest appApiRequest = new AppsRequest.AppApiRequest();

        log.debug("★ AppApi event Common handling 처리 [");

        log.debug("☆oldApiList:"+oldApiList);
        log.debug("☆newApiList:"+newApiList);

        boolean dupCheck = false;
        log.debug("size : " + oldApiList.size() + " hh : " + newApiList.size());
        for ( int i=0; i< oldApiList.size(); i++) {
            if(oldApiList.get(i) != null){
                for ( int j=0; j<newApiList.size(); j++ ) {
                    if(newApiList.get(j) != null){
                        if (StringUtils.equals(oldApiList.get(i).getApiId(), newApiList.get(j).getApiId())) {
                            log.debug("i:"+i + ", j:" + j + " 같다[" + oldApiList.get(i).getApiNm() + "], [ " + newApiList.get(j).getApiNm() + " ]");
                            dupCheck = true;
                        }
                    }
                }
            }

            if(!dupCheck){
                delList.add(oldApiList.get(i));
                log.debug("★del:"+oldApiList.get(i).getApiNm());
            }
            dupCheck = false;
        }

        for ( int i=0; i< newApiList.size(); i++ ) {
            if(newApiList.get(i) != null){
                for (  int j=0; j<oldApiList.size(); j++  ) {
                    if(oldApiList.get(j) != null) {
                        if (StringUtils.equals(newApiList.get(i).getApiId(), oldApiList.get(j).getApiId())) {
                            log.debug("NEW ] i:"+i + ", j:" + j + " 같다[" + newApiList.get(i).getApiNm() + "], [ " + oldApiList.get(j).getApiNm() + " ]");
                            dupCheck = true;
                        }
                    }
                }
            }
            if(!dupCheck){
                newList.add(newApiList.get(i));
                log.debug("★new:"+newApiList.get(i).getApiNm());
            }
            dupCheck = false;
        }

        // 건별로 보내는 API 적용
        for( ApiVO api : delList) {
            eventType = "D";
            appApiNoticeData.clear();
            appApiRequest.setAppKey(rsponse.getAppKey());
            appApiRequest.setApiId(api.getApiId());
            HashMap<String, Object> originalMap = apiRepository.selectAppApiAplvNoticeWait(appApiRequest);
            log.debug("★originalMap:["+originalMap.toString()+"]");

            //dataHeader
            HashMap<String, Object> dataHeader = new HashMap<String, Object>();
            dataHeader.put("CNTY_CD", new String("kr"));
            dataHeader.put("ENTR_CD", originalMap.get("ENTR_CD"));
            dataHeader.put("CLNT_IP_ADDR", new String("127.0.0.1"));
            dataHeader.put("APP_KEY", originalMap.get("APP_KEY"));

            //dataBody
            HashMap<String, Object> dataBody = new HashMap<String, Object>();
            dataBody.put("C_ENC_YN",'Y'); // 필수
            dataBody.put("C_APP_ID", originalMap.get("APP_KEY"));
            dataBody.put("USEORG_CD", originalMap.get("ENTR_CD"));
            dataBody.put("API_APP_ID", originalMap.get("APP_KEY"));
            dataBody.put("API_APP_NM", (String) originalMap.get("APP_NM"));
            dataBody.put("REG_TELE_DIV_CD", StringUtils.substring((String)originalMap.get("HFN_SVC_CD"),0,4));
            dataBody.put("REG_ENTR_DIV_CD", StringUtils.substring((String)originalMap.get("HFN_SVC_CD"),4,7));
            dataBody.put("API_VER", originalMap.get("API_VER"));
            String apiUri = (String) originalMap.get("API_SVC") + "/" + (String) originalMap.get("API_VER") + "/" + (String)originalMap.get("API_URI");
            dataBody.put("API_URI", apiUri);
            dataBody.put("API_NM", originalMap.get("API_NM"));
            dataBody.put("FEE_ACCOUNT", originalMap.get("ACC_NO"));
            try {
                if (originalMap.get("ACC_NO").toString() != null) {
                    dataBody.replace("FEE_ACCOUNT", AES256Util.decrypt(originalMap.get("ACC_NO").toString()));
                }
            } catch ( Exception e ) {
                log.error("앱 API 승인( CHANGE1 ) 계좌번호 복호화 오류");
                throw new BusinessException("E078",messageSource.getMessage("E078"));
            }
            dataBody.put("CRUD",eventType);

            // 세팅
            appApiNoticeData.put("dataHeader",dataHeader);
            appApiNoticeData.put("dataBody",dataBody);

            log.debug("★관계사 전송:" + HfnEnum.resolve(hfnCd).getName());
            log.debug("★data:"+appApiNoticeData);

            // 캐피탈 요건 반영
            if(StringUtils.equals(hfnCd, HfnEnum.HCP.value())) {
                dataBody.put("REG_SVC_ID", (String)originalMap.get("HFN_SVC_CD"));
            }

            this.handleEvent(appApiNoticeData, HfnEnum.resolve(hfnCd).getAppApiaAplvUrl());
        }

        // 건별로 보내는 API 적용
        for( ApiVO api : newList) {

            eventType = "C";
            appApiNoticeData.clear();
            appApiRequest.setAppKey(rsponse.getAppKey());
            appApiRequest.setApiId(api.getApiId());
            HashMap<String, Object> originalMap = apiRepository.selectAppApiAplvNoticeOriginal(appApiRequest);

            //dataHeader
            HashMap<String, Object> dataHeader = new HashMap<String, Object>();
            dataHeader.put("CNTY_CD", new String("kr"));
            dataHeader.put("ENTR_CD", originalMap.get("ENTR_CD"));
            dataHeader.put("CLNT_IP_ADDR", new String("127.0.0.1"));
            dataHeader.put("APP_KEY", originalMap.get("APP_KEY"));

            //dataBody
            HashMap<String, Object> dataBody = new HashMap<String, Object>();
            dataBody.put("C_ENC_YN",'Y'); // 필수
            dataBody.put("C_APP_ID", originalMap.get("APP_KEY"));
            dataBody.put("USEORG_CD", originalMap.get("ENTR_CD"));
            dataBody.put("API_APP_ID", originalMap.get("APP_KEY"));
            dataBody.put("API_APP_NM", (String) originalMap.get("APP_NM"));
            dataBody.put("REG_TELE_DIV_CD", StringUtils.substring((String)originalMap.get("HFN_SVC_CD"),0,4));
            dataBody.put("REG_ENTR_DIV_CD", StringUtils.substring((String)originalMap.get("HFN_SVC_CD"),4,7));
            dataBody.put("API_VER", originalMap.get("API_VER"));
            String apiUri = (String) originalMap.get("API_SVC") + "/" + (String) originalMap.get("API_VER") + "/" + (String)originalMap.get("API_URI");
            dataBody.put("API_URI", apiUri);
            dataBody.put("API_NM", originalMap.get("API_NM"));
            dataBody.put("FEE_ACCOUNT", originalMap.get("ACC_NO"));
            try {
                if (originalMap.get("ACC_NO").toString() != null) {
                    dataBody.replace("FEE_ACCOUNT", AES256Util.decrypt(originalMap.get("ACC_NO").toString()));
                }
            } catch ( Exception e ) {
                log.error("앱 API 승인( CHANGE2 ) 계좌번호 복호화 오류");
                throw new BusinessException("E078",messageSource.getMessage("E078"));
            }

            dataBody.put("CRUD",eventType);

            // 세팅
            appApiNoticeData.put("dataHeader",dataHeader);
            appApiNoticeData.put("dataBody",dataBody);

            log.debug("★관계사 전송:" + HfnEnum.resolve(hfnCd).getName());
            log.debug("★data:"+appApiNoticeData);

            // 캐피탈 요건 반영
            if(StringUtils.equals(hfnCd, HfnEnum.HCP.value())) {
                dataBody.put("REG_SVC_ID", (String)originalMap.get("HFN_SVC_CD"));
            }

            this.handleEvent(appApiNoticeData, HfnEnum.resolve(hfnCd).getAppApiaAplvUrl());
        }
    }

    public void withdrawalHandle(@NonNull AppsRsponse request, String hfnCd) {
        String eventType = "D";

        log.debug("★ AppApi event Common handling 처리.");

        AppsRequest appsRequest = new AppsRequest();
        appsRequest.setAppKey(request.getAppKey());

        // 등록시에는 appResponse(request) 안에 있음
        List<ApiVO> apiList = request.getApiList();
        AppsRequest.AppApiRequest appApiRequest = new AppsRequest.AppApiRequest();

        UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
        useorgDetailRequest.setUserKey(request.getUserKey());
        int count=0;
        log.debug("delete api list : " + apiList );
        // 건별로 보내는 API 적용
        for( ApiVO api : apiList) {
            if(!StringUtils.equals(api.getHfnCd(), hfnCd)){
                continue;
            }

            appApiNoticeData.clear();
            appApiRequest.setAppKey(request.getAppKey());
            appApiRequest.setApiId(api.getApiId());
            HashMap<String, Object> originalMap = apiRepository.selectAppApiAplvNoticeWait(appApiRequest);

            //dataHeader
            HashMap<String, Object> dataHeader = new HashMap<String, Object>();
            dataHeader.put("CNTY_CD", new String("kr"));
            dataHeader.put("ENTR_CD", originalMap.get("ENTR_CD"));
            dataHeader.put("CLNT_IP_ADDR", new String("127.0.0.1"));
            dataHeader.put("APP_KEY", originalMap.get("APP_KEY"));

            //dataBody
            HashMap<String, Object> dataBody = new HashMap<String, Object>();
            dataBody.put("C_ENC_YN",'Y'); // 필수
            dataBody.put("C_APP_ID", originalMap.get("APP_KEY"));
            dataBody.put("USEORG_CD", originalMap.get("ENTR_CD"));
            dataBody.put("API_APP_ID", originalMap.get("APP_KEY"));
            dataBody.put("API_APP_NM", (String) originalMap.get("APP_NM"));
            dataBody.put("REG_TELE_DIV_CD", StringUtils.substring((String)originalMap.get("HFN  _SVC_CD"),0,4));
            dataBody.put("REG_ENTR_DIV_CD", StringUtils.substring((String)originalMap.get("HFN_SVC_CD"),4,7));
            dataBody.put("API_VER", originalMap.get("API_VER"));
            String apiUri = (String) originalMap.get("API_SVC") + "/" + (String) originalMap.get("API_VER") + "/" + (String)originalMap.get("API_URI");
            dataBody.put("API_URI", apiUri);
            dataBody.put("API_NM", originalMap.get("API_NM"));
            dataBody.put("FEE_ACCOUNT", originalMap.get("ACC_NO"));
            try {
                if (originalMap.get("ACC_NO").toString() != null) {
                    dataBody.replace("FEE_ACCOUNT", AES256Util.decrypt(originalMap.get("ACC_NO").toString()));
                }
            } catch ( Exception e ) {
                log.error("앱 API 승인( DELETE ) 계좌번호 복호화 오류");
                throw new BusinessException("E078",messageSource.getMessage("E078"));
            }
            dataBody.put("CRUD",eventType);

            // 세팅
            appApiNoticeData.put("dataHeader",dataHeader);
            appApiNoticeData.put("dataBody",dataBody);

            log.debug("★관계사 전송:" + HfnEnum.resolve(hfnCd).getName());
            log.debug("★data: " + appApiNoticeData);

            // 캐피탈 요건 반영
            if(StringUtils.equals(hfnCd, HfnEnum.HCP.value())) {
                dataBody.put("REG_SVC_ID", (String)originalMap.get("HFN_SVC_CD"));
            }

            this.handleEvent(appApiNoticeData, HfnEnum.resolve(hfnCd).getAppApiaAplvUrl());
        }
        log.debug("★Notice 종료, 총 [ " + ++count + " ] 건 전송");
    }

    @Override
    public ResponseEntity<?> handleEvent(HashMap<String, Object> data, String url) {

        CommonApiResponse res = null;

        //if(profile.equals("local") || profile.equals("development")) {
        if(profile.equals("local")) {
            log.info("★ AppApi Notice API not send in " + profile + " property ");
            return ResponseEntity.ok(res);
        }

        res = noticeServerCommunicater.communicateServer(url, HttpMethod.POST, data, true);
        res.setStatCd((String) res.getResponseData().get("RSP_CD"));
        log.debug("★statCd:"+res.getStatCd());
        if(!StringUtils.equals(res.getStatCd(),RSP_CD_OK) && !StringUtils.equals(res.getStatCd(),RSP_CD_API_DUP)) {
            log.error("앱 API 승인 에러 BusinessException["+res.getStatCd()+"] : " + res.getResponseData().get("RES_MSG"));
            if(res.getResponseData().get("RES_MSG").toString() == null || res.getResponseData().get("RES_MSG").toString().isEmpty()) {
                log.error("앱 API 승인 에러 -> RES_MSG 없음");
                throw new BusinessException("E078",messageSource.getMessage("E078"));
            } else {
                log.error("앱 API 승인 에러 -> RES_MSG : " + res.getResponseData().get("RES_MSG").toString());
                throw new BusinessException("E078",res.getResponseData().get("RES_MSG").toString());
            }

        }
        else {
            log.debug("★정상처리["+res.getStatCd()+"]");
            return ResponseEntity.ok(res);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}