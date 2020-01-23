package com.hanafn.openapi.portal.cmct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.views.dto.HfnEnum;
import com.hanafn.openapi.portal.cmct.dto.GWResponse;
import com.hanafn.openapi.portal.cmct.dto.OAuthRequest;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.Charset;
import java.util.*;

@Component
@Slf4j
public class GWCommunicater {

    private static MessageSourceAccessor messageSource;
    private static RestTemplate restTemplate;
    private static String gwServerUrl;
    private static String gwInboundUrl;
    private static String gwFileUrl;
    private static String gwOutboundUrl;
    private static String oauthGrantType;
    private static String oauthScope;
    private static String URI_CLIENT = "/api/oauth/oauth/api/client";
    private static String URI_CLIENT_EXPIRE = "/api/oauth/oauth/api/client/expireDate";
    private static String URI_CLIENT_SECRET_NEW_PASSWORD = "/api/oauth/oauth/api/client/newPassword";
    private static String URI_TOKEN = "/api/oauth/oauth/token";

    private static final Logger logger = LoggerFactory.getLogger(GWCommunicater.class);

    @Autowired
    CommonUtil commonUtil;

    @Autowired
    public GWCommunicater(MessageSourceAccessor messageSource, RestTemplate restTemplate) {
        this.messageSource = messageSource;
        this.restTemplate=restTemplate;
    }

    @Value("${gw.oauth.url}")
    public void setGwServerUrl (String url) {
        this.gwServerUrl = url;
    }

    @Value("${gw.inbound.url}")
    public void setGwInboundUrl (String url) { this.gwInboundUrl = url; }

    @Value("${gw.file.url}")
    public void setGwFileUrl (String url) { this.gwFileUrl = url; }

    @Value("${gw.outbound.url}")
    public void setGwOutboundUrl (String url) {
        this.gwOutboundUrl = url;
    }

    @Value("${oauth.granttype}")
    public void setOauthGrantType (String grantType) {
        this.oauthGrantType = grantType;
    }

    @Value("${oauth.scope}")
    public void setOauthScope (String scope) {
        this.oauthScope = scope;
    }

