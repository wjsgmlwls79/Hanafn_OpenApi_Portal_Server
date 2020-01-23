package com.hanafn.openapi.portal.event;

import com.hanafn.openapi.portal.cmct.NoticeServerCommunicater;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.dto.CommonApiResponse;
import com.hanafn.openapi.portal.views.dto.HfnEnum;
import com.hanafn.openapi.portal.views.dto.UseorgRequest;
import com.hanafn.openapi.portal.views.repository.ApiRepository;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.vo.HfnInfoVO;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Getter
@Component
public class UseorgEventHandler implements EventHandlerInterface {

    private static final String RSP_CD_OK = "0000";

    private static final String[] allowedHfnCd = { HfnEnum.HCP.value(), HfnEnum.HBK.value() };
    private static final List<String> allowedHfnCdList = Arrays.asList(allowedHfnCd);

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    NoticeServerCommunicater noticeServerCommunicater;

    @Autowired
    MessageSourceAccessor messageSource;

    @Autowired
    CommonUtil commonUtil;

    private HashMap<String, Object> useorgNoticeData;

    public UseorgEventHandler() {
        super();
    }

    @Override
    public void eventCreate(String eventType, Object request, String hfnCd){
        if(StringUtils.isNotBlank(hfnCd) && !allowedHfnCdList.contains(hfnCd)) {
            log.debug("★처리되지않는 관계사입니다 => "+HfnEnum.resolve(hfnCd).getName());
            return;
        }

        useorgNoticeData = new HashMap<String,Object>();
        if(StringUtils.equals(eventType,"create")){
            this.registerHandle((UseorgRequest.UseorgDetailRequest) request, hfnCd);
        } else if(StringUtils.equals(eventType,"update")){
            this.changeHandle((UseorgRequest.UseorgDetailRequest) request, hfnCd);
        } else if(StringUtils.equals(eventType,"delete")){
            this.closeHandle((UseorgRequest.UseorgDetailRequest) request, hfnCd);
        } else {
            log.error("잘못된 USEORG 이벤트 형식입니다.");
            throw new BusinessException("E094",messageSource.getMessage("E094"));
        }
    }

    @Override
    public ResponseEntity<?> handleEvent(HashMap<String, Object> data, String url) {

        CommonApiResponse res = null;

        if(profile.equals("local")) {
            //if(profile.equals("local") || profile.equals("development")) {
            log.info("★ Useorg Notice API not send in " + profile + " property ");
            return ResponseEntity.ok(res);
        }

        res = noticeServerCommunicater.communicateServer(url, HttpMethod.POST, data, true);
        res.setStatCd((String) res.getResponseData().get("RSP_CD"));
        if(!StringUtils.equals(res.getStatCd(),RSP_CD_OK)){
            log.error("BusinessException["+res.getStatCd()+"] : " + res.getResponseData().get("RES_MSG"));
            throw new BusinessException("E094",messageSource.getMessage("E094"));
        }
        else {
            log.debug("★이용기관 승인 api 정상처리["+res.getStatCd()+"]");
            return ResponseEntity.ok(res);
        }
    }

