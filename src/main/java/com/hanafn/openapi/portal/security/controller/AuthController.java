package com.hanafn.openapi.portal.security.controller;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.*;
import com.hanafn.openapi.portal.security.jwt.JwtTokenProvider;
import com.hanafn.openapi.portal.security.service.CustomUserDetailsService;
import com.hanafn.openapi.portal.security.service.SignupService;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.util.SSOUtil;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.HfnInfoVO;
import com.hanafn.openapi.portal.views.vo.UseorgVO;
import com.hanafn.openapi.portal.views.vo.UserPwHisVO;
import com.hanafn.openapi.portal.views.vo.UserVO;
import com.hanafn.openapi.portal.cmct.RedisCommunicater;
import com.initech.eam.nls.CookieManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.CookieGenerator;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    MessageSourceAccessor messageSource;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    CommonUtil commonUtil;
    @Autowired
    JwtTokenProvider tokenProvider;
    @Autowired
    SignupService signUpService;
    @Autowired
    SettingService settingService;
    @Autowired
    SettingRepository settingRepository;
    @Autowired
    CustomUserDetailsService userDetailService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/hfnLogin")
    public ResponseEntity<?> authenticateHfnUser(@Valid @RequestBody HfnLoginRequest requestLogin) {

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(requestLogin.getUsername(), requestLogin.getPassword());
        authRequest.setDetails(requestLogin.getSiteCd());

        Authentication authentication = authenticationManager.authenticate(authRequest);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal currentUser = (UserPrincipal)authentication.getPrincipal();

        Collection<? extends GrantedAuthority> authorities =  currentUser.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();

        while(iter.hasNext()) {
            GrantedAuthority role = iter.next();

            if(StringUtils.equals(requestLogin.getSiteCd(), "adminPortal")){
                if(!(StringUtils.equals(role.getAuthority(),"ROLE_SYS_ADMIN") || StringUtils.equals(role.getAuthority(),"ROLE_HFN_ADMIN") ||
                        StringUtils.equals(role.getAuthority(),"ROLE_HFN_USER"))){
                    log.error("사용자 ID[" + currentUser.getUsername() + "]");
                    throw new BusinessException("L001",messageSource.getMessage("L001"));
                }
            }
        }

        String jwt = tokenProvider.generateToken(authentication);
        LoginResponse data= new LoginResponse(jwt);
        data.setPortalTosYn(currentUser.getPortalTosYn());
        data.setPrivacyTosYn(currentUser.getPrivacyTosYn());
        data.setTmpPasswordYn(currentUser.getTmpPasswordYn());

        // Redis 토큰저장
        RedisCommunicater.setToken(jwt);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/userLogin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest requestLogin) {


        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(requestLogin.getUsername(), requestLogin.getPassword());
        authRequest.setDetails(requestLogin.getSiteCd());

        Authentication authentication = authenticationManager.authenticate(authRequest);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal currentUser = (UserPrincipal)authentication.getPrincipal();

        if (StringUtils.equals(currentUser.getUserType(), "USER")
                || StringUtils.equals(currentUser.getUserType(), "ORGD")) {
            // 복호화
            try {
                // 이름
                if (currentUser.getUsername() != null && !"".equals(currentUser.getUsername())) {
                    String decryptedUseorgUserNm = AES256Util.decrypt(currentUser.getUsername());
                    currentUser.setUsername(decryptedUseorgUserNm);
                    log.debug("복호화 - 이름: {}", decryptedUseorgUserNm);
                }
            } catch ( Exception e ) {
                log.error("클라이언트ID 디크립트 에러");
            }
        }

        Collection<? extends GrantedAuthority> authorities =  currentUser.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();

        while(iter.hasNext()) {
            GrantedAuthority role = iter.next();

            if(StringUtils.equals(requestLogin.getSiteCd(), "userPortal")){
                if(!(StringUtils.equals(role.getAuthority(),"ROLE_PERSONAL") || StringUtils.equals(role.getAuthority(),"ROLE_ORG_ADMIN") || StringUtils.equals(role.getAuthority(),"ROLE_ORG_USER"))){
                    log.error("사용자 ID[" + currentUser.getUsername() + "]");
                    throw new BusinessException("L001",messageSource.getMessage("L001"));
                }
            }
        }

        String jwt = tokenProvider.generateToken(authentication);
        LoginResponse data= new LoginResponse(jwt);
        data.setPortalTosYn(currentUser.getPortalTosYn());
        data.setPrivacyTosYn(currentUser.getPrivacyTosYn());
        data.setUserType(currentUser.getUserType());
        data.setPwdChangeDt(currentUser.getPwdChangeDt());

        // Redis 토큰저장
        RedisCommunicater.setToken(jwt);

        return ResponseEntity.ok(data);
    }

    @GetMapping(value="/ssoCheck", produces="text/html;charset=UTF-8")
    public HttpServletResponse ssoCheck(HttpServletRequest request, HttpServletResponse response) {
        //인증정보
        String sso_id	= null;	//통합인증ID
        String uurl		= null;	//사용자 요청
        String appCode	= SSOUtil.SERVICE_NAME;
        String serverIp	= request.getLocalAddr();	//서버IP
        String userIp	= SSOUtil.getRemoteIP(request);	//사용자IP

        // 쿠키 및 헤더 확인
//        Enumeration<String> emheader = request.getHeaderNames();
//        while (emheader.hasMoreElements() ) {
//            String key = emheader.nextElement();
//            System.out.println(String.format("@@ >> %s=%s", key, request.getHeader(key)));
//        }
        //쿠키 암호화 적용
        CookieManager.setEncStatus(true);

        // 1.uurl 설정
        if (SSOUtil.checkUurl(uurl) == null) {
            uurl = SSOUtil.ASCP_URL;
        }
        log.debug("@@ uurl >> " + uurl);

        // 2.SSO ID 수신
        sso_id = SSOUtil.getSsoId(request);

        try{
            // 3.SSO ID가 없다면 SSO 통합 로그인 페이지로 이동
            if (sso_id == null) {
                log.debug("@@ sso - login page move");
                response.sendRedirect(SSOUtil.goLoginPage(response, uurl));
            } else {

                log.debug("@@ sso id is exist >> " + sso_id);
                // 4.쿠키 유효성 확인 :0(정상)
                String retCode = "1000";
                retCode = SSOUtil.getEamSessionCheck2(request, response);
                log.debug("@@ sso check retcode >> " + retCode);

                // 5. 쿠키 유효성 체크
                if ( (!retCode.equals("0")) && (!retCode.equals("1002")) ) { // 쿠키 비정상
                    log.debug("@@ cookie error: retcode >> " + retCode);

                    response.sendRedirect(SSOUtil.goErrorPage(response, Integer.parseInt(retCode)));
                } else { // 쿠키가 정상이면
                    log.debug("@@ sso cookie valid");
                    // 6.업무시스템 접속 정보저장
                    SSOUtil.saveAppInfo(appCode, serverIp, retCode, userIp, sso_id);

                    // sso 인증 완료 후 openapi 접속을 위한 토큰 할당
                    String userId = SSOUtil.ssoIdGenerator(sso_id);
                    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userId, "SSO");
                    authRequest.setDetails("adminPortal");
                    Authentication authentication = authenticationManager.authenticate(authRequest);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();

                    Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
                    Iterator<? extends GrantedAuthority> iter = authorities.iterator();

                    while (iter.hasNext()) {
                        GrantedAuthority role = iter.next();

//                    if(StringUtils.equals(ssoLoginRequest.getSiteCd(), "adminPortal")){
                        if (!(StringUtils.equals(role.getAuthority(), "ROLE_SYS_ADMIN") || StringUtils.equals(role.getAuthority(), "ROLE_HFN_ADMIN") ||
                                StringUtils.equals(role.getAuthority(), "ROLE_HFN_USER"))) {
                            log.error("사용자 ID[" + currentUser.getUsername() + "]");
                            throw new BusinessException("L001",messageSource.getMessage("L001"));
                        }
//                    }
                    }

                    String jwt = tokenProvider.generateToken(authentication);

                    log.debug("@@ token >> " + jwt);

                    // Redis 토큰저장
                    RedisCommunicater.setToken(jwt);

                    // 쿠키 세팅
                    CookieGenerator cg = new CookieGenerator();
                    cg.setCookieName("Admin-Portal-Token");
                    cg.addCookie(response, jwt);

                    response.sendRedirect("https://oneapi.hanafn.com:8443/#/dashboard");
                }
            }
        } catch(Exception e) {
            log.error("@@ exception >> " + e.toString());
        }

        return response;
    }

    @PostMapping("/logoutHfnUser")
    public ResponseEntity<SignUpResponse> logoutHfnUser(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(new SignUpResponse(true, "User registered successfully"));
    }

    @PostMapping("/logoutUser")
    public ResponseEntity<SignUpResponse> logoutUser() {
        return ResponseEntity.ok(new SignUpResponse(true, "User registered successfully"));
    }

    @PostMapping("/userIdDupCheck")
    public ResponseEntity<?> userIdDupCheck(@Valid @RequestBody UserRequest.UserDupCheckRequest request) {
        int data = signUpService.userIdDupCheck(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/brnDupCheck")
    public ResponseEntity<?> brnDupCheck(@Valid @RequestBody UseorgRequest.UseorgDupCheckRequest request) {
        UseorgRsponse.UseorgDupCheckResponse data = settingService.useorgBrnDupCheck(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/hfnGroupCode")
    public ResponseEntity<?> hfnGropCode() {

        HfnInfoRsponse data = new HfnInfoRsponse();
        List<HfnInfoVO> list = settingRepository.selectHfnCdList();
        data.setList(list);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/checkPwd")
    public ResponseEntity<?> checkPwd(@CurrentUser UserPrincipal currentUser, @RequestBody String userPwd){
        boolean result = commonUtil.compareWithShaStrings(userPwd, currentUser.getPassword());
        return ResponseEntity.ok(result);
    }

    /*********** 개인사용자 회원가입 *************/
    @PostMapping("/signUpUser")
    public ResponseEntity<SignUpResponse> signUpUser(@Valid @RequestBody SignUpRequest.UserSingnUpRequest request) {
        request.setRegUser(request.getUserNm());
        request.setUserId(StringUtils.lowerCase(request.getUserId()));

        signUpService.signupUserDupCheck(request);
        signUpService.insertUser(request);

        return ResponseEntity.ok(new SignUpResponse(true, "User registered successfully"));
    }

    /*********** 법인사용자 회원가입 *************/

    @PostMapping("/signUpUseorg")
    public ResponseEntity<SignUpResponse> signUpUseorg(
            @RequestParam(value = "fileData", required = false) MultipartFile file,
            @RequestParam(value = "useorgGb",required = false) String useorgGb,
            @RequestParam(value = "useorgNm",required = false) String useorgNm,
            @RequestParam(value = "useorgCtnt",required = false) String useorgCtnt,
            @RequestParam(value = "brn", required = false) String brn,
            @RequestParam(value = "useorgUserNm", required = false) String useorgUserNm,
            @RequestParam(value = "useorgUserEmail",required = false) String useorgUserEmail,
            @RequestParam(value = "useorgUserTel", required = false) String useorgUserTel,
            @RequestParam(value = "useorgUpload", required = false) String useorgUpload,
            @RequestParam(value = "useorgId", required = false) String useorgId,
            @RequestParam(value = "useorgPwd", required = false) String useorgPwd,
            @RequestParam(value = "userDi", required = false) String userDi,
            @RequestParam(value = "userResSeq", required = false) String userResSeq,
            @RequestParam(value = "useorgTel", required = false) String useorgTel,
            @RequestParam(value = "hbnUseYn", required = false) String hbnUseYn,
            @RequestParam(value = "hnwUseYn", required = false) String hnwUseYn,
            @RequestParam(value = "hcdUseYn", required = false) String hcdUseYn,
            @RequestParam(value = "hcpUseYn", required = false) String hcpUseYn,
            @RequestParam(value = "hlfUseYn", required = false) String hlfUseYn,
            @RequestParam(value = "hsvUseYn", required = false) String hsvUseYn,
            @RequestParam(value = "hmbUseYn", required = false) String hmbUseYn
    ) throws IOException, MessagingException {

        try {
            SignUpRequest.UseorgSignUpRequest request = new SignUpRequest.UseorgSignUpRequest();

            request.setUseorgGb(useorgGb);
            request.setUseorgNm(useorgNm);
            request.setUseorgCtnt(useorgCtnt);
            request.setBrn(brn);
            request.setUseorgUserNm(useorgUserNm);
            request.setUseorgUserEmail(useorgUserEmail);
            request.setUseorgUserTel(useorgUserTel);
            request.setUseorgUpload(useorgUpload);
            request.setUseorgId(useorgId.toLowerCase());
            request.setUseorgPwd(useorgPwd);
            request.setUseorgTel(useorgTel);
            request.setHbnUseYn(hbnUseYn);
            request.setHnwUseYn(hnwUseYn);
            request.setHcdUseYn(hcdUseYn);
            request.setHcpUseYn(hcpUseYn);
            request.setHlfUseYn(hlfUseYn);
            request.setHsvUseYn(hsvUseYn);
            request.setHmbUseYn(hmbUseYn);
            request.setRegUser(request.getUseorgUserNm());
            request.setRegId(request.getUseorgId());
            request.setUserDi(userDi);
            request.setUserResSeq(userResSeq);

             //         운영서버 반영
             if(StringUtils.equals(request.getUseorgGb(), "SOLE") && !StringUtils.equals(request.getUseorgUserTel(), "0123456789") && StringUtils.isNotBlank(request.getUserDi())){    // 테스트서버용 예외처리
                 if(request.getUserDi().length() != 64) {
                     log.error(messageSource.getMessage("E121"));
                     throw new BusinessException("E121",messageSource.getMessage("E121"));
                 }
             }

            SignUpRequest.UserSingnUpRequest sign = new SignUpRequest.UserSingnUpRequest();
            sign.setUserId(request.getUseorgId());
            sign.setUserEmail(request.getUseorgUserEmail());
            signUpService.signupUseorgEmailDupCheckWhenSignUp(sign);

            UserRequest.UserDupCheckRequest dupCheckRequest = new UserRequest.UserDupCheckRequest();
            dupCheckRequest.setUserId(request.getUseorgId().toLowerCase());
            signUpService.userIdDupCheck(dupCheckRequest);

            signUpService.insertUseorg(request, file);

            return ResponseEntity.ok(new SignUpResponse(true, "Coompany Registered successfully"));

        } catch (BusinessException e) {
            log.error("signUpUseorg 비즈니스 에러" + e);
            throw e;
        }catch(Exception e){
            log.error("signUpUseorg Exception 에러" + e);
            throw new BusinessException("E025",messageSource.getMessage("E025"));
        }
    }

    /*********** 개인회원 아이디 찾기 *************/
    @PostMapping("/searchUserId")
    public ResponseEntity<?> searchUserId(@Valid @RequestBody UserVO user) throws Exception {
        settingService.searchUserId(user);
        return ResponseEntity.ok(true);
    }

    /*********** 사업자회원 아이디 찾기 *************/
    @PostMapping("/searchUserOrgId")
    public ResponseEntity<?> searchUserOrgId(@Valid @RequestBody UseorgVO user) throws Exception {
        settingService.searchUserOrgId(user);
        return ResponseEntity.ok(true);
    }

    /*********** 개인회원 비밀번호 찾기 *************/
    @PostMapping("/searchUserPassword")
    public ResponseEntity<?> searchUserPassword(@Valid @RequestBody UserVO user) throws Exception {
        settingService.searchUserPassword(user);
        return ResponseEntity.ok(true);
    }

    /*********** 사업자회원 비밀번호 찾기 *************/
    @PostMapping("/searchOrgPassword")
    public ResponseEntity<?> searchOrgPassword(@Valid @RequestBody UseorgVO user) throws Exception {
        settingService.searchOrgPassword(user);
        return ResponseEntity.ok(true);
    }

    /***********  개인회원 비밀번호변경 *************/
    @PostMapping("/newUserPwd")
    public ResponseEntity<?> setUserPwd(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.UserPwdUpdateRequest request) {

        if (currentUser != null) {
            if(!currentUser.getUserId().equals(request.getUserId())) {
                log.error("비밀번호 변경 id 조작 가능성 감지");
                throw new BusinessException("E026","잘못된 요청입니다.");
            }
        }

        // 비밀번호 검증
        List<UserPwHisVO> userList = settingRepository.getUserIdPw(request.getUserId());

        if (userList != null) {
            for (UserPwHisVO userData : userList) {
                if(!commonUtil.compareWithShaStringsPw(request.getUserPwd(), userData.getUserPwd())) {
                    log.error("비밀번호 검증에러 [불일치]");
                    throw new BusinessException("E112", messageSource.getMessage("E112"));
                }
            }

            settingService.setUserPwd(request);
            return ResponseEntity.ok(new SignUpResponse(true, "User_Pwd_Update successfully"));
        } else {
            log.error(messageSource.getMessage("L005"));
            throw new BusinessException("L005", messageSource.getMessage("L005"));
        }
    }

    /***********  사업자회원 비밀번호변경 *************/
    @PostMapping("/setOrgPwd")
    public ResponseEntity<?> setOrgPwd(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.OrgPwdUpdateRequest request) {

        if (currentUser != null) {
            if(!currentUser.getUserId().equals(request.getUseorgId())) {
                log.error("비밀번호 변경 id 조작 가능성 감지");
                throw new BusinessException("E026","잘못된 요청입니다.");
            }
        }

        // 비밀번호 검증
        List<UserPwHisVO> userList = settingRepository.getUserIdPw(request.getUseorgId());

        if (userList != null) {
            for (UserPwHisVO userData : userList) {
                if(!commonUtil.compareWithShaStringsPw(request.getUseorgPwd(), userData.getUserPwd())) {
                    log.error(messageSource.getMessage("E112"));
                    throw new BusinessException("E112", messageSource.getMessage("E112"));
                }
            }

            settingService.setOrgPwd(request);
        } else {
            log.error(messageSource.getMessage("L005"));
            throw new BusinessException("L005", messageSource.getMessage("L005"));
        }

        return ResponseEntity.ok(new SignUpResponse(true, "User_Pwd_Update successfully"));
    }

    /***********  관리자 비밀번호변경 *************/
    @PostMapping("/hfnUserPwdUpdate")
    public ResponseEntity<?> hfnUserUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserPwdUpdateRequest request) {

        if(!currentUser.getUserId().equals(request.getHfnId())) {
            log.error("비밀번호 변경 id 조작 가능성 감지");
            throw new BusinessException("잘못된 요청입니다.");
        }

        // 19.11.04 추가 - 자신의 비밀번호만 바꿀 수 있는 로직이므로 강제로 세팅합니다.
        request.setHfnId(currentUser.getUserId());

        // 비밀번호 검증
        List<UserPwHisVO> userList = settingRepository.getHfnIdPw(request.getHfnId());

        for (UserPwHisVO userData : userList) {
            if(!commonUtil.compareWithShaStringsPw(request.getUserPwd(), userData.getUserPwd())) {
                log.error("hfnUserPwdUpdate 비밀번호 검증 에러 [불일치]");
                throw new BusinessException("E112",messageSource.getMessage("E112"));
            }
        }

        if (request.getUserPwd() != null && !StringUtils.equals(request.getUserPwd(), "")) {
            settingService.passwordCheck(request.getUserPwd());
            request.setUserPwd(passwordEncoder.encode(request.getUserPwd()));
        }

        settingService.updateHfnUserPwd(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Hfn User Update successfully"));
    }

    /***********  개인회원 인증완료 *************/
    @PostMapping("/certUpdateUser")
    public ResponseEntity<?> certUpdateUser(@Valid @RequestBody UserVO user) throws BusinessException {
        return ResponseEntity.ok(settingService.certUpdateUser(user));
    }

    /***********  사업자회원 인증완료 *************/
    @PostMapping("/certUpdateUseorg")
    public ResponseEntity<?> certUpdateUseorg(@Valid @RequestBody UseorgVO user) {
        return ResponseEntity.ok(settingService.certUpdateUseorg(user));
    }

    /***********  가입완료 *************/
    @PostMapping("/authComplete")
    public ResponseEntity<?> authComplete(@Valid @RequestBody UserRequest.UserAuthRequest request) {
        return ResponseEntity.ok(settingService.authComplete(request));
    }

    /***********  이메일 재전송 *************/
    @PostMapping("/sendEmail")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody UserRequest.UserAuthRequest request) throws Exception {
        settingService.sendEmail(request);
        return ResponseEntity.ok(true);
    }

    /***********  토큰 시간 갱신 *************/
    @PostMapping("/tokenTimeContinue")
    public ResponseEntity<?> tokenTimeContinue(@Valid @RequestBody LoginRequest.tokenRefesh requestLogin) {
        return ResponseEntity.ok("");
    }
}
