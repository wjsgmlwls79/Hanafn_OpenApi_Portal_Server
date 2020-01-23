package com.hanafn.openapi.portal.cmct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
@Data
public class AccountStatusCommunicater {

    @Autowired
    MessageSourceAccessor messageSource;

    @Value("${hbk.server.url}")
    private String hbkServerUrl;

    @Value("${hnw.server.url}")
    private String hnwServerUrl;

    @Autowired
    SettingRepository settingRepository;

    private static RestTemplate restTemplate;
    public AccountStatusCommunicater (RestTemplate restTemplate){
        this.restTemplate=restTemplate;
    }

    // 하나은행 계좌검증
    public ResponseEntity<String> communicateServer(UserPrincipal currentUser, HttpServletRequest request, String searchAccountBankCd) {

        String serverUrl = hbkServerUrl + "/v1/accountStatus";

        Date date = new Date();

        SimpleDateFormat format = new SimpleDateFormat("MMdd");

        String todayDate = format.format(date);

        if (StringUtils.equals(currentUser.getUserType(), "ORGD")) {
            String entrCd = settingRepository.selectEntrCdFromUserKey(currentUser.getEntrCd());
            currentUser.setEntrCd(entrCd);
        }

        CommonUtil commonUtil = new CommonUtil();
        String ip = commonUtil.getIp(request);

        HashMap<String, Object> parameters = new HashMap<>();
        HashMap<String, String> dataHeader = new HashMap<>();
        HashMap<String, String> dataBody = new HashMap<>();

        dataHeader.put("CNTY_CD", "kr");
        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")) {
            if (currentUser.getEntrCd() != null) {
                dataHeader.put("ENTR_CD", currentUser.getEntrCd());
            } else {
                dataHeader.put("ENTR_CD", "HANATI0");
            }
        } else {
            dataHeader.put("ENTR_CD", "HANATI0");
        }
        if (ip != null) {
            dataHeader.put("CLNT_IP_ADDR", ip);
        } else {
            dataHeader.put("CLNT_IP_ADDR", "127.0.0.1");
        }

        dataBody.put("C_ENC_YN","Y"); // 필수
        dataBody.put("C_APP_ID", "");
        dataBody.put("TR_DATE", todayDate);
        dataBody.put("SEARCH_ACCOUNT_BANK_CD", "081");
        dataBody.put("SEARCH_ACCOUNT_NO", searchAccountBankCd);
        dataBody.put("SEARCH_ACCOUNT_SSNO", "");
        dataBody.put("REQ_AMOUNT", "");
        dataBody.put("ACCOUNT_ACT_YN", "1");
        dataBody.put("ACC_CD", "");
        dataBody.put("DATA_FILLER", "");

        parameters.put("dataHeader", dataHeader);
        parameters.put("dataBody", dataBody);

        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")) {
            headers.set("ENTR_CD", currentUser.getEntrCd());
        } else {
            headers.set("ENTR_CD", "");
        }
        headers.setAccept(acceptableMediaTypes);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);

        HttpEntity<String> httpEntity = null;
        try {
            httpEntity = new HttpEntity<String>(new ObjectMapper().writeValueAsString(parameters), headers);
        } catch (JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new BusinessException("E099", messageSource.getMessage("E099"));
        }

        headers.setContentLength(httpEntity.getBody().length());

        log.info("-------- API 통신 시작: {}", serverUrl);
        log.info("Http Header : " + httpEntity.getHeaders());
        log.info("Http Body : " + httpEntity.getBody());

        ResponseEntity<String> responseEntity = null;

        try {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            responseEntity = restTemplate.postForEntity(serverUrl, httpEntity, String.class);

            log.info("responseEntity: {}", responseEntity);
            log.info("-------- API 통신 끝 -------- ");
        } catch (HttpServerErrorException hsee) {
            log.error("Server communication error. http error : [" + hsee.getMessage() + "]", hsee);
            throw new BusinessException("E099", messageSource.getMessage("E099"));
        } catch (HttpClientErrorException hcee) {
            log.error("Server communication error. http error : [" + hcee.getMessage() + "]", hcee);
            throw new BusinessException("E099", messageSource.getMessage("E099"));
        } catch (Exception e) {
            log.error("Server communication error", e);
            throw new BusinessException("E099", messageSource.getMessage("E099"));
        }
        return responseEntity;
    }

    // 하나금융투자 계좌상태 조회
    public ResponseEntity<String> communicateServerForHnw(UserPrincipal currentUser, HttpServletRequest request, String searchAccountBankCd) {

        String serverUrl = hnwServerUrl + "/v1/accountStatus";

        if (StringUtils.equals(currentUser.getUserType(), "ORGD")) {
            String entrCd = settingRepository.selectEntrCdFromUserKey(currentUser.getEntrCd());
            currentUser.setEntrCd(entrCd);
        }

        CommonUtil commonUtil = new CommonUtil();
        String ip = commonUtil.getIp(request);

        HashMap<String, Object> parameters = new HashMap<>();
        HashMap<String, String> dataHeader = new HashMap<>();
        dataHeader.put("CNTY_CD", "kr");

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")) {
            if (currentUser.getEntrCd() != null) {
                dataHeader.put("ENTR_CD", currentUser.getEntrCd());
            } else {
                dataHeader.put("ENTR_CD", "HANATI0");
            }
        } else {
            dataHeader.put("ENTR_CD", "HANATI0");
        }
        if (ip != null) {
            dataHeader.put("CLNT_IP_ADDR", ip);
        } else {
            dataHeader.put("CLNT_IP_ADDR", "127.0.0.1");
        }

        HashMap<String, String> dataBody = new HashMap<>();
        dataBody.put("C_ENC_YN","Y");
        dataBody.put("BNK_CD", "270");
        dataBody.put("FEE_ACCOUNT", searchAccountBankCd);

        parameters.put("dataHeader", dataHeader);
        parameters.put("dataBody", dataBody);

        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")) {
            headers.set("ENTR_CD", currentUser.getEntrCd());
        } else {
            headers.set("ENTR_CD", "");
            headers.set("ENTR_CD", "");
        }

        headers.setAccept(acceptableMediaTypes);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);

        HttpEntity<String> httpEntity = null;
        try {
            httpEntity = new HttpEntity<String>(new ObjectMapper().writeValueAsString(parameters), headers);
        } catch (JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new BusinessException("E099", messageSource.getMessage("E099"));
        }

        headers.setContentLength(httpEntity.getBody().length());

        log.info("-------- API 통신 시작: {}", serverUrl);
        log.info("Http Header : " + httpEntity.getHeaders());
        log.info("Http Body : " + httpEntity.getBody());

        ResponseEntity<String> responseEntity = null;

        try {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            responseEntity = restTemplate.postForEntity(serverUrl, httpEntity, String.class);

            log.info("responseEntity: {}", responseEntity);
            log.info("-------- API 통신 끝 -------- ");
        } catch (HttpServerErrorException hsee) {
            log.error("Server communication error. http error : [" + hsee.getMessage() + "]", hsee);
            throw new BusinessException("E099", messageSource.getMessage("E099"));
        } catch (HttpClientErrorException hcee) {
            log.error("Server communication error. http error : [" + hcee.getMessage() + "]", hcee);
            throw new BusinessException("E099", messageSource.getMessage("E099"));
        } catch (Exception e) {
            log.error("Server communication error", e);
            throw new BusinessException("E099", messageSource.getMessage("E099"));
        }
        return responseEntity;
    }
}