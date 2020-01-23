package com.hanafn.openapi.portal.views.controller;

import com.hanafn.openapi.portal.cmct.GWCommunicater;
import com.hanafn.openapi.portal.cmct.dto.GWResponse;
import com.hanafn.openapi.portal.cmct.dto.OAuthRequest;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.service.ApiService;
import com.hanafn.openapi.portal.views.service.ApisService;
import com.hanafn.openapi.portal.views.service.GuideService;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.ApiCtgrVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/apis")
@Slf4j
public class ApisController {

    @Value("${sandboxEntrCd}")
    private String sandboxEntrCd;

    @Value("${sandboxClientId}")
    private String sandboxClientId;

    @Value("${sandboxClientSecret}")
    private String sandboxClientSecret;

    @Autowired
    MessageSourceAccessor messageSource;
    @Autowired
    ApisService apisService;
    @Autowired
    SettingService settingService;
    @Autowired
    GuideService guideService;

    @PostMapping("/apis")
    public ResponseEntity<?> apis(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApisRequest request) {
        log.debug("★request:"+request);
        ApiCtgrRsponse.ApisResponse data = apisService.getApis(request);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/apisAll")
    public ResponseEntity<?> apisAll(@CurrentUser UserPrincipal currentUser) {
        List<ApiCtgrVO> data = apisService.getApisAll();
        return ResponseEntity.ok(data);
    }

    @PostMapping("/sandBoxCall")
    public ResponseEntity<?> sandBoxCall(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GuideRequest request) {

        OAuthRequest.ClientInfo clientInfo = new OAuthRequest.ClientInfo();
        clientInfo.setClientId(sandboxClientId);
        clientInfo.setEntrCd(sandboxEntrCd);
        clientInfo.setHfnCd(request.getHfnCd());
        String encKey = guideService.selectEnckeyByEntrCd(sandboxEntrCd);
        log.debug("★encKey : "+ encKey);

        String decryptedEnckey = null;
        try {
            decryptedEnckey = AES256Util.decrypt(encKey);
            log.debug("★decryptedEnckey:"+decryptedEnckey);
            clientInfo.setDecryptedEnckey(decryptedEnckey);
            clientInfo.setClientSecret(AES256Util.decrypt(sandboxClientSecret));
            log.debug("★decrypted Secret:"+AES256Util.decrypt(sandboxClientSecret));
        } catch (Exception e) {
            log.error("ClientSecret Decrypt Error", e);
        }

        GWResponse res = GWCommunicater.getAceessToken(clientInfo);
        String accessToken = (String)res.getDataBody().get("access_token");

        Map<String, Object> params = new HashMap<>();

        Map<String, Object> dataHeader = new HashMap<>();
        dataHeader.put("CNTY_CD","kr");
        dataHeader.put("CLNT_IP_ADDR","");
        dataHeader.put("ENTR_CD",sandboxEntrCd);

        params.put("dataHeader", dataHeader);
        params.put("dataBody", request.getParams());
        params.put("decryptedEnckey",clientInfo.getDecryptedEnckey());
        params.put("ENTR_CD",sandboxEntrCd);

        // client id set
        params.put("client_id", sandboxClientId);

        // 관계사코드 + URL
        String hfnCd = HfnEnum.resolve(request.getHfnCd()).getCode();
        String url = hfnCd + request.getUri();

        // 복호화된 accesstoken 넘겨준다.
        res = GWCommunicater.communicateGateway("", url, HttpMethod.valueOf(request.getMethod()), accessToken, params);
        return ResponseEntity.ok(res);
    }
}
