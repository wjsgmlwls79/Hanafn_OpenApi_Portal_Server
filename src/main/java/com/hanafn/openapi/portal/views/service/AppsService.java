package com.hanafn.openapi.portal.views.service;

import com.hanafn.openapi.portal.cmct.AccountStatusCommunicater;
import com.hanafn.openapi.portal.cmct.GWCommunicater;
import com.hanafn.openapi.portal.cmct.HubCommunicator;
import com.hanafn.openapi.portal.cmct.RedisCommunicater;
import com.hanafn.openapi.portal.cmct.dto.GWResponse;
import com.hanafn.openapi.portal.event.AppApiEventHandler;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.util.DateUtil;
import com.hanafn.openapi.portal.util.RSAUtil;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.AppsRepository;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AppsService {

    @Autowired
    AppsRepository appsRepository;
    @Autowired
    SettingRepository settingRepository;
    @Autowired
    SettingService settingService;
    @Autowired
    AppApiEventHandler appApiEventHandler;
    @Autowired
    AccountStatusCommunicater accountStatusCommunicater;
    @Autowired
    HubCommunicator hubCommunicator;
    @Autowired
    MessageSourceAccessor messageSource;

    private static final Logger logger = LoggerFactory.getLogger(AppsService.class);

    public List<AppsVO> selectAppListAll(AppsRequest appsRequest) {
        return appsRepository.selectAppListAll(appsRequest);
    }

    public List<AppsVO> selectUserPortalAppList(AppsRequest appsRequest) {
        List<AppsVO> appList = appsRepository.selectAppAll(appsRequest);
        List<AppsVO> appModList = appsRepository.selectAppModAll(appsRequest);

        boolean dupCheck = false;
        for(AppsVO appVO : appList){
            for(AppsVO appModVO : appModList){
                if(StringUtils.equals(appVO.getAppKey(),appModVO.getAppKey())) {
                    dupCheck = true;
                    break;
                }
                dupCheck = false;
            }
            if(dupCheck != true) {
                appModList.add(appVO);
            }
        }


        return appModList;
    }

    public AppsRsponsePaging selectAppsListPaging(AppsRequest appsRequest){
        if(appsRequest.getPageIdx() == 0)
            appsRequest.setPageIdx(appsRequest.getPageIdx() + 1);

        if(appsRequest.getPageSize() == 0){
            appsRequest.setPageSize(20);
        }

        appsRequest.setPageOffset((appsRequest.getPageIdx()-1)*appsRequest.getPageSize());
        int totCnt = appsRepository.countAppsList(appsRequest);
        List<AppsVO> list = appsRepository.selectAppsList(appsRequest);

        AppsRsponsePaging pagingData = new AppsRsponsePaging();
        pagingData.setPageIdx(appsRequest.getPageIdx());
        pagingData.setPageSize(appsRequest.getPageSize());
        pagingData.setTotCnt(totCnt);
        pagingData.setList(list);
        pagingData.setSelCnt(list.size());

        return pagingData;
    }

    /**** RSA 클라이언트 id, secret 다운로드 및 재발급 ****/
    public AppCidScrDlResponse keyDownload(AppsRequest.AppsIssueRequest request){
        AppCidScrDlResponse appCidScrDlResponse = new AppCidScrDlResponse();
        String decryptedEncKey = tokenForNoDup(request);
        AppsRequest appsRequest = new AppsRequest();
        appsRequest.setAppKey(request.getAppKey());
        String entrCd = settingRepository.selectUseorgEntrCd(appsRequest);

        String appScr = null;
        try {
            appScr = AES256Util.decrypt(request.getAppScr());
        } catch (Exception e){
            log.error("키 다운로드 앱 시크릿 복호화 에러:"+e.toString());
            throw new BusinessException("E076",messageSource.getMessage("E076"));
        }

        String original = String.format("client_id:%s\nclient_secret:%s\nencKey:%s\napp_key:%s\nentrCd:%s"
                ,request.getAppClientId(),appScr,decryptedEncKey,request.getAppKey(), entrCd);
        String pubkey = Optional.ofNullable(request.getPubKey()).orElse("");

        String fileContext = "";
        try {
            fileContext = RSAUtil.encrypt(original, pubkey);
        } catch (Exception e) {
            log.error("키 다운로드 암호화 에러:"+e.toString());
            throw new BusinessException("E076",messageSource.getMessage("E076"));
        }

        appCidScrDlResponse.setToken(fileContext);
        appCidScrDlResponse.setProcDttm(DateUtil.getCurrentDateTime12());

        appsRepository.updateAppDldttm(request.getAppKey());
        return appCidScrDlResponse;
    }

    public AppCidScrDlResponse reissueKeydownload(AppsRequest.AppsIssueRequest request){
        String decryptedEncKey = tokenForNoDup(request);
        AppsRequest appsRequest = new AppsRequest();
        appsRequest.setAppKey(request.getAppKey());
        String entrCd = settingRepository.selectUseorgEntrCd(appsRequest);

        String newAppScr = null;
        String appScr = null;

        try {
            appScr = AES256Util.decrypt(request.getAppScr());
        } catch (Exception e){
            log.error("키 재발급 시크릿 복호화 에러 : " + e.toString());
            throw new BusinessException("E076",messageSource.getMessage("E076"));
        }

        try {
            newAppScr = AES256Util.decrypt(request.getNewAppScr());
        } catch (Exception e){
            log.error("키 재발급 다운로드 시크릿 복호화 에러 : " + e.toString());
            throw new BusinessException("E076",messageSource.getMessage("E076"));
        }

        String original = String.format("client_id:%s\nclient_secret:%s\nnew_client_id:%s\nnew_client_secret:%s\nencKey:%s\napp_key:%s\nentrCd:%s",
                request.getAppClientId(),appScr,request.getNewAppClientId(),newAppScr,decryptedEncKey,request.getAppKey(),entrCd);
        String pubkey = Optional.ofNullable(request.getPubKey()).orElse("");

        String fileContext = "";
        try {
            fileContext = RSAUtil.encrypt(original, pubkey);
        } catch (IllegalArgumentException e) {
            log.error("키 재발급 다운로드 암호화 에러 : " + e.toString());
            throw new BusinessException("E076",messageSource.getMessage("E076"));
        }
        AppCidScrDlResponse appCidScrDlResponse = new AppCidScrDlResponse();
        appCidScrDlResponse.setProcDttm(DateUtil.getCurrentDateTime12());
        appCidScrDlResponse.setToken(fileContext);

        appsRepository.updateAppDldttm(request.getAppKey());
        return appCidScrDlResponse;
    }

    public String tokenForNoDup(AppsRequest.AppsIssueRequest request){
        UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
        useorgDetailRequest.setUserKey(request.getUserKey());
        UseorgVO useorgVO = settingRepository.selectUseorg(useorgDetailRequest);
        String encKey = useorgVO.getEncKey();
        String token="";
        try {
            if(encKey != null && !encKey.isEmpty()) {
                token =  AES256Util.decrypt(encKey);
            }
        } catch (Exception e) {
            log.error("encKey DecryptAES256 Error", e);
            token = "encKey Error";
        }
        return token;
    }

    /**** 앱조회-등록취소 ****/
    public void updateCancelApp(AppsRequest appsRequest) {

//		appsRepository.updateCancelApp(appsRequest);
        AppsRegRequest appsRegRequest = new AppsRegRequest();
        appsRegRequest.setAppKey(appsRequest.getAppKey());
        appsRepository.deleteAppMod(appsRegRequest);
        updateAplvStatCdChange(appsRequest);
    }

    /**** 앱 상태변경 ****/
    public AppsRsponse updateAppStatCd(AppsRequest appsRequest) {

        String clientId = appsRequest.getAppClientId();
        String newClientId = appsRequest.getNewAppClientId();

        if(StringUtils.equals(appsRequest.getAppStatCd(), "OK")) {
            appsRequest.setAppStatCd("CLOSE");
        } else {
            appsRequest.setAppStatCd("OK");
        }

        int row = appsRepository.updateAppStatCd(appsRequest);

        // Redis Set
        try {
            if(StringUtils.equals(appsRequest.getAppStatCd(), "CLOSE")) {
                RedisCommunicater.appRedisSet(clientId, "false");
                if ("" != newClientId && null != newClientId){
                    RedisCommunicater.appRedisSet(newClientId, "false");
                }
            } else {
                RedisCommunicater.appRedisSet(clientId, "true");
                if ("" !=  newClientId && null != newClientId) {
                    RedisCommunicater.appRedisSet(newClientId, "true");
                }
            }
        } catch (Exception e) {
            if(StringUtils.equals(appsRequest.getAppStatCd(), "CLOSE")) {
                RedisCommunicater.appRedisSet(clientId, "true");
                if ("" != newClientId && null != newClientId){
                    RedisCommunicater.appRedisSet(newClientId, "true");
                }
            } else {
                RedisCommunicater.appRedisSet(clientId, "false");
                if ("" !=  newClientId && null != newClientId) {
                    RedisCommunicater.appRedisSet(newClientId, "false");
                }
            }
        }

        AppsRsponse appsRsponse = new AppsRsponse();
        if (row > 0) {
            AppsVO appsVO = appsRepository.selectAppDetail(appsRequest);
            appsRsponse.setAppStatCd(appsVO.getAppStatCd());
        }
        return appsRsponse;
    }

    /**** 등록취소-승인내역 ****/
    public void updateAplvStatCdChange(AppsRequest appsRequest){

        // 승인일련번호 조회
        AplvRequest.AplvRegistRequest aplvRegistRequest = new AplvRequest.AplvRegistRequest();
        aplvRegistRequest.setAplvReqCd(appsRequest.getAppKey());
        AplvVO aplv = settingRepository.selectAplvForAplvReqCd(aplvRegistRequest);

        // 승인상태코드 변경
        AplvRequest.AplvApprovalRequest aplvUpdateRequest = new AplvRequest.AplvApprovalRequest();
        aplvUpdateRequest.setAplvStatCd("CANCLE");
        aplvUpdateRequest.setAplvSeqNo(aplv.getAplvSeqNo());

        settingRepository.updateAplvStatCdChange(aplvUpdateRequest);

        // 할인 정책 원복
        RequestApiVO requestApiVO = new RequestApiVO();
        requestApiVO.setAppKey(appsRequest.getAppKey());
        settingRepository.deleteDiscountForWAIT(requestApiVO);
        settingRepository.updateDiscountByYForN(requestApiVO);
    }

    /**** 앱등록 ****/
    // 앱등록 승인 후 처리
    public void insertAppAfterAplv(AppsRegRequest appsRegRequest) {

        appsRepository.insertAppInfo(appsRegRequest);
        appsRepository.deleteAppMod(appsRegRequest);

        // app secret his 테이블에 시크릿 추가
        AppsScrRequest appsScrRequest = new AppsScrRequest();
        appsScrRequest.setAppKey(appsRegRequest.getAppKey());
        appsScrRequest.setAppClientId(appsRegRequest.getAppClientId());
        appsScrRequest.setAppScr(appsRegRequest.getAppScr());
        appsScrRequest.setAppScrVldDttm(appsRegRequest.getAppSvcEnDt());
        appsScrRequest.setRegUser(appsRegRequest.getRegUser());
        appsScrRequest.setRegUserId(appsRegRequest.getRegUserId());
        appsRepository.insertAppNewScrHis(appsScrRequest);

        // PORTAL_APP_INFO 테이블에 앱 이동 후 상태 변경
        AppsRegRequest r = new AppsRegRequest();
        r.setAppKey(appsRegRequest.getAppKey());
        r.setAppStatCd("UNUSED");
        r.setAppAplvStatCd("APLV");
        appsRepository.appStatChange(r);
    }

    // 수정 테이블에 앱 정보 생성(승인전)
    public void insertApp(AppsRegRequest appsRegRequest) {

        try {
            // 계좌번호
            String encryptedAccNo = AES256Util.encrypt(appsRegRequest.getAccNo());
            appsRegRequest.setAccNo(encryptedAccNo);
        } catch ( Exception e ) {
            log.error(e.getMessage());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

        try {
            appsRepository.insertAppMod(appsRegRequest);

            insertAppCnlKey(appsRegRequest, "WAIT");
            insertAppApiInfo(appsRegRequest, "WAIT");
            insertAplvUseorg(appsRegRequest, "APP");
        } catch ( Exception e) {
            log.error(e.toString());
            throw new BusinessException("E075",messageSource.getMessage("E075"));
        }
    }

    public void deleteApp(AppsRegRequest appsRegRequest) {

        // 앱 삭제 전 수정테이블에 삽입
        AppsRequest appsRequest = new AppsRequest();
        appsRequest.setAppKey(appsRegRequest.getAppKey());
        AppsVO appInfo = appsRepository.selectAppDetail(appsRequest);
        appsRegRequest.setAppNm(appInfo.getAppNm());
        appsRegRequest.setAppStatCd(appInfo.getAppStatCd());
        appsRegRequest.setAppAplvStatCd(appInfo.getAppAplvStatCd());
        appsRegRequest.setAppScr(appInfo.getAppScr());
        appsRegRequest.setAppSvcStDt(appInfo.getAppSvcStDt());
        appsRegRequest.setAppSvcEnDt(appInfo.getAppSvcEnDt());
        appsRegRequest.setAppCtnt(appInfo.getAppCtnt());
        appsRegRequest.setUserKey(appInfo.getUserKey());
        appsRegRequest.setTermEtdYn(appInfo.getTermEtdYn());
        appsRegRequest.setAppClientId(appInfo.getAppClientId());
        appsRegRequest.setAppScrReisuYn(appInfo.getAppScrReisuYn());
        appsRegRequest.setRegDttm(appInfo.getRegDttm());
        appsRegRequest.setRegUser(appInfo.getRegUser());
        appsRegRequest.setRegUserId(appInfo.getRegUserKey());

        // 암호화
        try {
            // 계좌번호
            if (appsRegRequest.getAccNo() != null && !"".equals(appsRegRequest.getAccNo())) {
                String encryptedAccNo = AES256Util.encrypt(appsRegRequest.getAccNo());
                appsRegRequest.setAccNo(encryptedAccNo);
                log.debug("암호화 - 계좌번호: {}", encryptedAccNo);
            }
        } catch ( Exception e ) {
            log.error(e.getMessage());
            throw new BusinessException("E073",messageSource.getMessage("E073"));
        }

        try {
            appsRepository.insertAppMod(appsRegRequest);
        }catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException("E071",messageSource.getMessage("E071"));
        }

        // 접수자 승인 생성
        AplvRequest.AplvRegistRequest aplvRegistRequest = new AplvRequest.AplvRegistRequest();
        aplvRegistRequest.setAplvDivCd("APPDEL");
        aplvRegistRequest.setAplvReqCd(appsRegRequest.getAppKey());
        aplvRegistRequest.setAplvReqCtnt(appsRegRequest.getAppNm());
        aplvRegistRequest.setRegUserName(appsRegRequest.getRegUser());
        aplvRegistRequest.setRegUserId(appsRegRequest.getUserKey());
        settingService.insertAplv(aplvRegistRequest);
    }

    public void insertAppCnlKey(AppsRegRequest appsRegRequest, String useFL) {
        for(AppsCnlKeyRequest cnlKey : appsRegRequest.getCnlKeyList()){
            cnlKey.setAppKey(appsRegRequest.getAppKey());
            cnlKey.setRegUser(appsRegRequest.getRegUser());
            cnlKey.setUseFl(useFL);
            cnlKey.setRegUserId(appsRegRequest.getRegUserId());
            appsRepository.insertAppCnlKey(cnlKey);
        }
    }

    public void insertAppApiInfo(AppsRegRequest appsRegRequest, String useFL) {
        for (AppsApiInfoRequest apiInfo : appsRegRequest.getApiList()) {
            apiInfo.setAppKey(appsRegRequest.getAppKey());
            apiInfo.setRegUser(appsRegRequest.getRegUser());
            ApiRequest apiRequest = new ApiRequest();
            apiRequest.setApiId(apiInfo.getApiId());
            apiInfo.setHfnCd(appsRepository.selectApi(apiRequest).getHfnCd());
            apiInfo.setUseFl(useFL);
            apiInfo.setRegUserId(appsRegRequest.getRegUserId());
            appsRepository.insertAppApiInfo(apiInfo);
        }

        AppsRequest appsReq = new AppsRequest();
        appsReq.setAppKey(appsRegRequest.getAppKey());
        // 기존에 존재한 API 구분
        List<AppApiInfoVO> oldApiList = appsRepository.selectAppApiInfo(appsReq);
        for (AppsApiInfoRequest apiInfo : appsRegRequest.getApiList()) {
            for (AppApiInfoVO appApiInfoVO : oldApiList) {
                if (StringUtils.equals(apiInfo.getApiId(), appApiInfoVO.getApiId())) {
                    apiInfo.setIsExist("EXIST");
                }
            }
        }

        for (AppsApiInfoRequest apiInfo : appsRegRequest.getApiList()) {
            RequestApiVO requestApiVO = new RequestApiVO();
            requestApiVO.setAppKey(appsRegRequest.getAppKey());
            requestApiVO.setApiId(apiInfo.getApiId());
            requestApiVO.setRegUserId(appsRegRequest.getRegUserId());

            if (appsRegRequest.getAppSvcStDt() != null && appsRegRequest.getAppSvcEnDt() != null) {
                String stDt = appsRegRequest.getAppSvcStDt();
                String enDt = appsRegRequest.getAppSvcEnDt();

                requestApiVO.setStDt(this.parseDateFormat(stDt));
                requestApiVO.setEnDt(this.parseDateFormat(enDt));
            } else {
                requestApiVO.setStDt(null);
                requestApiVO.setEnDt(null);
            }

            if (apiInfo.getIsExist() == null) {
                requestApiVO.setRegUser(appsRegRequest.getRegUser());
                settingRepository.insertApiChargeDiscountRate(requestApiVO);
            }
        }

        // 삭제된 API 구분
        for (AppApiInfoVO appApiInfoVO : oldApiList) {
            for (AppsApiInfoRequest apiInfo : appsRegRequest.getApiList()) {
                if (StringUtils.equals(apiInfo.getApiId(), appApiInfoVO.getApiId())) {
                    appApiInfoVO.setIsExist("EXIST");
                }
            }
        }

        for (AppApiInfoVO appApiInfoVO : oldApiList) {
            if (appApiInfoVO.getIsExist() == null) {
                RequestApiVO requestApiVO = new RequestApiVO();
                requestApiVO.setAppKey(appsRegRequest.getAppKey());
                requestApiVO.setApiId(appApiInfoVO.getApiId());
                requestApiVO.setRegUserId(appsRegRequest.getRegUserId());
                settingRepository.updateDiscountByNForAppKeyAndApiId(requestApiVO);
            }
        }
    }

    private String parseDateFormat(String date) {
        SimpleDateFormat beforeFormat = new SimpleDateFormat("yyyymmdd");
        SimpleDateFormat afterFormat = new SimpleDateFormat("yyyy-mm");

        Date tempDate = null;

        try {
            tempDate = beforeFormat.parse(date);

            return afterFormat.format(tempDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**** 앱등록-승인정보 ****/
    public void insertAplvUseorg(AppsRegRequest appsRegRequest, String aplvDivCd) {

        AplvRequest.AplvRegistRequest aplvRegistRequest = new AplvRequest.AplvRegistRequest();
        aplvRegistRequest.setAplvDivCd(aplvDivCd);
        aplvRegistRequest.setAplvReqCd(appsRegRequest.getAppKey());
        aplvRegistRequest.setAplvReqCtnt(appsRegRequest.getAppNm());
        aplvRegistRequest.setRegUserName(appsRegRequest.getRegUser());
        aplvRegistRequest.setRegUserId(appsRegRequest.getRegUserId());

        settingService.insertAplv(aplvRegistRequest);
    }

    public AppsRsponse selectAppDetail(AppsRequest appsRequest) {
        AppsVO apps;
        List<AppApiInfoVO> appApiInfoList;
        List<AppCnlInfoVO> appCnlInfoList;

        if ("edit".equals(appsRequest.getPage()) || appsRequest.getPage() == null) {
            apps = appsRepository.selectAppModifyDetail(appsRequest);   // APP_INFO 테이블에서 가져옵니다.(혼동주의)
            //앱 채널 정보, 앱 API 정보
            appApiInfoList = appsRepository.selectAppApiInfo(appsRequest);
            appCnlInfoList  = appsRepository.selectAppCnlInfo(appsRequest);
        } else {
            apps = appsRepository.selectAppManageDetail(appsRequest);   // APP_INFO_MOD 테이블에서 가져옵니다.(혼동주의)
            //앱 채널 정보, 앱 API 정보
            appApiInfoList = appsRepository.selectAppApiInfoDetail(appsRequest);
            appCnlInfoList  = appsRepository.selectAppCnlInfoDetail(appsRequest);
        }

        if (StringUtils.equals(apps.getRegUserNmEncrypted(), "Y")) {
            try {
                // 이름
                if (apps.getRegUser() != null && !"".equals(apps.getRegUser())) {
                    String decryptedUserNm = AES256Util.decrypt(apps.getRegUser());
                    apps.setRegUser(decryptedUserNm);
                    log.debug("복호화 - 이름: {}", decryptedUserNm);
                }
            } catch ( Exception e ) {
                log.error(e.getMessage());
                throw new BusinessException("E026",messageSource.getMessage("E026"));
            }
        }

        if (StringUtils.equals(apps.getModUserNmEncrypted(), "Y")) {
            try {
                // 이름
                if (apps.getModUser() != null && !"".equals(apps.getModUser())) {
                    String decryptedUserNm = AES256Util.decrypt(apps.getModUser());
                    apps.setModUser(decryptedUserNm);
                    log.debug("복호화 - 이름: {}", decryptedUserNm);
                }
            } catch ( Exception e ) {
                log.error(e.getMessage());
                throw new BusinessException("E026",messageSource.getMessage("E026"));
            }
        }


        // 복호화
        try {
            // 계좌번호
            if (apps.getAccNo() != null && !"".equals(apps.getAccNo())) {
                String decryptedAccNo = AES256Util.decrypt(apps.getAccNo());
                apps.setAccNo(decryptedAccNo);
                log.debug("복호화 - 계좌번호: {}", decryptedAccNo);
            }
        } catch ( Exception e ) {
            log.error(e.getMessage());
            throw new BusinessException("E073",messageSource.getMessage("E073"));
        }

        //카테고리 정보 포함한 API정보
        List<ApiVO> list = new ArrayList<>();
        for (AppApiInfoVO appApiInfo : appApiInfoList) {
            ApiRequest apiRequest = new ApiRequest();
            apiRequest.setApiId(appApiInfo.getApiId());
            ApiVO apiInfo = appsRepository.selectApi(apiRequest);
            list.add(apiInfo);
        }

        AppsRsponse response = new AppsRsponse();
        String appScr = apps.getAppScr();
        String newAppScr = apps.getNewAppScr();

        try {
            if(appScr != null && !appScr.isEmpty()) {
                response.setAppScr(AES256Util.decrypt(appScr));
            }
            if(newAppScr != null && !newAppScr.isEmpty()) {
                response.setNewAppScr(AES256Util.decrypt(newAppScr));
            }
        } catch (Exception e) {
            log.error("Client Secret DecryptAES256 Error", e);
        }

        response.setAppClientId(apps.getAppClientId());
        response.setNewAppClientId(apps.getNewAppClientId());
        response.setAppScr(apps.getAppScr());
        response.setNewAppScr(apps.getNewAppScr());
        response.setAppKey(apps.getAppKey());
        response.setAppNm(apps.getAppNm());
        response.setAccCd(apps.getAccCd());
        response.setAccNo(apps.getAccNo());
        response.setAppStatCd(apps.getAppStatCd());
        response.setAppSvcStDt(apps.getAppSvcStDt());
        response.setAppSvcEnDt(apps.getAppSvcEnDt());
        response.setAppCtnt(apps.getAppCtnt());
        response.setTermEtdYn(apps.getTermEtdYn());
        response.setAppScrReisuYn(apps.getAppScrReisuYn());
        response.setUserKey(apps.getUserKey());
        response.setUseorgNm(apps.getUseorgNm());
        response.setRegDttm(apps.getRegDttm());
        response.setRegUser(apps.getRegUser());
        response.setModDttm(apps.getModDttm());
        response.setModUser(apps.getModUser());
        response.setAppScrVldDttm(apps.getAppScrVldDttm());
        response.setAppApiInfo(appApiInfoList);
        response.setDlDttm(apps.getDlDttm());
        response.setCnlKeyList(appCnlInfoList);
        response.setApiList(list);
        response.setAppAplvStatCd(apps.getAppAplvStatCd());

//        try {
//            if (apps.getRegUserNm() != null && !apps.getRegUserNm().isEmpty()) {
//                response.setRegUserNm(AES256Util.decrypt(apps.getRegUserNm()));
//            }
//            if (apps.getModUserNm() != null && !apps.getModUserNm().isEmpty()) {
//                response.setModUserNm(AES256Util.decrypt(apps.getModUserNm()));
//            }
//        } catch (Exception e) {
//            log.error("정상상태 앱 등록,수정유저 이름 복호화 혹은 null 에러 :" + e);
//            e.printStackTrace();
//            throw new BusinessException(messageSource.getMessage("E026"));
//        }

        return response;
    }

    // 앱 수정시 수정테이블에 삽입
    public void updateAppInfo(AppsRegRequest request) {

        // 암호화
        try {
            // 계좌번호
            if (request.getAccNo() != null && !"".equals(request.getAccNo())) {
                String encryptedAccNo = AES256Util.encrypt(request.getAccNo());
                request.setAccNo(encryptedAccNo);
                log.debug("암호화 - 계좌번호: {}", encryptedAccNo);
            }
        } catch ( Exception e ) {
            log.error(e.getMessage());
            throw new BusinessException("E073",messageSource.getMessage("E073"));
        }

        System.out.println("★request:" + request);
        appsRepository.insertAppModForUpdate(request);

        insertAppApiInfo(request, "WAIT");
        insertAppCnlKey(request, "WAIT");

        // 접수자 승인 생성
        insertAplvUseorg(request, "APPEDIT");
    }

    // Redis에서 app-api와 app-channel 정보 업데이트
    public void updateRedisInfo(AppsRegRequest request) {

        // 앱 채널(IP) 정보
        List<AppsCnlKeyRequest> nwCnl = request.getCnlKeyList();
        List<AppsCnlKeyRequest> exCnl = request.getExCnlKeyList();
        List<AppsCnlKeyRequest> finalExKey = exCnl;
        List<AppsCnlKeyRequest> finalNwKey = nwCnl;
        exCnl = exCnl.stream().filter(i -> !finalNwKey.contains(i)).collect(Collectors.toList());
        nwCnl = nwCnl.stream().filter(i -> !finalExKey.contains(i)).collect(Collectors.toList());

        request.setExCnlKeyList(exCnl);
        request.setCnlKeyList(nwCnl);

        // 앱 API 정보
        List<AppsApiInfoRequest> nwApi = request.getApiList();
        List<AppsApiInfoRequest> exApi = request.getExApiList();
        List<AppsApiInfoRequest> finalNwApi = nwApi;
        List<AppsApiInfoRequest> finalExApi = exApi;
        exApi = exApi.stream().filter(i -> !finalNwApi.contains(i)).collect(Collectors.toList());
        nwApi = nwApi.stream().filter(i -> !finalExApi.contains(i)).collect(Collectors.toList());

        request.setApiList(nwApi);
        request.setExApiList(exApi);

        // Redis
        String clientId = request.getAppClientId();
        List<ApiVO> redisExApi = request.getRedisExApiList();
        List<ApiVO> redisNwApi = request.getRedisApiList();
        List<ApiVO> finalRedisNwApi = redisNwApi;
        List<ApiVO> finalRedisExApi = redisExApi;
        if (finalRedisNwApi != null) {
            redisExApi = redisExApi.stream().filter(i -> !finalRedisNwApi.contains(i)).collect(Collectors.toList());
        }
        if (finalRedisExApi != null) {
            redisNwApi = redisNwApi.stream().filter(i -> !finalRedisExApi.contains(i)).collect(Collectors.toList());
        }

        // Redis API
        if (redisExApi != null) {
            try {
                for(ApiVO apiInfo : redisExApi){
                    RedisCommunicater.appApiRedisDel(clientId, apiInfo.getApiUrl());
                    if(StringUtils.equals(request.getAppScrReisuYn(), "Y")){
                        RedisCommunicater.appApiRedisDel(request.getNewAppClientId(), apiInfo.getApiUrl());
                    }
                }
            } catch (Exception e) {
                for(ApiVO apiInfo : redisExApi){
                    RedisCommunicater.appApiRedisSet(clientId, apiInfo.getApiUrl(), "true");
                    if(StringUtils.equals(request.getAppScrReisuYn(), "Y")){
                        RedisCommunicater.appApiRedisSet(request.getNewAppClientId(), apiInfo.getApiUrl(), "true");
                    }
                }
            }
        }
        if (redisNwApi != null) {
            try {
                for(ApiVO apiInfo : redisNwApi){
                    RedisCommunicater.appApiRedisSet(clientId, apiInfo.getApiUrl(), "true");
                    if(StringUtils.equals(request.getAppScrReisuYn(), "Y")){
                        RedisCommunicater.appApiRedisSet(request.getNewAppClientId(), apiInfo.getApiUrl(), "true");
                    }
                }
            } catch (Exception e) {
                for(ApiVO apiInfo : redisNwApi){
                    RedisCommunicater.appApiRedisDel(clientId, apiInfo.getApiUrl());
                    if(StringUtils.equals(request.getAppScrReisuYn(), "Y")){
                        RedisCommunicater.appApiRedisDel(request.getNewAppClientId(), apiInfo.getApiUrl());
                    }
                }
            }
        }

        // Redis IP
        try {
            for(AppsCnlKeyRequest del : exCnl){
                RedisCommunicater.appIpRedisDel(clientId, del.getCnlKey());
                if(StringUtils.equals(request.getAppScrReisuYn(), "Y")){
                    RedisCommunicater.appIpRedisDel(request.getNewAppClientId(), del.getCnlKey());
                }
            }
        } catch (Exception e) {
            for(AppsCnlKeyRequest del : exCnl){
                RedisCommunicater.appIpRedisSet(clientId, del.getCnlKey(), "true");
                if(StringUtils.equals(request.getAppScrReisuYn(), "Y")){
                    RedisCommunicater.appIpRedisSet(request.getNewAppClientId(), del.getCnlKey(), "true");
                }
            }
        }
        try {
            for(AppsCnlKeyRequest set : nwCnl){
                RedisCommunicater.appIpRedisSet(clientId, set.getCnlKey(), "true");
                if(StringUtils.equals(request.getAppScrReisuYn(), "Y")){
                    RedisCommunicater.appIpRedisSet(request.getNewAppClientId(), set.getCnlKey(), "true");
                }
            }
        } catch (Exception e) {
            for(AppsCnlKeyRequest set : nwCnl){
                RedisCommunicater.appIpRedisDel(clientId, set.getCnlKey());
                if(StringUtils.equals(request.getAppScrReisuYn(), "Y")){
                    RedisCommunicater.appIpRedisDel(request.getNewAppClientId(), set.getCnlKey());
                }
            }
        }
    }

    public void delAppApiInfo(AppsRegRequest appsRegRequest) {
        for(AppsApiInfoRequest api : appsRegRequest.getExApiList()) {
            api.setAppKey(appsRegRequest.getAppKey());
            api.setRegUser(appsRegRequest.getRegUser());
            api.setRegUserId(appsRegRequest.getRegUserId());
            appsRepository.delAppApiInfo(api);
        }
    }

    public void delAppCnlInfo(AppsRegRequest appsRegRequest) {
        for(AppsCnlKeyRequest cnlKey : appsRegRequest.getExCnlKeyList()){
            cnlKey.setAppKey(appsRegRequest.getAppKey());
            cnlKey.setRegUser(appsRegRequest.getRegUser());
            cnlKey.setRegUserId(appsRegRequest.getRegUserId());
            appsRepository.delAppCnlInfo(cnlKey);
        }
    }

    public void appExtend(AppsRegRequest appsRegRequest) {
        // 1) 앱정보 기간연장여부(TERM_ETD_YN) 변경
        appsRepository.updateAppTerm(appsRegRequest);

        // 2) 승인정보 승인구분코드(APLV_DIV_CD) 변경
        AplvRequest.AplvRegistRequest aplvRegistRequest = new AplvRequest.AplvRegistRequest();
        aplvRegistRequest.setAplvDivCd("APPEXP");
        aplvRegistRequest.setAplvReqCd(appsRegRequest.getAppKey());
        aplvRegistRequest.setAplvReqCtnt(appsRegRequest.getAppNm());
        aplvRegistRequest.setRegUserName(appsRegRequest.getModUser());
        aplvRegistRequest.setRegUserId(appsRegRequest.getModUserId());

        settingService.insertAplv(aplvRegistRequest);
    }

    /**** 앱 수정 테이블 정보조회(승인대기) ****/
    public AppsRsponse selectAppDetailMod(AppsRequest appsRequest) {

        AppsVO apps = appsRepository.selectAppModDetail(appsRequest);

        // 복호화
        try {
            // 계좌번호
            if (apps.getAccNo() != null && !"".equals(apps.getAccNo())) {
                String decryptedAccNo = AES256Util.decrypt(apps.getAccNo());
                apps.setAccNo(decryptedAccNo);
                log.debug("복호화 - 계좌번호: {}", decryptedAccNo);
            }
        } catch ( Exception e ) {
            log.error(e.toString());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

        //앱 채널 정보, 앱 API 정보
        List<AppApiInfoVO> appApiInfoList = appsRepository.selectAppApiInfoMod(appsRequest);
        List<AppCnlInfoVO> appCnlInfoList  = appsRepository.selectAppCnlInfoMod(appsRequest);

        //카테고리 정보 포함한 API정보
        List<ApiVO> list = new ArrayList<>();
        for (AppApiInfoVO appApiInfo : appApiInfoList) {
            ApiRequest apiRequest = new ApiRequest();
            apiRequest.setApiId(appApiInfo.getApiId());
            ApiVO apiInfo = appsRepository.selectApi(apiRequest);
            list.add(apiInfo);
        }


        AppsRsponse response = new AppsRsponse();
        String appScr = apps.getAppScr();
        String newAppScr = apps.getNewAppScr();

        try {
            if(appScr != null && !appScr.isEmpty()) {
                response.setAppScr(AES256Util.decrypt(appScr));
            }
            if(newAppScr != null && !newAppScr.isEmpty()) {
                response.setNewAppScr(AES256Util.decrypt(newAppScr));
            }
        } catch (Exception e) {
            log.error("Client Secret DecryptAES256 Error", e);
        }

        response.setAppClientId(apps.getAppClientId());
        response.setNewAppClientId(apps.getNewAppClientId());
        response.setAppScr(apps.getAppScr());
        response.setAppKey(apps.getAppKey());
        response.setAppNm(apps.getAppNm());
        response.setAccCd(apps.getAccCd());
        response.setAccNo(apps.getAccNo());
        response.setAppStatCd(apps.getAppStatCd());
        response.setAppSvcStDt(apps.getAppSvcStDt());
        response.setAppSvcEnDt(apps.getAppSvcEnDt());
        response.setAppCtnt(apps.getAppCtnt());
        response.setTermEtdYn(apps.getTermEtdYn());
        response.setAppScrReisuYn(apps.getAppScrReisuYn());
        response.setUserKey(apps.getUserKey());
        response.setUseorgNm(apps.getUseorgNm());
        response.setRegDttm(apps.getRegDttm());
        response.setRegUser(apps.getRegUser());
        response.setModDttm(apps.getModDttm());
        response.setModUser(apps.getModUser());
        response.setAppScrVldDttm(apps.getAppScrVldDttm());
        response.setAppApiInfo(appApiInfoList);
        response.setCnlKeyList(appCnlInfoList);
        response.setApiList(list);

//        try {
//            if (apps.getRegUserNm() != null && !apps.getRegUserNm().isEmpty()) {
//                response.setRegUserNm(AES256Util.decrypt(apps.getRegUserNm()));
//            }
//            if (apps.getModUserNm() != null && !apps.getModUserNm().isEmpty()) {
//                response.setModUserNm(AES256Util.decrypt(apps.getModUserNm()));
//            }
//        } catch (Exception e) {
//            log.error("승인대기앱 등록,수정유저 이름 복호화 혹은 null 에러 :" + e);
//            e.printStackTrace();
//            throw new BusinessException(messageSource.getMessage("E026"));
//        }

        return response;
    }

    public List<AppsVO> selectAppModAll(AppsRequest appsRequest) {
        return appsRepository.selectAppsListMod(appsRequest);
    }

    public AppsRsponsePaging selectAppsListPagingMod(AppsRequest appsRequest){
        if(appsRequest.getPageIdx() == 0)
            appsRequest.setPageIdx(appsRequest.getPageIdx() + 1);

        if(appsRequest.getPageSize() == 0){
            appsRequest.setPageSize(20);
        }

        appsRequest.setPageOffset((appsRequest.getPageIdx()-1)*appsRequest.getPageSize());
        int totCnt = appsRepository.countAppsListMod(appsRequest);
        List<AppsVO> list = appsRepository.selectAppsListMod(appsRequest);

        AppsRsponsePaging pagingData = new AppsRsponsePaging();
        pagingData.setPageIdx(appsRequest.getPageIdx());
        pagingData.setPageSize(appsRequest.getPageSize());
        pagingData.setTotCnt(totCnt);
        pagingData.setList(list);
        pagingData.setSelCnt(list.size());

        return pagingData;
    }

    /**
     * Secret Key, Client ID 재발급
     * @param appsRegRequest
     * @return
     */
    public AppsRsponse appSecretReisu(AppsRegRequest appsRegRequest) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 7);
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String expireDay = df.format(cal.getTime());

        //클라이언트 ID, 시크릿 재발급
        AppsRequest appsRequest = new AppsRequest();
        appsRequest.setAppKey(appsRegRequest.getAppKey());

        AppsVO appsVO = settingRepository.selectUseorgEntrCdFromInfo(appsRequest);
        String encKey = appsVO.getEncKey();
        String entrCd = appsVO.getEntrCd();

        // 재발행 진행중일 시 에러처리 추가 2019/11/13
        AppsVO appsVO2 = appsRepository.selectAppDetail(appsRequest);
        if(StringUtils.equals(appsVO2.getAppScrReisuYn(),"Y")) {
            log.error("앱키 재발행 진행중이므로 취소처리. (appkeyReisu)");
            throw new BusinessException("A001",messageSource.getMessage("A001"));
        }

        try {
            encKey = AES256Util.decrypt(encKey);
            log.debug("[Client/Secret 재발급] ENC KEY 복호화 : "+encKey);
        } catch ( Exception e ){
            log.error("[Client/Secret 재발급] ENC KEY 복호화 에러 : "+e.toString());
            throw new BusinessException("E072",messageSource.getMessage("E072"));
        }

        GWResponse gwResponse = GWCommunicater.clientSecretNewPassword(appsVO.getHfnCd(), appsRegRequest.getAppClientId(), expireDay, entrCd, encKey);

        String clientId		= (String)gwResponse.getDataBody().get("clientId");
        String clientScr	= (String)gwResponse.getDataBody().get("clientSecret");
        String decryptId	= "";
        String decryptScr	= "";
        String encryptScr	= "";

        try {
            decryptId = AES256Util.decrypt(encKey, clientId).split(":")[0];
            log.debug("[Client/Secret 재발급] CID ["+decryptId+"]");
            decryptScr = AES256Util.decrypt(encKey, clientScr).split(":")[0];
            log.debug("[Client/Secret 재발급] SCR ["+decryptScr+"]");
            encryptScr = AES256Util.encrypt(decryptScr);	// DB에 넣을땐 기본 AES

        } catch (Exception e) {
            log.error("Client Secret EncryptAES256 Error", e);
            throw new BusinessException("E072",messageSource.getMessage("E072"));
        }
        // 앱 정보
        appsRegRequest.setNewAppClientId(decryptId);
        appsRegRequest.setNewAppScr(encryptScr);
        appsRepository.updateAppScr(appsRegRequest);
//        appsRepository.updateAppScrMod(appsRegRequest);

        // 앱 Secret 내역 Update (기존)
        AppsScrRequest appsScrRequest = new AppsScrRequest();
        appsScrRequest.setAppKey(appsRegRequest.getAppKey());
        appsScrRequest.setAppScrVldDttm(expireDay);
        appsScrRequest.setModUser(appsRegRequest.getModUser());
        appsScrRequest.setModUserId(appsRegRequest.getModUserId());
        appsRepository.updateAppScrHis(appsScrRequest);

        // 앱 Secret 내역 Insert (신규)
        appsScrRequest = new AppsScrRequest();
        appsScrRequest.setAppKey(appsRegRequest.getAppKey());
        appsScrRequest.setAppClientId(decryptId);
        appsScrRequest.setAppScr(encryptScr);
        appsScrRequest.setAppScrVldDttm(appsRegRequest.getAppSvcEnDt());
        appsScrRequest.setRegUser(appsRegRequest.getModUser());
        appsScrRequest.setRegUserId(appsRegRequest.getRegUserId());
        appsRepository.insertAppNewScrHis(appsScrRequest);

        // Redis
        List<ApiVO> apiList = appsRegRequest.getRedisExApiList();
        List<AppsCnlKeyRequest> cnlKeyList = appsRegRequest.getExCnlKeyList();

        try {
            RedisCommunicater.appRedisSet(decryptId, "true");
        } catch (Exception e) {
            RedisCommunicater.appRedisDel(decryptId);
        }

        try {
            for(ApiVO apiInfo : apiList){
                RedisCommunicater.appApiRedisSet(decryptId, apiInfo.getApiUrl(), "true");
            }
        } catch (Exception e) {
            for(ApiVO apiInfo : apiList){
                RedisCommunicater.appApiRedisDel(decryptId, apiInfo.getApiUrl());
            }
        }

        try {
            for(AppsCnlKeyRequest cnl : cnlKeyList){
                RedisCommunicater.appIpRedisSet(decryptId, cnl.getCnlKey(), "true");
            }
        } catch (Exception e) {
            for(AppsCnlKeyRequest cnl : cnlKeyList){
                RedisCommunicater.appIpRedisDel(decryptId, cnl.getCnlKey());
            }
        }

        AppsRsponse response = new AppsRsponse();
        response.setAppClientId(appsRegRequest.getAppClientId());
        response.setNewAppClientId(decryptId);
        response.setAppScr(appsRegRequest.getAppScr());
        response.setNewAppScr(encryptScr);
        response.setAppScrReisuYn("Y");
        response.setModDttm(expireDay);

        return response;
    }

    public AppsRsponse keyReturn(AppsScrRequest appsScrRequest) {
        AppsVO apps = appsRepository.keyReturn(appsScrRequest);
        AppsRsponse response = new AppsRsponse();
        response.setAppClientId(apps.getAppClientId());
        response.setAppScr(apps.getAppScr());

        return response;
    }

    public HfnUserRoleVO hfnUserRole(UserRequest request) {
        HfnUserRoleVO role = appsRepository.hfnUserRole(request);
        return role;
    }

    public HfnUseorgListResponse useorgList(HfnUseorgListRequest request) {
        List<UseorgVO> list = appsRepository.useorgList(request);
        HfnUseorgListResponse resp = new HfnUseorgListResponse();

        resp.setList(list);

        return resp;
    }

    public List<AppsVO> selectAppAll(AppsRequest appsRequest) {
        return appsRepository.selectAppListAll(appsRequest);
    }

    public List<AppsVO> selectAppAllFixed(AppsRequest appsRequest) {
        return appsRepository.appListSelectedNoLPaging(appsRequest);
    }

    public AppsRsponsePaging appListSelected(AppsRequest request) {

        if(request.getPageIdx() == 0) {
            request.setPageIdx(request.getPageIdx()+1);
        }
        if(request.getPageSize() == 0) {
            request.setPageSize(20);
        }

        request.setPageOffset((request.getPageIdx()-1) * request.getPageSize());
        int totCnt = appsRepository.countAppListSelected(request);
        List<AppsVO> list = appsRepository.appListSelected(request);

        for (AppsVO appsVo: list) {
            if (StringUtils.equals(appsVo.getUserNmEncrypted(), "Y")) {
                try {
                    // 이름
                    if (appsVo.getRegUser() != null && !"".equals(appsVo.getRegUser())) {
                        String decryptedUserNm = AES256Util.decrypt(appsVo.getRegUser());
                        appsVo.setRegUser(decryptedUserNm);
                        log.debug("복호화 - 이름: {}", decryptedUserNm);
                    }
                } catch ( Exception e ) {
                    log.error(e.getMessage());
                    throw new BusinessException("E026",messageSource.getMessage("E026"));
                }
            }
        }

        AppsRsponsePaging resp = new AppsRsponsePaging();
        resp.setList(list);
        resp.setPageIdx(request.getPageIdx());
        resp.setPageSize(request.getPageSize());
        resp.setTotCnt(totCnt);
        resp.setSelCnt(list.size());

        return resp;
    }

    public HfnCompanyAllResponse hfnCompanyAll(UseorgRequest request) {
        List<HfnUserRoleVO> list = appsRepository.hfnCompanyAll(request);
        HfnCompanyAllResponse resp = new HfnCompanyAllResponse();
        resp.setList(list);
        return resp;
    }

    public ResponseEntity<?> accountStatus(UserPrincipal currentUser, HttpServletRequest request, String searchAccountBankCd) {
        return accountStatusCommunicater.communicateServer(currentUser, request, searchAccountBankCd);
    }

    // 계좌검증(하나 금융 투자)
    public ResponseEntity<?> accountStatusForHnw(UserPrincipal currentUser, HttpServletRequest request, String searchAccountBankCd) {
        return accountStatusCommunicater.communicateServerForHnw(currentUser, request, searchAccountBankCd);
    }

    public List<AppsVO> fetchAppListInChargeDiscountRate(AppsRequest request) {
        return appsRepository.selectAppListInChargeDiscountRate(request);
    }

    public List<ApiVO> fetchApiListInChargeDiscountRate(AppsRequest request) {
        return appsRepository.selectApiListInChargeDiscountRate(request);
    }

    /** 앱 별 항목암호화 키 리스트 가져오기 **/
    public AppsRsponse.AppCryptoKeyResponse selectCryptoKeyList(AppsRequest.AppCryptoKeyRequest request) {

        if(request.getPageIdx() == 0) {
            request.setPageIdx(request.getPageIdx() + 1);
        }

        if(request.getPageSize() == 0){
            request.setPageSize(20);
        }

        request.setPageOffset((request.getPageIdx() - 1) * request.getPageSize());

        int totCnt = appsRepository.cntSelectAppCryptoKey(request);
        List<AppCryptoKeyVO> list = appsRepository.selectAppCryptoKey(request);

        AppsRsponse.AppCryptoKeyResponse response = new AppsRsponse.AppCryptoKeyResponse();
        response.setKeyList(list);
        response.setTotCnt(totCnt);
        response.setSelCnt(list.size());

        return response;
    }

    /** 앱 별 항목암호화 키 관리 : 검색한 앱 리스트 가져오기 **/
    public AppsRsponse.AppCryptoKeyResponse selectCryptoKeyAppList(AppsRequest.AppCryptoKeyRequest request) {

        List<AppsVO> list = appsRepository.selectCryptoKeyAppList(request);
        AppsRsponse.AppCryptoKeyResponse response = new AppsRsponse.AppCryptoKeyResponse();
        response.setAppList(list);

        return response;
    }
}