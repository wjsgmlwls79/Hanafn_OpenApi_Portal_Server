package com.hanafn.openapi.portal.util;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.SignUpRequest;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class CommonUtil {
    @Autowired
    SettingRepository settingRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MessageSourceAccessor messageSource;

    public UserPrincipal userKeyChangeIfDeveloper(UserPrincipal currentUser){

        UserPrincipal userPrincipal = currentUser;
        if(StringUtils.equals(currentUser.getUserType(),"ORGD")) {
            currentUser.setUserKey(currentUser.getUseorgKey());
        }

        return userPrincipal;
    }

    public String stackTraceToString(Exception e) {
        Stream<StackTraceElement> stackTraceElementStream = Arrays.stream(e.getStackTrace()).limit(20);
        String stackResult = stackTraceElementStream.map(StackTraceElement::toString).collect(Collectors.joining("\n"));
        return stackResult;
    }

    /** 파일이름만 추출 => 파일이름 없을 시, 빈 문자열 return **/
    public String getOriginalFileName(String fileName) {
        String result = "";
        if(!StringUtils.isBlank(fileName)){
            int pos = fileName.lastIndexOf('\\');
            result = fileName.substring( pos + 1 );
            return result;
        }
        return result;
    }

    public String getUserTypeByRoleCd(String roleCd){
        String userType = null;
        if(StringUtils.equals(roleCd, "4"))
            userType = "ORGM";
        else if(StringUtils.equals(roleCd, "5"))
            userType = "ORGD";
        else if(StringUtils.equals(roleCd, "6"))
            userType = "USER";
        else {
            log.error("처리할 수 없는 roleCd :" + roleCd);
            throw new BusinessException(messageSource.getMessage("E092"));
        }
        return userType;
    }
    public long unixTime(){
        Date currentDate = new Date();
        long unixTime = currentDate.getTime() / 1000;
        return unixTime;
    }
    public Boolean compareWithShaStrings(String string, String shaString) {
        if (!passwordEncoder.matches(string, shaString)) {
            log.error(messageSource.getMessage("L003"));
            throw new BusinessException("L003",messageSource.getMessage("L003"));
        }
        return true;
    }

    public Boolean compareWithShaStringsPw(String string, String shaString) {
        if (passwordEncoder.matches(string, shaString)) {
            return false;
        }
        return true;
    }

    public String generateEncKey(SignUpRequest.UseorgSignUpRequest request){

        settingRepository.insertDateRowIfNotExists();
        settingRepository.updateDateSeqForNoDup();
        String encKey="";

        try{

            encKey = String.format("%10s%1s%6s%03d",request.getUseorgId(), 'R', DateUtil.getCurrentDate6(), settingRepository.getDateSeq());
            encKey = StringUtils.replace(encKey, " ", "@");

            log.debug("CommonUtil generateEncKey ["+encKey+"}");

        } catch (Exception e){
            log.error(e.getMessage());
            throw new BusinessException(messageSource.getMessage("E026"));
        }

        try {

            encKey = AES256Util.encrypt(encKey);
            log.debug("CommonUtil AES256Util.Encrypt EncKey) ["+encKey+"}");

        } catch (Exception e) {
            log.error("Error while Encrypt encKey");
            throw new BusinessException(messageSource.getMessage("E093"));
        }

        return encKey;
    }

    // 전화번호를 3개의 파트로 나누어 저장한다.
    public List<String> parseTelNum(String target){
        List<String> result = new ArrayList<String>();

        if(target.length() == 9){
            result.add(target.substring(0,2));
            result.add(target.substring(2,5));
            result.add(target.substring(5,9));
        }
        else if(target.length() == 10){
            result.add(target.substring(0,2));
            result.add(target.substring(2,6));
            result.add(target.substring(6,10));
        }
        else if(target.length() == 11){
            result.add(target.substring(0,3));
            result.add(target.substring(3,7));
            result.add(target.substring(7,11));
        }
        else if(target.length() == 12){
            result.add(target.substring(0,4));
            result.add(target.substring(4,8));
            result.add(target.substring(8,12));
        }
        else {
            result = null;
            log.error("처리할수 없는 전화번호 길이 (파싱에러) : " + target);
            throw new BusinessException("처리할 수 없는 전화번호 길이입니다 : " + target );
        }

        return result;
    }

    public String getIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        //log.debug(">>>> X-FORWARDED-FOR : " + ip);

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
            //log.debug(">>>> Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP"); // 웹로직
            //log.debug(">>>> WL-Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            //log.debug(">>>> HTTP_CLIENT_IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            //log.debug(">>>> HTTP_X_FORWARDED_FOR : " + ip);
        }
        if (ip == null) {
            //ip = request.getRemoteAddr();
        }
        return ip;
    }

    public boolean superAdminCheck(UserPrincipal currentUser) {
        if(StringUtils.equals(currentUser.getSiteCd(),"adminPortal")){
            Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
            for(GrantedAuthority ga : authorities) {
                if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                    return true;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }

    public String hfnCd2hfnNm(String hfnCd) {
        String hfnNm = "";
        switch(hfnCd) {
            case "01":
                hfnNm = "KEB하나은행";
                break;
            case "02":
                hfnNm = "하나금융투자";
                break;
            case "04":
                hfnNm = "하나생명";
                break;
            case "05":
                hfnNm = "하나캐피탈";
                break;
            case "12":
                hfnNm = "하나카드";
                break;
            case "14":
                hfnNm = "하나저축은행";
                break;
            case "99":
                hfnNm = "하나멤버스";
                break;
            default:
                hfnNm = "Invalid HFN Company";
                break;
        }

        return hfnNm;
    }
}