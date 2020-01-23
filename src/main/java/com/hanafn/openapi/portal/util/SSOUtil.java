package com.hanafn.openapi.portal.util;

import com.initech.eam.api.NXContext;
import com.initech.eam.api.NXNLSAPI;
import com.initech.eam.nls.CookieManager;
import com.initech.eam.smartenforcer.SECode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.io.*;
import java.net.*;

@Component
@Slf4j
public class SSOUtil {

    /*** SERVICE CONFIGURATION **********************************************************************/
    public static String SERVICE_NAME = "PHFNAPIADM01";    // 업무시스템 코드
//    public static String SERVICE_NAME = "THFNAPIADM01";    // 업무시스템 코드
    public static String SERVER_URL   = "https://oneapi.hanafn.com";      // 업무시스템 접속 도메인
//    public static String SERVER_URL   = "https://toneapi.hanafn.com";      // 업무시스템 접속 도메인
    public static String SERVER_PORT  = "8443";     // 업무시스템 접속 포트
    public static String ASCP_URL     = SERVER_URL + ":" + SERVER_PORT + "/auth/ssoCheck";

    // ASCP 페이지에서 uurl 로 리다이렉트하지 않을 페이지 지정
    public static String[] SKIP_URL = {"", "/", "/index.html", "/index.htm"};
    /*************************************************************************************************/


    /*** SSO CONFIGURATION ***************************************************************************/
    public static String NLS_URL			= "http://ssg.hanafn.com";
//    public static String NLS_URL			= "http://ssgtest.hanafn.com";
    public static String NLS_PORT			= "18080";
    public static String NLS_LOGIN_URL	= NLS_URL + ":" + NLS_PORT + "/nls3/clientLogin.jsp";
    public static String NLS_LOGOUT_URL	= NLS_URL + ":" + NLS_PORT + "/nls3/NCLogout.jsp";
    public static String NLS_ERROR_URL	= NLS_URL + ":" + NLS_PORT + "/nls3/error.jsp";
    public static String ND_URL1	= "http://ssg.hanafn.com:5480"; //운영 (IP : 10.72.42.20)
    public static String ND_URL2	= "http://ssg.hanafn.com:5480"; //운영 (IP : 10.72.42.20)
//    public static String ND_URL1	= "http://10.72.42.20:5480"; //운영 (IP : 10.72.42.20)
//    public static String ND_URL2	= "http://ssg.hanafn.com:5480"; //운영 (IP : 10.72.42.20)
//    public static String ND_URL1	= "http://ssgtest.hanafn.com:5480"; //테스트 (IP : 10.205.3.71)
//    public static String ND_URL2	= "http://ssgtest.hanafn.com:5480"; //테스트 (IP : 10.205.3.71)

    public static final int COOKIE_SESSTION_TIME_OUT = 3000000;

    // 인증 타입 (ID/PW 방식 : 1, 인증서 : 3)
    public static String TOA = "1";
    public static String SSO_DOMAIN = ".hanafn.com";

    public static final int timeout = 15000;

    /*************************************************************************************************/

    // 통합 SSO ID 조회
    public static String getSsoId(HttpServletRequest request) {
        String sso_id = CookieManager.getCookieValue(SECode.USER_ID, request);
        String sso_hmac = CookieManager.getCookieValue(SECode.USER_HMAC, request);
        String sso_ip = CookieManager.getCookieValue(SECode.USER_IP, request);
        String sso_lat = CookieManager.getCookieValue(SECode.USER_LAT, request);
        String sso_toa = CookieManager.getCookieValue(SECode.USER_TOA, request);

        log.debug("@@@ sso_id >> " + sso_id);
        log.debug("@@@ sso_hmac >> " + sso_hmac);
        log.debug("@@@ sso_ip >> " + sso_ip);
        log.debug("@@@ sso_lat >> " + sso_lat);
        log.debug("@@@ sso_toa >> " + sso_toa);

        return sso_id;
    }

    // 통합 SSO 로그인페이지 이동
    public static String goLoginPage(HttpServletResponse response, String uurl) throws Exception {
        CookieManager.addCookie(SECode.USER_URL, ASCP_URL, SSO_DOMAIN, response);
        CookieManager.addCookie(SECode.R_TOA, TOA, SSO_DOMAIN, response);

//        String page = NLS_LOGIN_URL+"?UURL=" + uurl;     // 통합 로그인 페이지로 이동
       String page = "https://oneapi.hanafn.com:8443/#/login";        // OpenAPI 관리자포탈 로그인 페이지로 이동

        return page;
    }

