package com.hanafn.openapi.portal.cmct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.views.dto.CommonApiResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Data
public class NoticeServerCommunicater implements CommunicatorInterface {

    @Autowired
    private static MessageSourceAccessor messageSource;

    private static RestTemplate restTemplate;
    public NoticeServerCommunicater (RestTemplate restTemplate){ this.restTemplate=restTemplate;}

    /**
     * Notice 서버와 통신을 처리한다.
     * @param url
     * @param method
     * @param data
     * @return 응답메시지
     * @throws BusinessException
     */

    @Override
    public CommonApiResponse communicateServer(String url, HttpMethod method, Object data, boolean procError) {
        String serverUrl = url;

        CommonApiResponse commonApiResponse = new CommonApiResponse();

        HttpHeaders headers = new HttpHeaders();

        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

        headers.setAccept(acceptableMediaTypes);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);

        String appKey = "";
        try {
            HashMap<String, Object> appApiNoticeData = (HashMap<String, Object>) data;
            HashMap<String, Object> dataHeader = (HashMap<String, Object>) appApiNoticeData.get("dataHeader");
            log.info("communicate server dataHeader: " + dataHeader.keySet());
            appKey = (String) dataHeader.get("APP_KEY");
        } catch (Exception e) {
            log.error("HttpHeader - Appkey 세팅 문제 발생", e);
            throw new BusinessException("AE01",messageSource.getMessage("AE01"));
        }
        headers.set("APP_KEY",appKey);

        // entity 생성
        HttpEntity<String> httpEntity = null;
        try {
            httpEntity = new HttpEntity<String>(new ObjectMapper().writeValueAsString(data), headers);
            log.info("★body:" + new ObjectMapper().writeValueAsString(data));
        } catch (JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new BusinessException("E098",messageSource.getMessage("E098"));
        }

        headers.setContentLength(httpEntity.getBody().length());
        commonApiResponse.setRequestEntity(httpEntity);

        String jsonMessage = "";

        log.info("-------- API 통신 시작: {}", serverUrl);
        log.info("Http Header : " + httpEntity.getHeaders());
        log.info("Http Body : " + httpEntity.getBody());

        try {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<String> responseEntity = restTemplate.exchange(serverUrl, method, httpEntity, String.class);

            commonApiResponse.setApiResultStatCd(responseEntity.getStatusCode());
            commonApiResponse.setRequestApiUrl(serverUrl);
            commonApiResponse.setResponseEntity(responseEntity);
            jsonMessage = responseEntity.getBody();

            log.info("responseEntity: {}", responseEntity);
            log.info("-------- API 통신 끝 -------- ");
        }
        catch(HttpServerErrorException hsee) {
            log.error("Server communication error. http error : [" + hsee.getMessage() + "]", hsee);
            jsonMessage = hsee.getResponseBodyAsString();
        }
        catch(HttpClientErrorException hcee) {
            log.error("Server communication error. http error : [" + hcee.getMessage() + "]", hcee);
            jsonMessage = hcee.getResponseBodyAsString();
        }
        catch(Exception e) {
            log.error("Server communication error", e);
            throw new BusinessException("E098",messageSource.getMessage("E098"));
        }

        /* 메시지 수신 */
        log.info("메시지를 수신합니다. " + jsonMessage.toString());

        Gson gson = new Gson();
        Map<String, Object> resMap = gson.fromJson(jsonMessage, Map.class);
        Map<String, Object> headerMap = (Map)resMap.get("dataHeader");
        Map<String, Object> bodyMap = new HashMap<>();

        if(resMap.get("dataBody") != null && resMap.get("dataBody") instanceof Map) {
            bodyMap = (Map<String, Object>)resMap.get("dataBody");
        }

        commonApiResponse.setResponseData(resMap);
        return commonApiResponse;
    }
}