package com.hanafn.openapi.portal.security.controller;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.dto.NiceIdResponse;
import com.hanafn.openapi.portal.security.repository.SignupRepository;
import com.hanafn.openapi.portal.views.dto.SndCertMgntRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class NiceIdController {

    private final SignupRepository signupRepository;

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${document.domain}")
    private String documentDomain;

    @Value("${domain}")
    private String domain;

    @Autowired
    MessageSourceAccessor messageSource;

    @PostMapping("/getEncData")
    public ResponseEntity<?> niceMain(HttpServletRequest request){
        NiceID.Check.CPClient niceCheck = new  NiceID.Check.CPClient();

        String sSiteCode = "BP298";				// NICE로부터 부여받은 사이트 코드
        String sSitePassword = "Q48LngZMgFPn";			// NICE로부터 부여받은 사이트 패스워드

        String sRequestNumber = "REQ0000000001";        	// 요청 번호, 이는 성공/실패후에 같은 값으로 되돌려주게 되므로
        // 업체에서 적절하게 변경하여 쓰거나, 아래와 같이 생성한다.
        sRequestNumber = niceCheck.getRequestNO(sSiteCode);
//        session.setAttribute("REQ_SEQ" , sRequestNumber);	// 해킹등의 방지를 위하여 세션을 쓴다면, 세션에 요청번호를 넣는다.

        String sAuthType = "";      	// 없으면 기본 선택화면, M: 핸드폰, C: 신용카드, X: 공인인증서

        String popgubun 	= "N";		//Y : 취소버튼 있음 / N : 취소버튼 없음
        String customize 	= "";		//없으면 기본 웹페이지 / Mobile : 모바일페이지

        String sGender = ""; 			//없으면 기본 선택 값, 0 : 여자, 1 : 남자

        // CheckPlus(본인인증) 처리 후, 결과 데이타를 리턴 받기위해 다음예제와 같이 http부터 입력합니다.
        //리턴url은 인증 전 인증페이지를 호출하기 전 url과 동일해야 합니다. ex) 인증 전 url : http://www.~ 리턴 url : http://www.~
//        String sReturnUrl = "https://hanafnapimarket.com/#/niceSuccess";      // 성공시 이동될 URL
        domain = request.getRequestURL().toString().replaceAll(request.getRequestURI(), "");
        if(profile.equals("staging") && domain.contains("https://")) {
            domain = domain.replace("https://","http://");
            log.debug("★domain replaced;");
        }
        log.debug("★" + domain);
        String sReturnUrl = domain+"/auth/niceSuccess";      // 성공시 이동될 URL
        String sErrorUrl = domain+"/auth/niceFail";        // 실패시 이동될 URL

        // 입력될 plain 데이타를 만든다.
        String sPlainData = "7:REQ_SEQ" + sRequestNumber.getBytes().length + ":" + sRequestNumber +
                "8:SITECODE" + sSiteCode.getBytes().length + ":" + sSiteCode +
                "9:AUTH_TYPE" + sAuthType.getBytes().length + ":" + sAuthType +
                "7:RTN_URL" + sReturnUrl.getBytes().length + ":" + sReturnUrl +
                "7:ERR_URL" + sErrorUrl.getBytes().length + ":" + sErrorUrl +
                "11:POPUP_GUBUN" + popgubun.getBytes().length + ":" + popgubun +
                "9:CUSTOMIZE" + customize.getBytes().length + ":" + customize +
                "6:GENDER" + sGender.getBytes().length + ":" + sGender;

        String sMessage = "";
        String sEncData = "";

        int iReturn = niceCheck.fnEncode(sSiteCode, sSitePassword, sPlainData);
        if( iReturn == 0 )
        {
            sEncData = niceCheck.getCipherData();
        }
        else if( iReturn == -1)
        {
            sMessage = "나이스 본인인증 암호화 시스템 에러입니다.";
        }
        else if( iReturn == -2)
        {
            sMessage = "나이스 본인인증 암호화 처리오류입니다.";
        }
        else if( iReturn == -3)
        {
            sMessage = "나이스 본인인증 암호화 데이터 오류입니다.";
        }
        else if( iReturn == -9)
        {
            sMessage = "나이스 본인인증 입력 데이터 오류입니다.";
        }
        else {
            sMessage = "[나이스 본인인증] 알수 없는 에러 입니다. iReturn : " + iReturn;
        }

        if(iReturn != 0) {
            log.error(sMessage);
            throw new BusinessException("E026",sMessage);
        }

        return ResponseEntity.ok(sEncData);
    }

    @PostMapping(value = "/niceSuccess", produces = "text/html;charset=UTF-8")
    public String niceSucess(HttpServletRequest httpServletRequest, HttpServletResponse response, @RequestParam(value = "EncodeData")String request) {

        log.debug("★Servletresponse:"+response);

        NiceID.Check.CPClient niceCheck = new  NiceID.Check.CPClient();

        String sEncodeData = requestReplace(request, "encodeData");

        String sSiteCode = "BP298";				// NICE로부터 부여받은 사이트 코드
        String sSitePassword = "Q48LngZMgFPn";			// NICE로부터 부여받은 사이트 패스워드

        String sCipherTime = "";			// 복호화한 시간
        String sRequestNumber = "";			// 요청 번호
        String sResponseNumber = "";		// 인증 고유번호
        String sAuthType = "";				// 인증 수단
        String sName = "";					// 성명
        String sDupInfo = "";				// 중복가입 확인값 (DI_64 byte)
        String sConnInfo = "";				// 연계정보 확인값 (CI_88 byte)
        String sBirthDate = "";				// 생년월일(YYYYMMDD)
        String sGender = "";				// 성별
        String sNationalInfo = "";			// 내/외국인정보 (개발가이드 참조)
        String sMobileNo = "";				// 휴대폰번호
        String sMobileCo = "";				// 통신사
        String sMessage = "";
        String sPlainData = "";

        int iReturn = niceCheck.fnDecode(sSiteCode, sSitePassword, sEncodeData);

        java.util.HashMap mapresult = null;
        NiceIdResponse niceIdResponse = new NiceIdResponse();

        if( iReturn == 0 )
        {
            sPlainData = niceCheck.getPlainData();
            sCipherTime = niceCheck.getCipherDateTime();

            // 데이타를 추출합니다.
            mapresult = niceCheck.fnParse(sPlainData);

            sRequestNumber  = (String)mapresult.get("REQ_SEQ");
            sResponseNumber = (String)mapresult.get("RES_SEQ");
            sAuthType		= (String)mapresult.get("AUTH_TYPE");
            log.debug("★NAME:"+(String)mapresult.get("NAME"));
            log.debug("★UTF8_NAME:"+(String)mapresult.get("UTF8_NAME"));
            sName			= (String)mapresult.get("NAME");
//            sName			= (String)mapresult.get("UTF8_NAME"); //charset utf8 사용시 주석 해제 후 사용
            sBirthDate		= (String)mapresult.get("BIRTHDATE");
            sGender			= (String)mapresult.get("GENDER");
            sNationalInfo  	= (String)mapresult.get("NATIONALINFO");
            sDupInfo		= (String)mapresult.get("DI");
            sConnInfo		= (String)mapresult.get("CI");
            sMobileNo		= (String)mapresult.get("MOBILE_NO");
            sMobileCo		= (String)mapresult.get("MOBILE_CO");

//            String session_sRequestNumber = (String)session.getAttribute("REQ_SEQ");
//            if(!sRequestNumber.equals(session_sRequestNumber))
//            {
//                sMessage = "세션값 불일치 오류입니다.";
//                sResponseNumber = "";
//                sAuthType = "";
//            }

            niceIdResponse.setSResponseNumber(sResponseNumber);
            niceIdResponse.setSRequestNumber(sRequestNumber);
            niceIdResponse.setSAuthType(sAuthType);
            niceIdResponse.setSName(sName);

            if(sBirthDate.length() >= 8){
                sBirthDate = sBirthDate.substring(2,8);
                log.debug("★나이스본인인증 생일자리수 축소 => " + sBirthDate);
            }
            niceIdResponse.setSBirthDate(sBirthDate);
            niceIdResponse.setSCipherTime(sCipherTime);
            niceIdResponse.setSDupInfo(sDupInfo);
            niceIdResponse.setSConnInfo(sConnInfo);
            if(StringUtils.equals(sGender,"0")){    // 여자
                sGender = "F";
            } else if (StringUtils.equals(sGender,"1")) {   // 남자
                sGender = "M";
            }
            niceIdResponse.setSGender(sGender);
            niceIdResponse.setSMessage(sMessage);
            niceIdResponse.setSMobileCo(sMobileCo);
            niceIdResponse.setSMobileNo(sMobileNo);
            if(StringUtils.equals(sNationalInfo,"0")) { // 내국인
                sNationalInfo = "local";
            } else if (StringUtils.equals(sGender,"1")){ // 외국인
                sNationalInfo = "foreigner";
            }
            niceIdResponse.setSNationalInfo(sNationalInfo);
            niceIdResponse.setSPlainData(sPlainData);
        }
        else if( iReturn == -1)
        {
            sMessage = "복호화 시스템 오류입니다.";
        }
        else if( iReturn == -4)
        {
            sMessage = "복호화 처리 오류입니다.";
        }
        else if( iReturn == -5)
        {
            sMessage = "복호화 해쉬 오류입니다.";
        }
        else if( iReturn == -6)
        {
            sMessage = "복호화 데이터 오류입니다.";
        }
        else if( iReturn == -9)
        {
            sMessage = "입력 데이터 오류입니다.";
        }
        else if( iReturn == -12)
        {
            sMessage = "사이트 패스워드 오류입니다.";
        }
        else
        {
            sMessage = "알 수 없는 에러 입니다. iReturn : " + iReturn;
        }

        log.debug("★NiceRsponse:"+niceIdResponse);

        SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
        sndCertMgntRequest.setSendCd("S1");		// 본인인증 코드
        sndCertMgntRequest.setSendNo(niceIdResponse.getSDupInfo());
        sndCertMgntRequest.setResultCd(niceIdResponse.getSResponseNumber());

        try {
            signupRepository.insertSndCertMgntForSelfAuth(sndCertMgntRequest);
        } catch ( Exception e ) {
            log.error("본인인증 성공 이력 DB 삽입 중 에러 : " + e.toString());
            throw new BusinessException("N001", messageSource.getMessage("N001"));
        }

        documentDomain = httpServletRequest.getRequestURL().toString().replaceAll(httpServletRequest.getRequestURI(), "").replaceAll("https://","").replaceAll("http://","");
        log.debug("★DocumentDomain:"+documentDomain);
//        https://www.hanafnapimarket.com
        return "<html><head>" +
                "<script>" +
                "alert('인증 완료되었습니다.');" +
                "document.domain=\""+documentDomain+"\";" +
                "window.opener.document.getElementById('niceResult').value=\"true\";" +
                "window.opener.document.getElementById('DI').value=\""+niceIdResponse.getSDupInfo()+"\";" +
                "window.opener.document.getElementById('RES_SEQ').value=\""+niceIdResponse.getSResponseNumber()+"\";" +
                "window.opener.document.getElementById('MOBILE_NO').value=\""+niceIdResponse.getSMobileNo()+"\";" +
                "window.opener.document.getElementById('MOBILE_CO').value=\""+niceIdResponse.getSMobileCo()+"\";" +
                "window.opener.document.getElementById('S_NAME').value=\""+niceIdResponse.getSName()+"\";" +
                "window.opener.document.getElementById('NATIONALINFO').value=\""+niceIdResponse.getSNationalInfo()+"\";" +
                "window.close();" +
                "</script></head></html>";
    }

    @PostMapping(value="/niceFail", produces = "text/html;charset=UTF-8")
    public String niceFail(HttpServletResponse response, @RequestParam(value = "EncodeData")String request) {
        NiceID.Check.CPClient niceCheck = new NiceID.Check.CPClient();

        String sEncodeData = requestReplace(request, "encodeData");

        String sSiteCode = "BP298";                // NICE로부터 부여받은 사이트 코드
        String sSitePassword = "Q48LngZMgFPn";            // NICE로부터 부여받은 사이트 패스워드

        String sCipherTime = "";            // 복호화한 시간
        String sRequestNumber = "";            // 요청 번호
        String sErrorCode = "";                // 인증 결과코드
        String sAuthType = "";                // 인증 수단
        String sMessage = "";
        String sPlainData = "";

        int iReturn = niceCheck.fnDecode(sSiteCode, sSitePassword, sEncodeData);

        if (iReturn == 0) {
            sPlainData = niceCheck.getPlainData();
            sCipherTime = niceCheck.getCipherDateTime();

            // 데이타를 추출합니다.
            java.util.HashMap mapresult = niceCheck.fnParse(sPlainData);

            sRequestNumber = (String) mapresult.get("REQ_SEQ");
            sErrorCode = (String) mapresult.get("ERR_CODE");
            sAuthType = (String) mapresult.get("AUTH_TYPE");
        } else if (iReturn == -1) {
            sMessage = "복호화 시스템 에러입니다.";
        } else if (iReturn == -4) {
            sMessage = "복호화 처리 오류입니다.";
        } else if (iReturn == -5) {
            sMessage = "복호화 해쉬 오류입니다.";
        } else if (iReturn == -6) {
            sMessage = "복호화 데이터 오류입니다.";
        } else if (iReturn == -9) {
            sMessage = "입력 데이터 오류입니다.";
        } else if (iReturn == -12) {
            sMessage = "사이트 패스워드 오류입니다.";
        } else {
            sMessage = "알 수 없는 에러 입니다. iReturn : " + iReturn;
        }

        SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
        sndCertMgntRequest.setSendCd("S1");		// 본인인증 코드
        sndCertMgntRequest.setSendNo("SELF AUTH FAIL");
        sndCertMgntRequest.setResultCd(sErrorCode);

        try {
            signupRepository.insertSndCertMgntForSelfAuth(sndCertMgntRequest);
        } catch ( Exception e ) {
            log.error("본인인증 실패 이력 DB 삽입 중 에러 => " + sndCertMgntRequest);
            throw new BusinessException("N001", messageSource.getMessage("N001"));
        }

        return "<html><script>alert('인증에 실패하였습니다');" +
                "window.opener.document.getElementById('niceResult').value=\"false\";" +
                "window.close();" +
                "</script></html>";
    }


    public String requestReplace (String paramValue, String gubun) {

        String result = "";

        if (paramValue != null) {

            paramValue = paramValue.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

            paramValue = paramValue.replaceAll("\\*", "");
            paramValue = paramValue.replaceAll("\\?", "");
            paramValue = paramValue.replaceAll("\\[", "");
            paramValue = paramValue.replaceAll("\\{", "");
            paramValue = paramValue.replaceAll("\\(", "");
            paramValue = paramValue.replaceAll("\\)", "");
            paramValue = paramValue.replaceAll("\\^", "");
            paramValue = paramValue.replaceAll("\\$", "");
            paramValue = paramValue.replaceAll("'", "");
            paramValue = paramValue.replaceAll("@", "");
            paramValue = paramValue.replaceAll("%", "");
            paramValue = paramValue.replaceAll(";", "");
            paramValue = paramValue.replaceAll(":", "");
            paramValue = paramValue.replaceAll("-", "");
            paramValue = paramValue.replaceAll("#", "");
            paramValue = paramValue.replaceAll("--", "");
            paramValue = paramValue.replaceAll("-", "");
            paramValue = paramValue.replaceAll(",", "");

            if(gubun != "encodeData"){
                paramValue = paramValue.replaceAll("\\+", "");
                paramValue = paramValue.replaceAll("/", "");
                paramValue = paramValue.replaceAll("=", "");
            }

            result = paramValue;

        }
        return result;
    }

}