    /**
     * Cilent 정보를 생성한다.
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static GWResponse createClientInfo(String hfnCd, String expireDate, String entrCd) throws BusinessException {

        hfnCd = HfnEnum.resolve(hfnCd).getCode();
        String serverUrl = gwServerUrl + hfnCd + URI_CLIENT;

        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));

        // header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(mediaType);
        headers.set("ENTR_CD", entrCd);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("authorizedGrantTypes", oauthGrantType);
        parameters.put("scope", oauthScope);
        parameters.put("expireDate", expireDate);

        HttpEntity<String> httpEntity = null;

        try {
            httpEntity = new HttpEntity<>(new ObjectMapper().writeValueAsString(parameters), headers);
        }catch(JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new BusinessException(messageSource.getMessage("E098"));
        }

        headers.setContentLength(httpEntity.getBody().length());

        return communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
    }

    /**
     * Cilent 유효기간 변경
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static GWResponse createClientExpire(String hfnCd, String clientId, String expireDate, String entrCd) throws BusinessException {

        hfnCd = HfnEnum.resolve(hfnCd).getCode();
        String serverUrl = gwServerUrl + hfnCd + URI_CLIENT_EXPIRE;

        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));

        // header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(mediaType);
        headers.set("ENTR_CD", entrCd);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("clientId", clientId);
        parameters.put("expireDate", expireDate);

        HttpEntity<String> httpEntity = null;

        try {
            httpEntity = new HttpEntity<>(new ObjectMapper().writeValueAsString(parameters), headers);
        }catch(JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new BusinessException(messageSource.getMessage("E098"));
        }

        return communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
    }

    /**
     * Cilent ID, Secret 재발급
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static GWResponse clientSecretNewPassword(String hfnCd, String clientId, String expireDate, String entrCd, String encKey) throws BusinessException {

        hfnCd = HfnEnum.resolve(hfnCd).getCode();
        String serverUrl = gwServerUrl + hfnCd + URI_CLIENT_SECRET_NEW_PASSWORD;

        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));

        // header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(mediaType);
        headers.set("ENTR_CD", entrCd);

        // unixtime 추가
        Date currentDate = new Date();
        long unixTime = currentDate.getTime() / 1000;
        clientId = clientId + ":" + unixTime;

        try {
            clientId = AES256Util.encrypt(encKey, clientId);
        } catch ( Exception e ) {
            log.error(e.getMessage());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("clientId", clientId);
        parameters.put("expireDate", expireDate);
        HttpEntity<String> httpEntity = null;

        try {
            httpEntity = new HttpEntity<>(new ObjectMapper().writeValueAsString(parameters), headers);
        }catch(JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new BusinessException("E098",messageSource.getMessage("E098"));
        }

        return communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
    }

    /**
     * Access Token을 생성하여 복호화 한후, GWResponse Body에 담는다.
     * @Request : String StringClient = clientRequest.getClientId() + ":" + clientRequest.getClientSecret() + ":" + unixTime;
     * @Response :
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static GWResponse getAceessToken(OAuthRequest.ClientInfo clientRequest) throws BusinessException {

        String hfnCd = clientRequest.getHfnCd();
        if(StringUtils.isNotBlank(clientRequest.getHfnCd())) {
            hfnCd = HfnEnum.resolve(clientRequest.getHfnCd()).getCode();
        }
        String serverUrl = gwServerUrl + hfnCd + URI_TOKEN;

        String decryptedEncKey = clientRequest.getDecryptedEnckey();
        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

        // header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptableMediaTypes);
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        Date currentDate = new Date();
        long unixTime = currentDate.getTime() / 1000;

        String StringClient = clientRequest.getClientId() + ":" + clientRequest.getClientSecret() + ":" + unixTime;

        log.debug("★StringClient:"+StringClient);
        String encodedClient = null;
        try {
            encodedClient = AES256Util.encrypt(decryptedEncKey,StringClient);
        } catch ( Exception e) {
            log.error("UnsuportedEncoding : [" + StringClient + "]");
            throw new BusinessException("E098",messageSource.getMessage("E098"));
        }
        String auth = "Basic " + encodedClient;
        headers.add("Authorization", auth);
        headers.add("ENTR_CD",clientRequest.getEntrCd());

        // entity 생성
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("grant_type", oauthGrantType);
        bodyMap.add("scope",oauthScope);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(bodyMap, headers);

        GWResponse accessTokenResponse = communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
        log.debug("★initial accessTokenResponse:" + accessTokenResponse);
        String fullTokenResponse = (String)accessTokenResponse.getDataBody().get("access_token");
        log.debug("★accessTokenResponse["+accessTokenResponse);
        String accessToken = fullTokenResponse.split(":")[0];
        log.debug("★accessToken["+accessToken);

        accessTokenResponse.getDataBody().put("access_token", (String) accessToken);
        log.debug("★final accessTokenResponse:" + accessTokenResponse);
        return accessTokenResponse;
    }

    /**
     * GateWay와 통신한다.
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static GWResponse communicateGateway(String gwType, String url, HttpMethod method, String accessToken, Map<String, Object> params) throws BusinessException {

        String serverUrl = gwInboundUrl + url;
        if (gwType.equals("onefgw")) serverUrl = gwFileUrl + url;

        String decryptedEnckey = (String)params.get("decryptedEnckey");
        log.debug("★cmGWDecEnckey:"+decryptedEnckey);
        Date currentDate = new Date();
        long unixTime = currentDate.getTime() / 1000;

        String decryptedAccessToken = null;
        try {
            decryptedAccessToken = AES256Util.decrypt(decryptedEnckey, accessToken);
            log.debug("★decryptedAccessToken : " + decryptedAccessToken);
        } catch ( Exception e ) {
            log.error(" accessToken 복호화 오류 : [ " + accessToken + " ]" + "[ " + decryptedEnckey + " ]");
            throw new BusinessException("E100",messageSource.getMessage("E100"));
        }
        log.debug("★decryptedAT : "+decryptedAccessToken);

        String Authorization = decryptedAccessToken + ":" + (String)params.get("client_id");
        log.debug("★unixtime (CG):" + unixTime);
        log.debug("★Authorization : "+Authorization);
        String encryptedAccessToken = null;
        try {
            encryptedAccessToken = AES256Util.encrypt(decryptedEnckey,Authorization);
        } catch (Exception e) {
            log.error("communicateGateway accesstoken 암호화 세팅에러 : " + Authorization + " / " + decryptedEnckey);
            throw new BusinessException("E100",messageSource.getMessage("E100"));
        }

        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));

        // header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(mediaType);
        headers.set("Authorization", "bearer " + encryptedAccessToken);
        headers.set("ENTR_CD", (String) params.get("ENTR_CD"));
        headers.set("APP_KEY", (String) params.get("APP_KEY"));

        params.remove("APP_KEY");
        params.remove("decryptedEnckey");
        params.remove("client_id");

        // entity 생성
        HttpEntity<String> httpEntity = null;
        try{
            httpEntity = new HttpEntity<String>(new ObjectMapper().writeValueAsString(params), headers);
            log.info("★inner body:"+ new ObjectMapper().writeValueAsString(params));
        }catch(JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new BusinessException("E100",messageSource.getMessage("E100"));
        }

        return communicateServer(serverUrl, method, httpEntity, false);
    }

    /**
     * 서버와 통신을 처리한다.
     * @param serverUrl
     * @param method
     * @param httpEntity
     * @return 응답메시지
     * @throws BusinessException
     */
    private static GWResponse communicateServer(String serverUrl, HttpMethod method, HttpEntity httpEntity, boolean procError) throws BusinessException {

        String jsonMessage = "";
        try {
            log.info("메시지를 송신합니다. " + serverUrl);
            log.info("Http Header : " + httpEntity.getHeaders());
            log.info("Http Body : " + httpEntity.getBody());

            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<String> responseEntity = restTemplate.exchange(serverUrl, method, httpEntity, String.class);
            jsonMessage = responseEntity.getBody();
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
            throw new BusinessException("E101",messageSource.getMessage("E101"));
        }

        log.info("메시지를 수신합니다. " + jsonMessage.toString());

        Gson gson = new Gson();
        Map<String, Object> resMap = gson.fromJson(jsonMessage, Map.class);
        Map<String, Object> headerMap = (Map)resMap.get("dataHeader");
        Map<String, Object> bodyMap = new HashMap<>();

        if(resMap.get("dataBody") != null && resMap.get("dataBody") instanceof Map) {
            bodyMap = (Map<String, Object>)resMap.get("dataBody");
        }

        GWResponse gwResponse = new GWResponse();
        gwResponse.setDataHeader(headerMap);
        gwResponse.setDataBody(bodyMap);

        if(procError) {
            String gwResCd = (String)headerMap.get("GW_RSLT_CD");

            Object obj = gwResponse.getDataBody().get("resultCode");
            double oauthResCd = 0;

            if(obj != null) {
                oauthResCd = (double)gwResponse.getDataBody().get("resultCode");
            }

            // 만일 응답코드가 200이 아니라면 오류 처리
            if(!StringUtils.equals(gwResCd, "1200") ) {
                log.error("G/W 서버와 통신결과 오류응답을 받았습니다.");
                throw new BusinessException("E098",messageSource.getMessage("E098", new String[]{gwResCd}));
            }

            if(oauthResCd != 0 && oauthResCd != 200) {
                log.error("OAuth 서버와 통신결과 오류응답을 받았습니다.");
                String[] params = new String[]{Integer.toString((int)oauthResCd)};
                throw new BusinessException("E098",messageSource.getMessage("E098", params));
            }
        }
        return gwResponse;
    }
}