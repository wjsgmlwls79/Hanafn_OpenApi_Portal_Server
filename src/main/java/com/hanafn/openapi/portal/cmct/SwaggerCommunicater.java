package com.hanafn.openapi.portal.cmct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hanafn.openapi.portal.cmct.dto.SwaggerResponse;
import com.hanafn.openapi.portal.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SwaggerCommunicater {

    private static MessageSourceAccessor messageSource;
    private static String gwServerUrl;

    private static String URI_SWAGGER = "/v2/api-docs";

    @Autowired
    public SwaggerCommunicater(MessageSourceAccessor messageSource) {
        this.messageSource = messageSource;
    }

    @Value("${gw.oauth.url}")
    public void setGwServerUrl (String url) {
        this.gwServerUrl = url;
    }

    /**
     * Swagger 정보를 생성한다.
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static SwaggerResponse swaggerInfo(String servicesUrl) throws BusinessException {
        String serverUrl = gwServerUrl + "/api/" + servicesUrl + URI_SWAGGER;

        // header 생성
        HttpHeaders headers = new HttpHeaders();

        Map<String, Object> parameters = new HashMap<String, Object>();
        HttpEntity<String> httpEntity = null;

        try {
            httpEntity = new HttpEntity<>(new ObjectMapper().writeValueAsString(parameters), headers);
        }catch(JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new BusinessException(messageSource.getMessage("E098"));
        }

        return communicateServer(serverUrl, HttpMethod.GET, httpEntity, true);

    }

    /**
     * 서버와 통신을 처리한다.
     * @param serverUrl
     * @param method
     * @param httpEntity
     * @return 응답메시지
     * @throws BusinessException
     */
    private static SwaggerResponse communicateServer(String serverUrl, HttpMethod method, HttpEntity httpEntity, boolean procError) throws BusinessException {

        String jsonMessage = "";
        try {
            log.info("메시지를 송신합니다. " + serverUrl);
            log.info("Http Header : " + httpEntity.getHeaders());
            log.info("Http Body : " + httpEntity.getBody());

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<String> responseEntity = restTemplate.exchange(serverUrl, method, httpEntity, String.class);

            jsonMessage = responseEntity.getBody();
        }
        catch(HttpServerErrorException hsee) {
            log.error("Server communication error. http error : [" + hsee.getMessage() + "]", hsee);
            jsonMessage = hsee.getResponseBodyAsString();
            throw new BusinessException(messageSource.getMessage("E018"));
        }
        catch(HttpClientErrorException hcee) {
            log.error("Server communication error. http error : [" + hcee.getMessage() + "]", hcee);
            jsonMessage = hcee.getResponseBodyAsString();
            throw new BusinessException(messageSource.getMessage("E018"));
        }
        catch(Exception e) {
            log.error("Server communication error", e);
            throw new BusinessException(messageSource.getMessage("E098"));
        }

        log.info("메시지를 수신합니다. " + jsonMessage.toString());

        Gson gson = new Gson();
        Map<String, Object> resMap = gson.fromJson(jsonMessage, Map.class);
        String swagger = "";
        Map<String, Object> info = new HashMap<>();
        String host = "";
        String basePath = "";
        List<Map<String, Object>> tags = new ArrayList<>();
        Map<String, Object> paths = new HashMap<>();
        Map<String, Object> definitions = new HashMap<>();

        swagger = (String)resMap.get("swagger");
        host = (String)resMap.get("host");
        basePath = (String)resMap.get("basePath");
        if(resMap.get("tags") != null && resMap.get("tags") instanceof List) {
            tags = (List<Map<String, Object>>) resMap.get("tags");
        }
        if(resMap.get("info") != null && resMap.get("info") instanceof Map) {
            info = (Map<String, Object>)resMap.get("info");
        }
        if(resMap.get("paths") != null && resMap.get("paths") instanceof Map) {
            paths = (Map<String, Object>)resMap.get("paths");
        }
        if(resMap.get("definitions") != null && resMap.get("definitions") instanceof Map) {
            definitions = (Map<String, Object>)resMap.get("definitions");
        }

        SwaggerResponse swaggerResponse = new SwaggerResponse();
        swaggerResponse.setSwagger(swagger);
        swaggerResponse.setInfo(info);
        swaggerResponse.setHost(host);
        swaggerResponse.setBasePath(basePath);
        swaggerResponse.setTags(tags);
        swaggerResponse.setPaths(paths);
        swaggerResponse.setDefinitions(definitions);

        return swaggerResponse;
    }
}