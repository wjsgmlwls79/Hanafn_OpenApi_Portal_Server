package com.hanafn.openapi.portal.testModule;

import com.google.gson.Gson;
import com.hanafn.openapi.portal.cmct.GWCommunicater;
import com.hanafn.openapi.portal.cmct.dto.GWResponse;
import com.hanafn.openapi.portal.exception.ServerException;
import com.hanafn.openapi.portal.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestModuleC {
    private static MessageSourceAccessor messageSource;
    private static String gwServerUrl;
    private static String oauthGrantType;
    private static String oauthScope;

    private static String URI_TOKEN = "/api/oauth/oauth/token";

    @Autowired
    public TestModuleC(MessageSourceAccessor messageSource) {
        this.messageSource = messageSource;
    }

    @Value("${gw.server.url}")
    public void setGwServerUrl (String url) {
        this.gwServerUrl = url;
    }

    @Value("${oauth.granttype}")
    public void setOauthGrantType (String grantType) {
        this.oauthGrantType = grantType;
    }
    @Value("${oauth.scope}")
    public void setOauthScope (String scope) {
        this.oauthScope = scope;
    }

    private static final int BUFFER_SIZE = 4096;
    private String direcotryPath = "/myDirectory";
    private String fileName = "/myFile.txt";


    @GetMapping("/appKey")
    public void keyReturn (HttpServletRequest hrequest, HttpServletResponse response) throws Exception {

        //
        ServletContext context = hrequest.getServletContext();
        String appPath = context.getRealPath("");
        System.out.println("appPath = " + appPath);

        // construct the complete absolute path of the file

        String fullPath = appPath + direcotryPath;
        File dir = new File(fullPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        File targetFile = new File(fullPath + fileName);
        FileWriter fw = new FileWriter(targetFile, true);
        fw.write("새로운파일~");
        fw.flush();
        fw.close();

        FileInputStream inputStream = new FileInputStream(fullPath + fileName);

        // get MIME type of the file
        String mimeType = context.getMimeType(fullPath + fileName);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);

        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) targetFile.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                targetFile.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();
    }


    @GetMapping("/download")
    public void downloadFile(HttpServletRequest request,
                                             HttpServletResponse response) throws IOException{
// get absolute path of the application
        ServletContext context = request.getServletContext();
        String appPath = context.getRealPath("");
        System.out.println("appPath = " + appPath);

        // construct the complete absolute path of the file
        String fullPath = appPath + direcotryPath;
        File dir = new File(fullPath);

        if(!dir.exists()){
            dir.mkdirs();
        }
        File targetFile = new File(fullPath+fileName);
        FileWriter fw = new FileWriter(targetFile, true);
        fw.write("파일내용");
        fw.flush();
        fw.close();

        FileInputStream inputStream = new FileInputStream(fullPath+fileName);

        // get MIME type of the file
        String mimeType = context.getMimeType(fullPath+fileName);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);

        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) targetFile.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                targetFile.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();

