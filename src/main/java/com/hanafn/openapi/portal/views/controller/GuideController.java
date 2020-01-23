package com.hanafn.openapi.portal.views.controller;

import com.hanafn.openapi.portal.cmct.GWCommunicater;
import com.hanafn.openapi.portal.cmct.dto.GWResponse;
import com.hanafn.openapi.portal.cmct.dto.OAuthRequest;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.service.GuideService;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.ApiCtgrVO;
import com.hanafn.openapi.portal.views.vo.ApiDevGuideVO;
import com.hanafn.openapi.portal.views.vo.UseorgVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GuideController {

    private final SettingService settingService;
    private final GuideService guideService;
    private final CommonUtil commonUtil;
    private final MessageSourceAccessor messageSource;
    private static final Logger logger = LoggerFactory.getLogger(GuideController.class);

    @Value("${spring.profiles.active}")
    private String thisServer;

    @PostMapping("/guideGw")
    public ResponseEntity<?> guideOAuth(@CurrentUser UserPrincipal currentUser, HttpServletRequest servletRequest, @Valid @RequestBody GuideRequest request) {

        OAuthRequest.ClientInfo clientInfo = new OAuthRequest.ClientInfo();

        clientInfo.setClientId(request.getClientId());
        clientInfo.setEntrCd(request.getEntrCd());
        String encKey = guideService.selectEnckeyByEntrCd(request.getEntrCd());
        log.debug("★encKey : "+ encKey);

        String decryptedEnckey = null;

        try {
            decryptedEnckey = AES256Util.decrypt(encKey);
            log.debug("★decryptedEnckey:"+decryptedEnckey);
            clientInfo.setDecryptedEnckey(decryptedEnckey);
            clientInfo.setClientSecret(AES256Util.decrypt(request.getClientSecret()));
            log.debug("★decrypted Secret:"+AES256Util.decrypt(request.getClientSecret()));
            clientInfo.setHfnCd(request.getHfnCd());
        } catch (Exception e) {
            log.error("ClientSecret Decrypt Error", e);
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

        // ENCKEY로 암호화 된 ACCESS TOKEN이 반환된다.
        GWResponse res = GWCommunicater.getAceessToken(clientInfo);
        String accessToken = (String)res.getDataBody().get("access_token");

        String clientIpAddr = servletRequest.getHeader("X-FORWARDED-FOR");
        if (clientIpAddr == null ) clientIpAddr = servletRequest.getRemoteAddr();

        Map<String, Object> dataHeader = new HashMap<>();
        dataHeader.put("CNTY_CD","kr");
        dataHeader.put("CLNT_IP_ADDR",clientIpAddr);
        dataHeader.put("ENTR_CD",request.getEntrCd());

        Map<String, Object> params = new HashMap<>();
        params.put("dataHeader", dataHeader);
        params.put("dataBody", request.getParams());
        params.put("decryptedEnckey",clientInfo.getDecryptedEnckey());

        params.put("ENTR_CD",request.getEntrCd());
        params.put("APP_KEY",request.getAppKey());

        // client id set
        params.put("client_id", request.getClientId());

        // ENCKEY 암호화된 ACCESSTOKEN 그대로 넘겨준다.
        // GW통신 요청 : [Authorization Aes256Uitil.encrypt(decryptedEncKey, (복호화된 ACCESSTOKEN:UNIXTIME:CLIENT_ID))]
        // GW통신 응답 : GW 응답 전문(암호화X)

        /**2019.12.12 품질 통계로그 쌓기 위해 관계사GW 구분하여 호출
         * @개발
         * onegw  : https://10.168.36.224:22001/onegw/hbk-dev/api/hbk-service/v1/exchangeRate
         * onefgw : https://10.168.36.224:22001/onefgw/file-dev/api/hbk-service/v1/exchangeRate
         * @품질/운영
         * onegw  : https://10.168.36.224:22001/onegw/hbk-dev/api/hbk-service/v1/exchangeRate
         * onefgw : https://10.168.36.224:22001/onefgw/file-dev/api/hbk-service/v1/exchangeRate
        */
        // 관계사코드 + URL
        String hfnCd = HfnEnum.resolve(request.getHfnCd()).getCode();
        if ("onefgw".equals(request.getGwType())) hfnCd = "file";
        if (thisServer.equals("development")) hfnCd +="-dev";
        String url = hfnCd + request.getUri();

        res = GWCommunicater.communicateGateway(request.getGwType(), url, HttpMethod.valueOf(request.getMethod()), accessToken, params);

        return ResponseEntity.ok(res);
    }

    /*
     * ******************************개발 가이드******************************
     * */

    @PostMapping("/devGuides")
    public ResponseEntity<?> devGuideList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiDevGuideRequest request) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities){
            if(!ga.getAuthority().contains("ROLE_SYS_ADMIN")){
                request.setSearchHfnCd(currentUser.getHfnCd());
            }
        }

        List<ApiCtgrVO> searchApiCtgrList = request.getSearchApiCtgrList();
        String qApiCtgrs = guideService.settingSearchApiCtgrs(searchApiCtgrList);
        request.setSearchApiCtgrs(qApiCtgrs);

        List<ApiDevGuideVO> data = null;

        try {
            if (StringUtils.isBlank(request.getSearchUserKey())) {
                data = guideService.selectApiDevGuideAllList(request);
            } else {
                data = guideService.selectApiDevGuideList(request);
            }

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("execute Error", e);
            throw new BusinessException("E020",messageSource.getMessage("E020"));
        }
    }

    @PostMapping("/appsAll")
    public ResponseEntity<?> appsAll(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsAllRequest request) {
        AppsAllRsponse data = guideService.selectAppsAll(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/devGuidesUseorgAll")
    public ResponseEntity<?> devGuidesUseorgAll(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiDevGuideRequest.ApiDevGuideUseorgAllRequest request) {
        UseorgRsponse data = guideService.selectDevGuidesUseorgAll(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/devGuidesApiCtgrAll")
    public ResponseEntity<?> devGuidesApiCtgrAll(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiDevGuideRequest request) {

        ApiCtgrRsponse data = null;

        if(!StringUtils.isBlank(request.getSearchHfnCd())){
            data = guideService.selectDevGuidesApiCtgrForUseorgList(request);
        }else{
            data = guideService.selectDevGuidesApiCtgrAll(request);
        }

        return ResponseEntity.ok(data);
    }

    @PostMapping("/devGuidesAppsAll")
    public ResponseEntity<?> devGuidesAppsAll(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiDevGuideRequest.ApiDevGuideApiAllRequest request) {

        UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
        useorgDetailRequest.setUserKey(request.getSearchUserKey());

        UseorgVO useorgInfo = settingService.selectUseorg(useorgDetailRequest);
        AppsAllRsponse.devGuidesAppsAllRsponse data = guideService.selectDevGuidesAppsAll(request);

        data.setEntrCd(useorgInfo.getEntrCd());

        return ResponseEntity.ok(data);
    }
}