package com.hanafn.openapi.portal.cmct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.hanafn.openapi.portal.cmct.dto.HubResponse;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.views.dto.CommonApiResponse;
import com.hanafn.openapi.portal.views.dto.SndCertMgntRequest;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class HubCommunicator {

    private static MessageSourceAccessor messageSource;
    private static String hfnPushMessageUrl;
    private static String hfnPushMessageKey;

    @Autowired
    SettingRepository settingRepository;

    @Value("${spring.profiles.active}")
    private String thisServer;

    @Autowired
    public HubCommunicator(MessageSourceAccessor messageSource) { this.messageSource = messageSource; }

    @Value("${hfn.push.message.url}")
    public void setHfnPushMessageUrl (String url) { this.hfnPushMessageUrl = url; }

    @Value("${hfn.push.message.key}")
    public void setHfnPushMessageKey (String key) { this.hfnPushMessageKey = key; }

    public void HubMsgCommunicator(String title, String msg, List<String> userList, String userKey, String sendCd) {

        System.out.println("★thisServer:"+thisServer);
        String serverURL = hfnPushMessageUrl;
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("authKey", hfnPushMessageKey);
        map.add("title", title);
        map.add("msg", msg);
        map.add("msgViewYn", "Y");

        for(String user : userList) {
            map.add("user", user);
        }

        log.debug("@@ parameter check >> " + map.toString());

        try {
            String result = "";
            if(thisServer.equals("local")) {
                result = "this is local test";
            } else {
                log.info("@@ url = " + serverURL);
                log.info("@@ 메시지 허브 송신 시작");
                result = restTemplate.postForObject(serverURL, map, String.class);
                log.info("@@ 메시지 허브 송신결과 >> " + result);

                // 통신 응답값(Json String)을 map으로 parsing
                HashMap<String, Object> resultMap = new ObjectMapper().readValue(result, HashMap.class);
                String code = (String)resultMap.get("CODE");

                // DB에 메시지허브 발송이력 삽입
                SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
                sndCertMgntRequest.setUserKey(userKey);
                sndCertMgntRequest.setSendCd(sendCd);
                sndCertMgntRequest.setSendCtnt(map.toString());
                sndCertMgntRequest.setResultCd(code);
                settingRepository.insertHubMsgData(sndCertMgntRequest);
            }
        }catch(Exception e) {
            log.error(e.toString());
        }
    }
}