//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "test.txt"+ "\"")
//                .body(file);
    }

    @GetMapping("/card/paycheckk")
    public GWResponse paycheckk(@RequestParam HashMap<String,String> paramMap) {
        int i = 0;
        for(String key : paramMap.keySet()){
            log.debug("key ["+ i++ + "] = " + key);
        }
        String serverUrl = gwServerUrl + URI_TOKEN;
        String jsonMessage = "";
        HttpHeaders httpHeaders = new HttpHeaders();

        String StringClient = "5cd87ea5-a82a-4ead-afc0-7628132fcb3c:5c48a96a28fed9f97270dee1ee9ecfbf";
        byte[] encodedClient = Base64.encodeBase64(StringClient.getBytes());
        String auth = "Basic " + new String(encodedClient);
        httpHeaders.add("Content-Type","application/x-www-form-urlencoded");
        httpHeaders.add("Authorization", auth);

        // entity 생성
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("grant_type", oauthGrantType);
        bodyMap.add("scope", oauthScope);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(bodyMap, httpHeaders);

        GWResponse gwResponse = communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
        String accessToken= (String)gwResponse.getDataBody().get("access_token");

        String targetUrl = "/api/api-test/card/paycheck";
        Map<String, Object> params = new HashMap<>();

        Map<String, Object> dataHeader = new HashMap<>();
        dataHeader.put("ENTR_CD", "S10293847");
        dataHeader.put("CNTY_CD","kr");
        dataHeader.put("CLNT_IP_ADDR","");

        params.put("dataHeader", dataHeader);
        params.put("dataBody", paramMap);
        params.put("ENTR_CD","S10293847");
        log.debug("★params:"+params);
//        return GWCommunicater.communicateGateway(targetUrl, HttpMethod.POST, accessToken, params);
        return null;
    }

    @GetMapping("/capital/contract")
    public GWResponse CapitalContract(@RequestParam HashMap<String,String> paramMap) {
        String clientSecret = "default";
        try {
            clientSecret = CryptoUtil.decryptAES256(new String("fXtB9chO3J0DPVaCjZHrR36FrShndt6EsTrq3/DlTEXH5aeXjz1mUa5SvmovzY5KaOnbX4sSWWKTCHRhLjzihgQTCOAIwEMCQt2/c+qHXtTltKTK"));
        } catch(Exception e){
            e.printStackTrace();
        }
        log.debug("★시크릿:["+clientSecret);
        int i = 0;
        for(String key : paramMap.keySet()){
            log.debug("key ["+ i++ + "] = " + key);
        }
        String serverUrl = gwServerUrl + URI_TOKEN;
        String jsonMessage = "";
        HttpHeaders httpHeaders = new HttpHeaders();

        String StringClient = "2812da84-dd73-42c3-9f4d-8208bc535da5:"+clientSecret;
        byte[] encodedClient = Base64.encodeBase64(StringClient.getBytes());
        String auth = "Basic " + new String(encodedClient);
        httpHeaders.add("Content-Type","application/x-www-form-urlencoded");
        httpHeaders.add("Authorization", auth);

        // entity 생성
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("grant_type", oauthGrantType);
        bodyMap.add("scope", oauthScope);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(bodyMap, httpHeaders);

        GWResponse gwResponse = communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
        String accessToken= (String)gwResponse.getDataBody().get("access_token");

        String targetUrl = "/api/api-test/hcp/v1/contract/info";
        Map<String, Object> params = new HashMap<>();

        Map<String, Object> dataHeader = new HashMap<>();
        dataHeader.put("ENTR_CD", "S10293847");
        dataHeader.put("CNTY_CD","kr");
        dataHeader.put("CLNT_IP_ADDR","");

        params.put("dataHeader", dataHeader);
        params.put("dataBody", paramMap);
//        return GWCommunicater.communicateGateway(targetUrl, HttpMethod.POST, accessToken, params);
        return null;
    }

    @GetMapping("/card/paycheck")
    public GWResponse paycheck(@RequestParam HashMap<String,String> paramMap) {
        int i = 0;
        for(String key : paramMap.keySet()){
            log.debug("key ["+ i++ + "] = " + key);
        }
        String serverUrl = gwServerUrl + URI_TOKEN;
        String jsonMessage = "";
        HttpHeaders httpHeaders = new HttpHeaders();

        String StringClient = "25c59879-c223-41a2-a470-a05f726bfdfc:1e632ada330e0eb9a05da69bce24c9b844b3eac";
        byte[] encodedClient = Base64.encodeBase64(StringClient.getBytes());
        String auth = "Basic " + new String(encodedClient);
        httpHeaders.add("Content-Type","application/x-www-form-urlencoded");
        httpHeaders.add("Authorization", auth);

        // entity 생성
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("grant_type", oauthGrantType);
        bodyMap.add("scope", oauthScope);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(bodyMap, httpHeaders);

        GWResponse gwResponse = communicateServer(serverUrl, HttpMethod.POST, httpEntity, true);
        String accessToken= (String)gwResponse.getDataBody().get("access_token");

        String targetUrl = "/api/api-test/card/paycheck";
        Map<String, Object> params = new HashMap<>();

        Map<String, Object> dataHeader = new HashMap<>();
        dataHeader.put("ENTR_CD", "S10293847");
        dataHeader.put("CNTY_CD","kr");
        dataHeader.put("CLNT_IP_ADDR","");

        params.put("dataHeader", dataHeader);
        params.put("dataBody", new HashMap<>());
//        return GWCommunicater.communicateGateway(targetUrl, HttpMethod.POST, accessToken, params);
        return null;
    }


    /**
     * 서버와 통신을 처리한다.
     * @param serverUrl
     * @param method
     * @param httpEntity
     * @return 응답메시지
     * @throws ServerException
     */
    private static GWResponse communicateServer(String serverUrl, HttpMethod method, HttpEntity httpEntity, boolean procError) throws ServerException {

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
            throw new ServerException(messageSource.getMessage("E098"));
        }

        log.debug("메시지를 수신합니다. " + jsonMessage.toString());

        Gson gson = new Gson();
        Map<String, Object> resMap = gson.fromJson(jsonMessage, Map.class);
        Map<String, Object> headerMap = (Map)resMap.get("dataHeader");
        Map<String, Object> bodyMap = (Map)resMap.get("dataBody");
////        Map<String, Object> bodyMap = new HashMap<>();
//
//        if(resMap.get("dataBody") != null && resMap.get("dataBody") instanceof Map) {
//            bodyMap = (Map<String, Object>)resMap.get("dataBody");
//        }


        GWResponse gwResponse = new GWResponse();
        gwResponse.setDataHeader(headerMap);
        gwResponse.setDataBody(bodyMap);

        log.debug("★GWResponse:"+gwResponse);
        log.debug("여기까지 끝");

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
                throw new ServerException(messageSource.getMessage("E098", new String[]{gwResCd}));
            }

            if(oauthResCd != 0 && oauthResCd != 200) {
                log.error("OAuth 서버와 통신결과 오류응답을 받았습니다.");
                String[] params = new String[]{Integer.toString((int)oauthResCd)};
                throw new ServerException(messageSource.getMessage("E098", params));
            }
        }
        return gwResponse;
    }

    @PostMapping("/noticeToHfnTest")
    public ResponseEntity<?> noticeToUseorg(@RequestBody Map<String, String> param) {
        System.out.println("*********** post service-b/msgconvert");
        log.debug("*********** post service-b/msgconvert");
        log.debug(param.keySet().toString());
        System.out.println("★noticeToHfnTest:"+param.keySet().toString());

        // 받은 param으로 맵 xml 생성하여 관계사에 던져준다......

        return ResponseEntity.ok(param.keySet().toString());
    }

    public static class TestModule {
    }
}