    private void commonHandle(@NonNull UseorgRequest.UseorgDetailRequest request, String hfnCd, String eventType) {
        HashMap<String,Object> data = apiRepository.selectUseorgNoticeData(request);

        //dataHeader
        HashMap<String, Object> dataHeader = new HashMap<String, Object>();
        dataHeader.put("CNTY_CD", new String("kr"));
        dataHeader.put("ENTR_CD", data.get("ENTR_CD"));
        dataHeader.put("CLNT_IP_ADDR", new String("127.0.0.1"));


        //dataBody
        HashMap<String, Object> dataBody = new HashMap<String, Object>();
        dataBody.put("C_ENC_YN",'Y'); // 필수
        dataBody.put("CRUD", eventType);

//        dataBody.put("C_APP_ID",data.get("USER_KEY"));
        dataBody.put("USEORG_CD", data.get("ENTR_CD"));
        dataBody.put("USEORG_NM", data.get("USEORG_NM"));
        dataBody.put("BRN", data.get("BRN"));
        dataBody.put("USEORG_LGN_ID", data.get("USEORG_ID"));
        dataBody.put("USEORG_DIV_CD", "1");

        // 관리자 휴대폰번호
        String useorgUserTel = (String) data.get("USEORG_USER_TEL");

        try {
            dataBody.put("USEORG_USER_EMAIL", AES256Util.decrypt( (String) data.get("USEORG_USER_EMAIL")));
            useorgUserTel = (String) AES256Util.decrypt(useorgUserTel);
        } catch ( Exception e ) {
            log.error("useorgEventHandler 이메일 이용기관 복호화에러");
            throw new BusinessException("E094",messageSource.getMessage("E094"));
        }

        List<String> useorgUserTelList = commonUtil.parseTelNum(useorgUserTel);
        dataBody.put("USEORG_USER_TEL1", useorgUserTelList.get(0));
        dataBody.put("USEORG_USER_TEL2", useorgUserTelList.get(1));
        dataBody.put("USEORG_USER_TEL3", useorgUserTelList.get(2));

        String useorgTel = (String) data.get("USEORG_TEL");
        if(StringUtils.isNotBlank(useorgTel)) {
            if( useorgTel.length() == 9) {
                dataBody.put("USEORG_TEL1", StringUtils.substring(useorgTel,0,2));
                dataBody.put("USEORG_TEL2",StringUtils.substring(useorgTel,2,5));
                dataBody.put("USEORG_TEL3", StringUtils.substring(useorgTel,5,9));
            }
            else if(useorgTel.length() == 10) {
                if(!useorgTel.startsWith("02")) {   // 02로 시작하지 않을 시 334 규칙을 따름
                    dataBody.put("USEORG_TEL1", StringUtils.substring(useorgTel,0,3));
                    dataBody.put("USEORG_TEL2",StringUtils.substring(useorgTel,3,6));
                    dataBody.put("USEORG_TEL3", StringUtils.substring(useorgTel,6,10));
                } else {
                    dataBody.put("USEORG_TEL1", StringUtils.substring(useorgTel,0,2));
                    dataBody.put("USEORG_TEL2",StringUtils.substring(useorgTel,2,6));
                    dataBody.put("USEORG_TEL3", StringUtils.substring(useorgTel,6,10));
                }
            } else if ( useorgTel.length() == 11 ) {
                dataBody.put("USEORG_TEL1", StringUtils.substring(useorgTel,0,3));
                dataBody.put("USEORG_TEL2",StringUtils.substring(useorgTel,3,7));
                dataBody.put("USEORG_TEL3", StringUtils.substring(useorgTel,7,11));
            } else {
                log.error("이용기관 승인 전화번호 parsing => 처리할수 없는 길이 :" + useorgTel.length());
                throw new BusinessException("UE01", messageSource.getMessage("UE01"));
            }
        } else {
            dataBody.put("USEORG_TEL1", "");
            dataBody.put("USEORG_TEL2", "");
            dataBody.put("USEORG_TEL3", "");
        }

        // 세팅
        useorgNoticeData.put("dataHeader",dataHeader);
        useorgNoticeData.put("dataBody",dataBody);

        System.out.println("★data process:" + useorgNoticeData);
        this.handleEvent(useorgNoticeData, HfnEnum.resolve(hfnCd).getUseorgAplvUrl());
    }

    public void registerHandle(@NonNull UseorgRequest.UseorgDetailRequest request, String hfnCd) {
        this.commonHandle(request, hfnCd, "C");
    }

    public void changeHandle(@NonNull UseorgRequest.UseorgDetailRequest request, String hfnCd) {
        List<HfnInfoVO> list = settingRepository.selectHfnListByUseorgYn(request);

        for( HfnInfoVO hfnInfoVO : list ){
            log.debug("★이용기관 수정호출 대상 [:"+ HfnEnum.resolve(hfnInfoVO.getHfnCd()).getName());
            if(StringUtils.equals(hfnInfoVO.getHfnCd(), "01")){ // 현재 은행만 처리
                log.debug("★이용기관 수정호출 api To[:"+ HfnEnum.resolve(hfnInfoVO.getHfnCd()).getName());
                this.commonHandle(request, hfnInfoVO.getHfnCd(), "U");
            }
        }
    }

    public void closeHandle(@NonNull UseorgRequest.UseorgDetailRequest request, String hfnCd) {
        log.debug("★ delete event handling 처리.");
        this.commonHandle(request, hfnCd, "D");
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}