    /* 그룹용 함수 */
    //ND API를 사용해서 쿠키검증하는것(현재 표준에서는 사용안함, 근데 해도 되기는 함)
    public static String getEamSessionCheck2(HttpServletRequest request, HttpServletResponse response) {
        String retCode = "";

//        List<String> serverurlList = new ArrayList<String>();
//        serverurlList.add(ND_URL1);
//        serverurlList.add(ND_URL2);
//
//        NXContext context = new NXContext(serverurlList);

        String serverUrl = ND_URL1;
        NXContext context = new NXContext(serverUrl);
        try {
            NXNLSAPI nxNLSAPI = new NXNLSAPI(context);
            retCode = nxNLSAPI.readNexessCookie(request, response, 0, 300000);
        } catch(Exception npe) {
            npe.printStackTrace();
        }
        return retCode;
    }

    // SSO 에러페이지 URL
    public static String goErrorPage(HttpServletResponse response, int error_code) throws Exception {
        CookieManager.removeNexessCookie(SSO_DOMAIN, response);
        CookieManager.addCookie(SECode.USER_URL, ASCP_URL, SSO_DOMAIN, response);
//        response.sendRedirect(NLS_ERROR_URL + "?errorCode=" + error_code);
        return NLS_ERROR_URL + "?errorCode=" + error_code;
    }

    /**
     * check default page => 사용자 요청 페이지 체크
     */
    public static String checkUurl(String uurl) {
        String uri = null;
        URL url = null;
        try {
            url = new URL(uurl);
            uri = url.getPath();
        } catch (Exception e) {
            // URI 인 경우
            uri = uurl;
        }
        for (int i = 0; i < SKIP_URL.length; i++) {
            if (SKIP_URL[i].equals(uri)) {
//                uurl = null;
                break;
            }
        }
        return uurl;
    }

    // 업무시스템 접속정보 저장
    public static void saveAppInfo(String appcode, String serverip, String rstcode, String userip, String sso_id) {
        StringBuffer sb = new StringBuffer();
        BufferedReader in = null;
        BufferedWriter bw = null;

        try {
            String Stat_url = NLS_URL + ":" + NLS_PORT + "/3rdParty/appSsoAuth.jsp";
            URL url = new URL(Stat_url);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true); //POST
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            String gb = "\t";
            String param = appcode	+ gb
                    + serverip	+ gb
                    + rstcode	+ gb
                    + userip	+ gb
                    + sso_id	+ gb;
            param = URLEncoder.encode(CookieManager.encryptWithSEED(param), "UTF-8");
            //write
            bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bw.write("param=" + param);
            bw.flush();
            bw.close();

            //read
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            log.debug("@@ sso - save app info success");

        } catch (MalformedURLException e) {
            sb.append("MalformedURLException::" + e.toString());
        } catch (IOException e) {
            sb.append("IOException::" + e.toString());
        } catch (Exception e) {
            sb.append("exception::" + e.toString());
        } finally {
            try {
                if(in != null) { in.close(); }
                if(bw != null) { bw.close(); }
            } catch( Exception e) {}
        }
    }

    public static String getRemoteIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
//        log.info("@@ X-Forwarded-For: " + ip);

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
//            log.info("@@ Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP"); // 웹로직
//            log.info("@@ WL-Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
//            log.info("@@ HTTP_CLIENT_IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
//            log.info("@@ HTTP_X_FORWARDED_FOR : " + ip);
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

//        log.info("@@ Result : IP Address : "+ip);

        return ip;
    }

    public static String ssoIdGenerator(String sso_id) {
        String hfnCd = sso_id.substring(0,2);
        String hfnId = "";

        switch(hfnCd) {
            case "00":
                hfnId = sso_id.substring(5,9);
                break;
            case "01":
                // 은행인 경우 사번은 7자리
                hfnId = sso_id.substring(3,9);
                break;
            case "02":
                hfnId = sso_id.substring(5,9);
                break;
            case "04":
                hfnId = sso_id.substring(5,9);
                break;
            case "05":
                hfnId = sso_id.substring(5,9);
                break;
            case "07":
                hfnId = sso_id.substring(5,9);
                break;
            case "12":
                hfnId = sso_id.substring(5,9);
                break;
            case "99":
                hfnId = sso_id.substring(5,9);
                break;
            default:
                hfnId = sso_id.substring(5,9);
                break;
        }

        log.debug("@@ sso hfn userid >> " + hfnCd + hfnId);
        return hfnCd + hfnId;
    }



}
