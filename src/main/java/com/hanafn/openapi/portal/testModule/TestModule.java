package com.hanafn.openapi.portal.testModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hanafn.openapi.portal.exception.ServerException;
import com.hanafn.openapi.portal.file.FileService;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.views.dto.UseorgRequest;
import com.hanafn.openapi.portal.views.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/test-module")
@CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
public class TestModule {
    private static String gwServerUrl = "http://10.167.36.219:28688";
    private static String oauthGrantType = "client_credentials";
    private static String oauthScope = "default";
    private static String URI_TOKEN = "/api/oauth/oauth/token";

    @Autowired
    SettingService settingService;

    @Autowired
    FileService fileService;

    @PostMapping("/useorgDownload")
    public ResponseEntity<Resource> useorgDownload(HttpServletRequest httpServletRequest, HttpServletResponse response, @RequestParam String userKey) throws FileNotFoundException {
        UseorgRequest.UseorgDetailRequest request = new UseorgRequest.UseorgDetailRequest();
        request.setUserKey(userKey);
        String fileName = settingService.selectUseorg(request).getUseorgUpload();
        Resource resource = fileService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = httpServletRequest.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.debug("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * HTML단으로 부터의 요청을 받아 API 로직을 수행하는 컨트롤러
     * @param paramMap(요청 PARAMETER)
     * @return
     */

    @RequestMapping(value="/capital/contract", produces="application/json;charset=utf8")
    public ResponseEntity<?> capitalContract(@RequestBody Map<String, Object> paramMap, HttpServletRequest request) throws Exception{
        String clientId = "a7c77724-e0a3-481e-b36c-e59f6f8a4bc7";
        String entrCd = "HUK7020022";
        String encryptedEncKey = "rRF9tH2LWGwX0a5OpsVa+sQrZ/ztEyqdcQEYUK8hJus=";
        String encClientSecret = "Hc9BMrQEGPjXDKG5hOdeQ6tElc6gLWMBSCeiMbf7sT5a5/+qoWEuBJmWobzBcfTc";
        String clientSecret = "";
        log.debug("★encryptedEncKey : "+ encryptedEncKey);

        String encKey = "";
        try {
            encKey = AES256Util.decrypt(encryptedEncKey);
            clientSecret = AES256Util.decrypt(encKey, encClientSecret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("★encKey:"+encKey);

        String accessToken = getAceessToken(clientId, clientSecret, entrCd, encKey);

        Map<String, Object> params = new HashMap<>();

        Map<String, Object> dataHeader = new HashMap<>();
        dataHeader.put("CNTY_CD","KR");
        dataHeader.put("CLNT_IP_ADDR","");
        dataHeader.put("ENTR_CD",entrCd);

        params.put("dataHeader", dataHeader);
        params.put("dataBody", paramMap);
//        params.put("decryptedEnckey",clientInfo.getDecryptedEnckey());
        params.put("ENTR_CD",entrCd);

        // client id set
        params.put("client_id", clientId);

        // 복호화된 accesstoken 넘겨준다.
        String jsonStr  = communicateGateway("/api/api-dev/v1/contract/info", HttpMethod.POST, clientId, accessToken, encKey, params);
        Gson gson = new Gson();
        Map<String, Object> resMap = gson.fromJson(jsonStr, Map.class);
        return  ResponseEntity.ok(resMap);
    }

    @PostMapping(value="/life/contract", produces="application/json;charset=utf8")
    public ResponseEntity<?> lifeContract(@RequestBody HashMap<String,String> paramMap, HttpServletRequest request) throws Exception{
        String clientId = "afd24492-2081-4e2e-9fc8-1e3bfb563301";
        String entrCd = "HUK7020022";
        String encryptedEncKey = "rRF9tH2LWGwX0a5OpsVa+sQrZ/ztEyqdcQEYUK8hJus=";
        String encClientSecret = "LbKQKCdCyObZhbHyVi4WVBlIZthfxADnaEvW1TuNhZTiVnHUcuSEvAp1tz1dTfgD";
        String clientSecret = "";
        log.debug("★encryptedEncKey : "+ encryptedEncKey);

        String encKey = "";
        try {
            encKey = AES256Util.decrypt(encryptedEncKey);
            clientSecret = AES256Util.decrypt(encKey, encClientSecret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("★encKey:"+encKey);

        String accessToken = getAceessToken(clientId, clientSecret, entrCd, encKey);

        Map<String, Object> params = new HashMap<>();

        Map<String, Object> dataHeader = new HashMap<>();
        dataHeader.put("CNTY_CD","KR");
        dataHeader.put("CLNT_IP_ADDR","");
        dataHeader.put("ENTR_CD",entrCd);

        params.put("dataHeader", dataHeader);
        params.put("dataBody", paramMap);
//        params.put("decryptedEnckey",clientInfo.getDecryptedEnckey());
        params.put("ENTR_CD",entrCd);

        // client id set
        params.put("client_id", clientId);

        // 복호화된 accesstoken 넘겨준다.
        String jsonStr  = communicateGateway("/api/api-dev/v1/checkBgi", HttpMethod.POST, clientId, accessToken, encKey, params);
        return ResponseEntity.ok(jsonStr);
    }

    @PostMapping(value="/hbk/fulltext", produces="application/json;charset=utf8")
    public ResponseEntity<?> fulltext(@RequestBody HashMap<String,String> paramMap, HttpServletRequest request) throws Exception{

        String jsonStr  = communicateGateway("/test/fullMessage", HttpMethod.POST, paramMap);
        return ResponseEntity.ok(jsonStr);
    }

    @PostMapping(value="/doEncrypt", produces="application/json;charset=utf8")
    public ResponseEntity<?> doEncrypt(@RequestBody HashMap<String,String> paramMap, HttpServletRequest request) throws Exception{
        String rtn = "";
        try {
            String gtype = paramMap.get("gtype");
            String encdec = paramMap.get("encdec");
            if (encdec.equals("enc")) {
                if (gtype.equals("card"))
                    rtn = CryptoUtil.encryptAES256_hexKey(paramMap.get("key"), paramMap.get("inputText"), "euc-kr");
                else if (gtype.equals("members"))
                    rtn = CryptoUtil.encryptAES256(paramMap.get("key"), paramMap.get("iv"), paramMap.get("inputText"), "euc-kr");
                else if (gtype.equals("bank"))
                    rtn = CryptoUtil.encryptAES256_bank(paramMap.get("key"), paramMap.get("inputText"), "euc-kr");
                else
                    rtn = CryptoUtil.encryptAES256(paramMap.get("key"), paramMap.get("inputText"), "euc-kr");
            } else {
                if (gtype.equals("card"))
                    rtn = CryptoUtil.decryptAES256_hexKey(paramMap.get("key"), paramMap.get("inputText"), "euc-kr");
                else if (gtype.equals("members"))
                    rtn = CryptoUtil.decryptAES256(paramMap.get("key"), paramMap.get("iv"), paramMap.get("inputText"), "euc-kr");
                else if (gtype.equals("bank"))
                    rtn = CryptoUtil.decryptAES256_bank(paramMap.get("key"), paramMap.get("inputText"), "euc-kr");
                else
                    rtn = CryptoUtil.decryptAES256(paramMap.get("key"), paramMap.get("inputText"), "euc-kr");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("★rtn:"+rtn);

        Map<String, Object> response = new HashMap<>();

        response.put("response", rtn);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/card/paycheck-test")
//    public ResponseEntity<?> paycheckTest(@RequestParam HashMap<String,String> paramMap) {
//        String serverUrl = gwServerUrl + URI_TOKEN;
//        String jsonMessage = "";
//        HttpHeaders httpHeaders = new HttpHeaders();
//
//        // accessToken 발급받기 위하여 clientId, Secret을 세팅하고, 이것을 이용하여 헤더의 Authorization에 세팅한다.
//        String StringClient = "3688060b-1117-4cf3-8b94-9028bdc0ff83:5c48a96a28fed9f97270dee1ee9ecfbf";  // clientid:clientsecret
//        byte[] encodedClient = Base64.encodeBase64(StringClient.getBytes());
//        String auth = "Basic " + new String(encodedClient);
//        httpHeaders.add("Content-Type","application/x-www-form-urlencoded");
//        httpHeaders.add("Authorization", auth);
//
//        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
//        bodyMap.add("grant_type", oauthGrantType);
//        bodyMap.add("scope", oauthScope);
//        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(bodyMap, httpHeaders);
//
//        // GW를 통하여 Auth 서버와 통신을 한 후, API 통신을 위한 AccessToken을 발급받아온다.
//        GWResponse gwResponse = communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
//        String accessToken= (String)gwResponse.getDataBody().get("access_token");
//
//        // 원하는 API 서비스의 주소(GW/API서버/서비스URL)와 GW통신을 위한 데이터들을 세팅한다.
//        String targetUrl = "/api/api-test/card/paycheck";
//        Map<String, Object> params = new HashMap<>();
//
//        Map<String, Object> dataHeader = new HashMap<>();
//        dataHeader.put("ENTR_CD", "HUK7020022");
//        dataHeader.put("CNTY_CD","kr");
//        dataHeader.put("CLNT_IP_ADDR","");
//
//        params.put("dataHeader", dataHeader);
//        params.put("dataBody", paramMap);
//
//        // GW를 통하여 API 서버와 통신을 한다.
//        return ResponseEntity.ok(communicateGateway(targetUrl, HttpMethod.POST, clientId, accessToken, params));
//    }

    public String getAceessToken(String clientId, String clientSecret, String entrCd, String encKey) throws Exception {
        String serverUrl = gwServerUrl + URI_TOKEN;

        // header 생성
        HttpHeaders headers = new HttpHeaders();

        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

        //headers.setAccept(acceptableMediaTypes);
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        Date currentDate = new Date();
        long unixTime = currentDate.getTime() / 1000;

        String StringClient = clientId + ":" + clientSecret + ":" + unixTime;
        String encodedClient = null;
        try {
            encodedClient = AES256Util.encrypt(encKey,StringClient);
        } catch ( Exception e) {
            log.debug("UnsuportedEncoding : [" + StringClient + "]");
            e.printStackTrace();
            return "";
        }
        String auth = "Basic " + encodedClient;
        headers.add("Authorization", auth);
        headers.add("ENTR_CD",entrCd);

        // entity 생성
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("grant_type", oauthGrantType);
        bodyMap.add("scope", oauthScope);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(bodyMap, headers);

        String jsonMessage = communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
        Gson gson = new Gson();
        Map<String, Object> resMap = gson.fromJson(jsonMessage, Map.class);

        String fullTokenResponse = resMap.containsKey("dataBody") ? (String)((Map)resMap.get("dataBody")).get("access_token") : "";
        String accessToken = AES256Util.decrypt(encKey, fullTokenResponse).split(":")[0];

        return accessToken;
    }

    private String communicateGateway(String url, HttpMethod method, String clientId, String accessToken, String encKey, Map<String, Object> params) throws ServerException {
        String serverUrl = gwServerUrl + url;

        Date currentDate = new Date();
        log.debug("currentDate.getTime()="+currentDate.getTime());
        long unixTime = currentDate.getTime() +15000 / 1000;


        String Authorization = accessToken + ":" + unixTime + ":" + clientId;
        String encryptedAccessToken = null;
        try {
            encryptedAccessToken = AES256Util.encrypt(encKey,Authorization);
        } catch (Exception e) {
            log.debug("★communicateGateway accesstoken 암호화 세팅에러");
            throw new ServerException("★communicateGateway accesstoken 암호화 세팅에러");
        }

        // header 생성
        HttpHeaders headers = new HttpHeaders();

        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

        headers.setAccept(acceptableMediaTypes);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);

        Map<String, Object> dataHeader = (Map<String, Object>) params.get("dataHeader");

        headers.set("Authorization", "bearer " + encryptedAccessToken);
        headers.set("ENTR_CD", (String) params.get("ENTR_CD"));

        // entity 생성
        HttpEntity<String> httpEntity = null;
        try{
            httpEntity = new HttpEntity<String>(new ObjectMapper().writeValueAsString(params), headers);
            log.debug("★inner body:"+ new ObjectMapper().writeValueAsString(params));
        }catch(JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new ServerException("Request Parameter Processing Error");
        }

        return communicateServer(serverUrl, method, httpEntity, false);
    }

    private String communicateGateway(String url, HttpMethod method, Map<String, String> params) throws ServerException {
        String serverUrl = gwServerUrl + url;

        Date currentDate = new Date();
        log.debug("currentDate.getTime()="+currentDate.getTime());
        long unixTime = currentDate.getTime() +15000 / 1000;

        // header 생성
        HttpHeaders headers = new HttpHeaders();

        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);

        headers.setAccept(acceptableMediaTypes);
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);

//        headers.set("req_message", params.get("req_message"));

        // entity 생성
        HttpEntity<String> httpEntity = null;
        try{
            httpEntity = new HttpEntity<String>(new ObjectMapper().writeValueAsString(params), headers);
            log.debug("★inner body:"+ new ObjectMapper().writeValueAsString(params));
        }catch(JsonProcessingException e) {
            log.error("Request Parameter Processing Error", e);
            throw new ServerException("Request Parameter Processing Error");
        }

        return communicateServer(serverUrl, method, httpEntity, false);
    }

    private String communicateServer(String serverUrl, HttpMethod method, HttpEntity httpEntity, boolean procError) throws ServerException {

        String jsonMessage = "";
        try {
            log.debug("메시지를 송신합니다. " + serverUrl);
            log.debug("Http Header : " + httpEntity.getHeaders());
            log.debug("Http Body : " + httpEntity.getBody());

            RestTemplate restTemplate = new RestTemplate();
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
            throw new ServerException("Server communication error");
        }

        log.debug("메시지를 수신합니다. " + jsonMessage.toString());

        return jsonMessage;
    }
}
