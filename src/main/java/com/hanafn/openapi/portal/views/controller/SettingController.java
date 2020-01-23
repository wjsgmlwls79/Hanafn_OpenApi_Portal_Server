package com.hanafn.openapi.portal.views.controller;

import com.hanafn.openapi.portal.cmct.HubCommunicator;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.file.FileService;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.SignUpRequest;
import com.hanafn.openapi.portal.security.dto.SignUpResponse;
import com.hanafn.openapi.portal.security.repository.RoleRepository;
import com.hanafn.openapi.portal.security.service.SignupService;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.AppsRepository;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class SettingController {

    @Autowired
    SettingService settingService;
    @Autowired
    SignupService signUpService;
    @Autowired
    SettingRepository settingRepository;
    @Autowired
    AppsRepository appsRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    MessageSourceAccessor messageSource;
    @Autowired
    CommonUtil commonUtil;
    @Autowired
    FileService fileService;
    @Autowired
    HubCommunicator hubCommunicator;

    @PostMapping("/findHfnUser")
    public ResponseEntity<?> findHfnUser(@Valid @RequestBody HfnInfoRequest request) {

        HfnUserRsponse data = settingService.selectMyHfnMember(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/useorg")
    public ResponseEntity<?> useorg(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest.UseorgDetailRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        try {
            UseorgVO data = settingService.selectUseorg(request);

            String useorgUpload = data.getUseorgUpload();
            if(!StringUtils.isBlank(useorgUpload)){
                int pos = useorgUpload.lastIndexOf('/');
                String result = useorgUpload.substring( pos + 1 );
                data.setUseorgUpload(result);
            }
            return ResponseEntity.ok(data);
        }catch(Exception e) {
            log.error("이용기관 정보 조회중 에러 : " + e.toString());
            throw new BusinessException("E120",messageSource.getMessage("E120"));
        }
    }

    @PostMapping("/useorgs")
    public ResponseEntity<?> useorgList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest request) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() != "ROLE_SYS_ADMIN") {
                request.setSearchHfnCd(currentUser.getHfnCd());
                break;
            }
        }

        UseorgRsponsePaging data = settingService.selectUseorgListPaging(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/brnDupCheck")
    public ResponseEntity<?> brnDupCheck(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest.UseorgDupCheckRequest request) {
        UseorgRsponse.UseorgDupCheckResponse data = settingService.useorgBrnDupCheck(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/useorgRegist")
    public ResponseEntity<?> useorgRegist(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest.UseorgRegistRequest request) {

        request.setRegUserName(currentUser.getUsername());
        request.setRegUserId(currentUser.getUserKey());

        settingService.insertUseorg(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Useorg registered successfully"));
    }

    @PostMapping("/useorgUpdate")
    public ResponseEntity<?> useorgUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest.UseorgUpdateRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        if (request.getNewUseorgPwd() != null && !StringUtils.equals(request.getNewUseorgPwd(), "")) {
            // 비밀번호 검증
            List<UserPwHisVO> userList = settingRepository.getUseorgPw(currentUser.getUserKey());

            for (UserPwHisVO userData : userList) {
                if(!commonUtil.compareWithShaStringsPw(request.getNewUseorgPwd(), userData.getUserPwd())) {
                    log.error(messageSource.getMessage("E112"));
                    throw new BusinessException("E112",messageSource.getMessage("E112"));
                }
            }
        }

        request.setModUser(currentUser.getUsername());
        request.setModUserId(currentUser.getUserKey());

        if (request.getNewUseorgPwd() != null && !StringUtils.equals(request.getNewUseorgPwd(), "")) {
            request.setNewUseorgPwd(passwordEncoder.encode(request.getNewUseorgPwd()));
            UserLoginRequest.UpdatePwdRequest userLoginRequest = new UserLoginRequest.UpdatePwdRequest();
            userLoginRequest.setUserKey(request.getUserKey());
            userLoginRequest.setNewPwd(request.getNewUseorgPwd());
            settingService.updateUserLoginPwd(userLoginRequest);
        }

        request.setHfnCd(currentUser.getHfnCd());   // 수정하는 사람의 HFNCD 기준

        SignUpRequest.UserSingnUpRequest sign = new SignUpRequest.UserSingnUpRequest();

        sign.setUserId(request.getUseorgId());
        sign.setUserEmail(request.getUseorgUserEmail());

        signUpService.signupUseorgEmailDupCheck(sign);
        settingService.updateUseorg(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Useorg update successfully"));
    }


    @PostMapping("/useorgStatCdChange")
    public ResponseEntity<?> useorgStatCdChange(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest.UseorgStatCdChangeRequest request) {

        request.setRegUserName(currentUser.getUsername());
        request.setRegUserId(currentUser.getUserKey());

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        settingService.updateUseorgStatCdChange(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Useorg_Stat_Cd Change successfully"));
    }

    @PostMapping("/useorgsAll")
    public ResponseEntity<?> useorgsAll(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest request) {

        UseorgRsponse data = settingService.selectUseorgAllList(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/useorgsAllByHfn")
    public ResponseEntity<?> useorgsAllByHfn(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest request) {

        UseorgRsponse data = settingService.selectUseorgAllListByHfn(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/useorgDownload")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
    public ResponseEntity<Resource> useorgDownload(@CurrentUser UserPrincipal currentUser, HttpServletRequest httpServletRequest, HttpServletResponse response, @RequestParam String userKey) {
        UseorgRequest.UseorgDetailRequest request = new UseorgRequest.UseorgDetailRequest();
        request.setUserKey(userKey);
        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        String fileName = settingService.selectUseorg(request).getUseorgUpload();
        log.debug("★Filename:"+fileName);
        Resource resource = null;

        try {
            resource = fileService.loadFileAsResource(fileName);
        } catch (Exception e) {
            log.error("이용기관 사업증 다운로드 에러 : " + e.toString());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

        String contentType = null;

        contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // 이용자 포탈 - API 제휴관리 관련
    @PostMapping("/useorgHfnAplv")
    public ResponseEntity<?> useorgHfnAplv
    (          @CurrentUser UserPrincipal currentUser,
               @RequestParam(value = "fileData", required = false) MultipartFile file,
               @RequestParam("userKey") String userKey,
               @RequestParam("useorgNm") String useorgNm,
               @RequestParam("useorgUserNm") String useorgUserNm,
               @RequestParam("useorgCtnt") String useorgCtnt,
               @RequestParam("useorgId") String useorgId,
               @RequestParam("hfnCd") String hfnCd,
               @RequestParam("useorgUpload") String useorgUpload,
               @RequestParam("fileChangedCheck") boolean fileChangedCheck,
               @RequestParam("statCd") String statCd
    ) throws IOException {
        UseorgRequest.HfnAplvRequest request = new UseorgRequest.HfnAplvRequest();
        request.setUserKey(userKey);
        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        request.setUseorgNm(useorgNm);
        request.setUseorgUserNm(useorgUserNm);
        request.setUseorgCtnt(useorgCtnt);
        request.setUseorgId(useorgId);
        request.setHfnCd(hfnCd);
        request.setUseorgUpload(useorgUpload);
        request.setStatCd(statCd);

        settingService.useorgHfnAplv(request, file, fileChangedCheck);

        return ResponseEntity.ok(new SignUpResponse(true, "Hfn Aplv successfully"));
    }

    /*
     * ******************************사용자******************************
     * */

    @PostMapping("/user")
    public ResponseEntity<?> user(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.UserDetailRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(), "userPortal")) {
            request.setUserKey(currentUser.getUserKey());
        }
        UserVO data = settingService.selectUser(request);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/users")
    public ResponseEntity<?> userList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest request) {
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities){
            if(ga.getAuthority().contains("ROLE_SYS_ADMIN") || ga.getAuthority().contains("ROLE_HFN_ADMIN")){
                if(!ga.getAuthority().contains("ROLE_SYS_ADMIN"))
                {
                    request.setSearchHfnCd(currentUser.getHfnCd());
                }
                request.setUserKey(currentUser.getUserKey());
                UserRsponsePaging data = settingService.selectUserListPaging(request);

                return ResponseEntity.ok(data);
            }
        }

        log.error(messageSource.getMessage("E021"));
        throw new BusinessException("E021",messageSource.getMessage("E021"));
    }

    @PostMapping("/userUpdate")
    public ResponseEntity<?> userUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.UserUpdateRequest request) throws Exception {

        // 현재 세션의 user_key, hfnCd로 강제 변경
//        request.setUserKey(currentUser.getUserKey());

        if (!currentUser.getUserKey().equals(request.getUserKey())) {
            log.error(messageSource.getMessage("C001"));
            throw new BusinessException("C001",messageSource.getMessage("C001"));
        }

        if(request.getNewUserPwd() != null && !StringUtils.equals(request.getNewUserPwd(), "")){

            // 비밀번호 검증
            List<UserPwHisVO> userList = settingRepository.getUseorgPw(currentUser.getUserKey());

            for (UserPwHisVO userData : userList) {
                if(!commonUtil.compareWithShaStringsPw(request.getNewUserPwd(), userData.getUserPwd())) {
                    log.error(messageSource.getMessage("E112"));
                    throw new BusinessException("E112",messageSource.getMessage("E112"));
                }
            }

            request.setNewUserPwd(passwordEncoder.encode(request.getNewUserPwd()));

            // 패스워드 NotNull 일때만 변경
            UserLoginRequest.UpdatePwdRequest userLoginRequest = new UserLoginRequest.UpdatePwdRequest();
            userLoginRequest.setUserKey(request.getUserKey());
            userLoginRequest.setNewPwd(request.getNewUserPwd());
            userLoginRequest.setUserId(request.getUserId());
            settingService.updateUserLoginPwd(userLoginRequest);
        }

        request.setModUserName(currentUser.getUsername());
        request.setModUserId(currentUser.getUserKey());

        SignUpRequest.UserSingnUpRequest sign = new SignUpRequest.UserSingnUpRequest();

        sign.setUserId(request.getUserId());
        sign.setUserEmail(request.getUserEmail());

        signUpService.signupUserDupCheckUpdate(sign);

        settingService.updateUser(request);

        return ResponseEntity.ok(new SignUpResponse(true, "User Update successfully"));
    }

    @PostMapping("/userStatCdChange")
    public ResponseEntity<?> userStatCdChange(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.UserStatCdChangeRequest request) {
        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        settingService.updateUserStatCdChange(request);

        return ResponseEntity.ok(new SignUpResponse(true, "User_Stat_Cd Change successfully"));
    }

    @PostMapping("/userPwdUpdate")
    public ResponseEntity<?> userPwdUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.UserPwdUpdateRequest request) {

        request.setRegUserName(currentUser.getUsername());
        request.setUserKey(currentUser.getUserKey());
        settingService.userPwdUpdate(request);

        return ResponseEntity.ok(new SignUpResponse(true, "User_Pwd_Update successfully"));
    }

    @PostMapping("/userPwdAndTosUpdate")
    public ResponseEntity<?> userPwdAndTosUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.UserPwdAndTosUpdateRequest request) {

        request.setRegUserName(currentUser.getUsername());
        request.setUserKey(currentUser.getUserKey());
        settingService.userPwdAndTosUpdate(request);

        return ResponseEntity.ok(new SignUpResponse(true, "User_Pwd_And_Tos_Update successfully"));
    }

    @PostMapping("/getUserWithdraw")
    public ResponseEntity<?> secedeUser(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest request) {
        UserWithdrawVO userWithdrawVO = settingService.selectUserWithdraw(request);
        return ResponseEntity.ok(userWithdrawVO);
    }

    @PostMapping("/secedeUser")
    public ResponseEntity<?> secedeUser(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.UserSecedeRequest request) {
        request.setUserKey(currentUser.getUserKey());
        settingService.secedeUser(request);
        return ResponseEntity.ok(new SignUpResponse(true, "User_Pwd_Update successfully"));
    }

    @PostMapping("/secedeUseorg")
    public ResponseEntity<?> secedeUseorg(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest.UseorgSecedeRequest request) {
        request.setUserKey(currentUser.getUserKey());
        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        request.setUserId(currentUser.getUserId());
        request.setUserNm(currentUser.getUsername());
        settingService.secedeUseorg(request);
        return ResponseEntity.ok(new SignUpResponse(true, "User_Pwd_Update successfully"));
    }

    @PostMapping("/tmpPwdIssue")
    public ResponseEntity<?> tmpPwdIssue(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.UserTmpPwdUpdateRequest request) {

         /*
        request.setRegUserName(currentUser.getUsername());
        UserRsponse.UserTmpPwdIssueResponse data = settingService.tmpPwdIssue(request);

        return ResponseEntity.ok(data);
         */
         request.setRegUserId(currentUser.getUserKey());

         Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
         for(GrantedAuthority ga : authorities){
             if(ga.getAuthority().contains("ROLE_SYS_ADMIN") || ga.getAuthority().contains("ROLE_HFN_ADMIN")){
                 request.setRegUserName(currentUser.getUsername());
                 UserRsponse.UserTmpPwdIssueResponse data = settingService.tmpPwdIssue(request);

                 return ResponseEntity.ok(data);
             }
         }

        log.error(messageSource.getMessage("E021"));
         throw new BusinessException("E021",messageSource.getMessage("E021"));
    }

    /*
     * ******************************승인******************************
     * */

    @PostMapping("/aplv")
    public ResponseEntity<?> aplv(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AplvRequest.AplvDetailRequest request) {
        request.setProcId(currentUser.getUserKey());
        request.setProcUser(currentUser.getUsername());
//        request.setHfnCd(currentUser.getHfnCd());
        AplvRsponse.AplvDetilResponse data = settingService.selectAplv(request);

        return ResponseEntity.ok(data);
    }

    // 승인 리스트 로드
    @PostMapping("/aplvs")
    public ResponseEntity<?> aplvList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AplvRequest request) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() != "ROLE_SYS_ADMIN") {
                request.setSearchHfnCd(currentUser.getHfnCd());
                break;
            }
        }

        request.setProcId(currentUser.getUseorgKey());
        request.setProcUser(currentUser.getUsername());
        AplvRsponsePaging data = settingService.selectAplvListPaging(request);

        return ResponseEntity.ok(data);
    }

    // 결재자가 승인버튼 클릭시 처리
    @PostMapping("/aplvApproval")
    public ResponseEntity<?> aplvApproval(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AplvRequest.AplvApprovalRequest request) throws Exception {

        //request.setRegUserName(currentUser.getUsername());
        request.setProcUser(currentUser.getUsername());
        request.setProcId(currentUser.getUserKey());
        request.setHfnCd(currentUser.getHfnCd());
        settingService.aplvApproval(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Aplv Approval successfully"));
    }

    @PostMapping("/aplvReject")
    public ResponseEntity<?> aplvReject(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AplvRequest.AplvRejectRequest request) throws Exception {

        request.setRegUserName(currentUser.getUsername());
        request.setProcUser(currentUser.getUsername());
        request.setProcId(currentUser.getUserKey());
        request.setHfnCd(currentUser.getHfnCd());
        settingService.aplvReject(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Aplv Reject successfully"));
    }

    /*
     * ******************************관계사사용자******************************
     * */

    @PostMapping("/hfnUserById")
    public ResponseEntity<?> hfnUserById(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserDetailRequest request) {

        HfnUserVO data = settingService.selectHfnUserById(request);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/hfnUser")
    public ResponseEntity<?> hfnUser(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserDetailRequest request, HttpServletResponse httpServletResponse)  throws IOException {

        if (!StringUtils.equals(currentUser.getUserKey(), request.getUserKey())) {
            httpServletResponse.setHeader("ErrorCode", "expired");
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인하십시오.");
        }
        request.setUserKey(currentUser.getUserKey());
        HfnUserRsponse data = settingService.selectHfnUser(request);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/hfnUsers")
    public ResponseEntity<?> userList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest hfnUserRequest) {
/*
        hfnUserRequest.setUserKey(currentUser.getUserKey());
        HfnUserRsponsePaging data = settingService.selectHfnUserListPaging(hfnUserRequest);

        return ResponseEntity.ok(data);
 */

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities){
            if(ga.getAuthority().contains("ROLE_SYS_ADMIN") || ga.getAuthority().contains("ROLE_HFN_ADMIN")){
                if(!ga.getAuthority().contains("ROLE_SYS_ADMIN"))
                {
                    hfnUserRequest.setSearchHfnCd(currentUser.getHfnCd());
                }
                hfnUserRequest.setUserKey(currentUser.getUserKey());
                HfnUserRsponsePaging data = settingService.selectHfnUserListPaging(hfnUserRequest);

                return ResponseEntity.ok(data);
            }
        }

        log.error(messageSource.getMessage("E021"));
        throw new BusinessException("E021",messageSource.getMessage("E021"));
    }

    @PostMapping("/hfnUserRegist")
    public ResponseEntity<?> hfnUserRegist(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserRegistRequest request) {

        //패드워드 유효성 검사(영문자 대문자 + 숫자 8~16자리)
        settingService.passwordCheck(request.getUserPwd());

        request.setTmpPwd(passwordEncoder.encode(request.getUserPwd()));
        request.setUserPwd(passwordEncoder.encode(request.getUserPwd()));

        request.setRegUserName(currentUser.getUsername());
        request.setRegUserId(currentUser.getUserKey());
        settingService.insertHfnUser(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Hfn User registered successfully"));
    }

    @PostMapping("/hfnIdDupCheck")
    public ResponseEntity<?> userHfnIdDupCheck(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserDupCheckRequest request) {
        UserRsponse.UserDupCheckResponse data = settingService.hfnIdDupCheck(request);

        if (StringUtils.equals(data.getUserIdDupYn(), "Y")) {
            log.error(messageSource.getMessage("E028"));
            throw new BusinessException("E028",messageSource.getMessage("E028"));
        }
        return ResponseEntity.ok(data);
    }

    @PostMapping("/hfnUserUpdate")
    public ResponseEntity<?> hfnUserUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserUpdateRequest request) {

        if (request.getNewUserPwd() != null && !StringUtils.equals(request.getNewUserPwd(), "")) {
            settingService.passwordCheck(request.getUserPwd());
            settingService.passwordCheck(request.getNewUserPwd());

            request.setNewUserPwd(passwordEncoder.encode(request.getNewUserPwd()));
        }

        request.setRegUserName(currentUser.getUsername());
        request.setRegUser(currentUser.getUserKey());
        settingService.updateHfnUser(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Hfn User Update successfully"));
    }

    @PostMapping("/UserInfoUpdate")
    public ResponseEntity<?> UserInfoUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserUpdateRequest request) {

        /*
        if (request.getNewUserPwd() != null && !StringUtils.equals(request.getNewUserPwd(), "")) {
                settingService.passwordCheck(request.getUserPwd());
                settingService.passwordCheck(request.getNewUserPwd());

                request.setNewUserPwd(passwordEncoder.encode(request.getNewUserPwd()));
            }

            request.setRegUserName(currentUser.getUsername());
            request.setRegUser(currentUser.getUserKey());
            settingService.updateHfnUser(request);

            return ResponseEntity.ok(new SignUpResponse(true, "Hfn User Update successfully"));
         */

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        if(request.getRoleCd().equals("1")){
            for(GrantedAuthority ga : authorities) {
                if(ga.getAuthority() != ("ROLE_SYS_ADMIN")) {
                    log.error(messageSource.getMessage("C001"));
                    throw new BusinessException("C001",messageSource.getMessage("C001"));
                }
            }
        } else if (request.getRoleCd().equals("2")) {
            for(GrantedAuthority ga : authorities) {
                if((ga.getAuthority() != ("ROLE_SYS_ADMIN")) && (ga.getAuthority() != ("ROLE_HFN_ADMIN"))) {
                    log.error(messageSource.getMessage("C001"));
                    throw new BusinessException("C001",messageSource.getMessage("C001"));
                }
            }
        } else if (request.getRoleCd().equals("3")) {

        } else {
            log.error(messageSource.getMessage("C001"));
            throw new BusinessException("C001",messageSource.getMessage("C001"));
        }

        UserRequest.UserDetailRequest userDetailRequest = new UserRequest.UserDetailRequest();
        userDetailRequest.setUserKey(currentUser.getUserKey());
        userDetailRequest.setHfnCd(currentUser.getHfnCd());
        userDetailRequest.setHfnId(currentUser.getUserId());
        UserVO user = settingRepository.selectHfnUserPwd(userDetailRequest);

        if(passwordEncoder.matches(request.getUserPwd(), user.getUserPwd())) {

            if (request.getNewUserPwd() != null && !StringUtils.equals(request.getNewUserPwd(), "")) {
                settingService.passwordCheck(request.getUserPwd());
                settingService.passwordCheck(request.getNewUserPwd());

                // 비밀번호 검증
                List<UserPwHisVO> userList = settingRepository.getUserIdPw(userDetailRequest.getHfnId());

                if (userList != null) {
                    for (UserPwHisVO userData : userList) {
                        if(!commonUtil.compareWithShaStringsPw(request.getNewUserPwd(), userData.getUserPwd())) {
                            log.error("비밀번호 검증에러 [불일치]");
                            throw new BusinessException("E112", messageSource.getMessage("E112"));
                        }
                    }

                } else {
                    log.error(messageSource.getMessage("L005"));
                    throw new BusinessException("L005", messageSource.getMessage("L005"));
                }

                request.setNewUserPwd(passwordEncoder.encode(request.getNewUserPwd()));
            }

            request.setRegUserName(currentUser.getUsername());
            request.setRegUser(currentUser.getUserKey());
            request.setUserKey(currentUser.getUserKey());
            request.setHfnCd(currentUser.getHfnCd());
            request.setHfnId(currentUser.getUserId());
            settingService.updateHfnUser(request);

            return ResponseEntity.ok(new SignUpResponse(true, "Hfn User Update successfully"));
        } else {
            //return ResponseEntity.ok(new SignUpResponse(false, "User Info Update Failed"));
            log.error(messageSource.getMessage("C001"));
            throw new BusinessException("L001",messageSource.getMessage("L001"));
        }
    }

    @PostMapping("/HfnUserStatCdChange")
    public ResponseEntity<?> hfnUserStatCdChange(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserStatCdChangeRequest request) {

        request.setRegUserName(currentUser.getUsername());
        request.setRegUserId(currentUser.getUserKey());
        settingService.updateHfnUserStatCdChange(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Hfn User_Stat_Cd Change successfully"));
    }

    @PostMapping("/hfnUserPwdUpdate")
    public ResponseEntity<?> hfnUserPwdUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserPwdUpdateRequest request) {

        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        request.setUserKey(currentUser.getUserKey());

        // 비밀번호 검증
        List<UserPwHisVO> userList = settingRepository.getUseorgPw(request.getUserKey());

        for (UserPwHisVO userData : userList) {
            if(!commonUtil.compareWithShaStringsPw(request.getUserPwd(), userData.getUserPwd())) {
                log.error(messageSource.getMessage("E112"));
                throw new BusinessException("E112",messageSource.getMessage("E112"));
            }
        }

        settingService.hfnUserPwdUpdate(request);

        return ResponseEntity.ok(new SignUpResponse(true, "User_Pwd_Update successfully"));
    }

    @PostMapping("/hfnUserPwdAndTosUpdate")
    public ResponseEntity<?> hfnUserPwdAndTosUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserPwdAndTosUpdateRequest request) {

        request.setRegUserName(currentUser.getUsername());
        request.setUserKey(currentUser.getUserKey());
        settingService.hfnUserPwdAndTosUpdate(request);

        return ResponseEntity.ok(new SignUpResponse(true, "User_Pwd_And_Tos_Update successfully"));
    }

    /** 대직자 **/
    @PostMapping("/hfnAltUpdate")
    public ResponseEntity<?> hfnAltUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserUpdateRequest request) {
        request.setRegUserName(currentUser.getUsername());
        request.setRegUser(currentUser.getUserKey());
        settingService.updateHfnAltInfo(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Update successfully"));
    }

    @PostMapping("/getHfnAltUsers")
    public ResponseEntity<?> getHfnAltUsers(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnAltUserRequest request) {
        return ResponseEntity.ok(settingService.getHfnAltUsers(request));
    }

    /** **/

    @PostMapping("/userAplvLevelInfo")
    public ResponseEntity<?> userAplvLevelInfo(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserAplvLevelRequest request) {

        HfnUserVO user = settingService.userAplvLevelInfo(request);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/aplvLineSetting")
    public ResponseEntity<?> aplvLineSetting(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AplvRequest.AplvLineSetRequest request) {
        request.setRegUser(currentUser.getUsername());
        request.setRegId(currentUser.getUserKey());
        settingService.aplvLineSetting(request);
        return ResponseEntity.ok(new SignUpResponse(true, "Aplv Line Setting successfully"));
    }

    /*** 개발자 등록 ***/
    @PostMapping("/regDeveloper")
    public ResponseEntity<?> regDeveloper(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.RegistDeveloperRequest request) {
        request.setModUser(currentUser.getUsername());
        request.setEntrCd(currentUser.getUseorgKey());
        request.setUserKey(currentUser.getUserKey());

        // 암호화
        try {
            // 휴대전화번호
            if (request.getUserTel() != null && !"".equals(request.getUserTel())) {
                String encryptedUserTel = AES256Util.encrypt(request.getUserTel());
                request.setUserTel(encryptedUserTel);
            }
        } catch ( Exception e ) {
            log.error(e.toString());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

        try {
            // 이메일
            if (request.getUserEmail() != null && !"".equals(request.getUserEmail())) {
                String encryptedUserEmail = AES256Util.encrypt(request.getUserEmail());
                request.setUserEmail(encryptedUserEmail);
                log.debug("암호화 - 이메일: {}", encryptedUserEmail);
            }
        } catch ( Exception e ) {
            log.error(e.toString());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

        settingService.checkDevDupCheck(request);
        settingService.checkDevExistsOtherUseorg(request);
        settingService.checkDevExists(request);

        UseorgVO useorgVO = settingRepository.selectUseorgByEntrCd(currentUser.getUseorgKey());
        request.setUserCompany(useorgVO.getUseorgNm());

        settingService.regDeveloper(request);
        return ResponseEntity.ok(new SignUpResponse(true, "Developer Register successfully"));
    }

    /*** 개발자 리스트 로딩 ***/
    @PostMapping("/selectDeveloperList")
    public ResponseEntity<?> selectDeveloperList(@CurrentUser UserPrincipal currentUser) {
        commonUtil.userKeyChangeIfDeveloper(currentUser);
        UserRequest.RegistDeveloperRequest request = new UserRequest.RegistDeveloperRequest();

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setEntrCd(currentUser.getUseorgKey()); // 개발자관련
        }

        UserRsponse.DeveloperListResponse data = settingService.selectDeveloperList(request);

        return ResponseEntity.ok(data);
    }

    /*** 개발자 삭제 ***/
    @PostMapping("/deleteDeveloper")
    public ResponseEntity<?> deleteDeveloper(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest.RegistDeveloperRequest request) {
        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setEntrCd(currentUser.getUseorgKey()); // 개발자관련
        }

        request.setModUser(currentUser.getUsername());
        request.setUserKey(currentUser.getUserKey());
        settingService.deleteDeveloper(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Developer Delete successfully"));
    }

    /*** 보안 체크리스트 파일 다운로드 ***/
    @PostMapping("/downloadChecklist")
    public ResponseEntity<?> downloadChecklist(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody FileDownloadRequest fileDownloadRequest) {
        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        File path = new File(".");

        if (StringUtils.equals(fileDownloadRequest.getName(), "A")) {
            //fileDownloadResponse.setUrl("");
            log.debug("project path = " + path.getAbsolutePath());
            String classPath = SettingController.class.getResource("").getPath();
            log.debug("class path = " + classPath);
            String filePath = classPath + "../../util/SecurityCheckList.txt";
            File test = new File(filePath);
            log.debug("file path = " + test.getAbsolutePath());
        }

        fileDownloadResponse.setName(path.getName());

        return ResponseEntity.ok(fileDownloadResponse);

    }

    /*** 결재선 목록 조회  ***/
    @PostMapping("/getAplLineList")
    public ResponseEntity<?> getAplLineList(@RequestBody HfnUserRequest.HfnUserAplvLevelRequest hfnUser) {
        System.out.println("1111111111");
        System.out.println(hfnUser.getHfnCd());
        return ResponseEntity.ok(settingService.getAplLineList(hfnUser));
    }

    //------------------------------------------------------------------------------------------------------
    // 개발자공간-API제휴신청
    //------------------------------------------------------------------------------------------------------
    @PostMapping("/apiJehuReg")
    public ResponseEntity<?> apiJehuReg(@CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "userId",       required = false) String userId,
            @RequestParam(value = "compNm",       required = false) String compNm,
            @RequestParam(value = "qnaType",      required = false) String qnaType,
            @RequestParam(value = "userNm",       required = false) String userNm,
            @RequestParam(value = "userTel",      required = false) String userTel,
            @RequestParam(value = "apiNm",        required = false) String apiNm,
            @RequestParam(value = "hfnCd",        required = false) String hfnCd,
            @RequestParam(value = "reqTitle",     required = false) String reqTitle,
            @RequestParam(value = "reqCtnt",      required = false) String reqCtnt,
            @RequestParam(value = "fileData1",    required = false) MultipartFile upLoadFileData1,
            @RequestParam(value = "fileData2",    required = false) MultipartFile upLoadFileData2
    ) throws IOException {

        try {
            ApiJehuRequest request = new ApiJehuRequest();
            String fileData1 = "";
            String fileData2 = "";

            if ( upLoadFileData1 != null ) {
                fileData1 = fileService.fileSave(upLoadFileData1);
            }
            if ( upLoadFileData2 != null ) {
                fileData2 = fileService.fileSave(upLoadFileData2);
            }

            request.setUserKey(currentUser.getUserKey());
            request.setUserId(currentUser.getUserId());
            request.setCompNm(compNm);
            request.setQnaType(qnaType);
            request.setUserNm(userNm);
            request.setUserTel(userTel);
            request.setApiNm(apiNm);
            request.setHfnCd(hfnCd);
            request.setReqTitle(reqTitle);
            request.setReqCtnt(reqCtnt);
            request.setFileData1(fileData1);
            request.setFileData2(fileData2);

            settingService.insertApiJehu(request);

            return ResponseEntity.ok(new SignUpResponse(true, "ApiJehuReg successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "ApiJehuReg Fail"));
        }
    }

    //------------------------------------------------------------------------------------------------------
    // 개발자공간-공지사항
    //------------------------------------------------------------------------------------------------------


    @PostMapping("/selectQnaList")
    public ResponseEntity<?> selectQnaList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody QnaRequest qnaRequest) {

        if(currentUser.getSiteCd().equals("adminPortal")) {
            if(!commonUtil.superAdminCheck(currentUser)) {
                if(!qnaRequest.getHfnCd().equals(currentUser.getHfnCd())) {
                    log.info("selectQnaList hfnCd 변경가능성 감지");
                    throw new BusinessException("E026","잘못된 요청입니다.");
                }
            }

            try {
                boolean isAdmin = false;
                Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
                for(GrantedAuthority ga : authorities){
                    if(ga.getAuthority().contains("ROLE_SYS_ADMIN")){
                        isAdmin = true;
                        break;
                    }
                }
                if (!isAdmin) {
                    qnaRequest.setHfnCd(currentUser.getHfnCd());
                }
                QnaResponse data = settingService.selectQnaList(qnaRequest);
                return ResponseEntity.ok(data);
            }catch(Exception e){
                return ResponseEntity.ok(new SignUpResponse(false, "QnA search Fail"));
            }
        } else if (currentUser.getSiteCd().equals("userPortal")) {
            try {

                if (!currentUser.getUserKey().equals(qnaRequest.getUserKey())) {
                    log.error("올바른 사용자정보가 아닙니다.");
                    throw new BusinessException("E026","올바른 사용자정보가 아닙니다.");
                }

                QnaResponse data = settingService.selectQnaList(qnaRequest);
                return ResponseEntity.ok(data);
            }catch(Exception e){
                return ResponseEntity.ok(new SignUpResponse(false, "QnA search Fail"));
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "올바르지 않은 접근입니다."));
    }

    @PostMapping("/detailQna")
    public ResponseEntity<?> detailQna(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody QnaRequest qnaRequest) {

        try {
            QnaResponse data = settingService.detailQna(qnaRequest);
            return ResponseEntity.ok(data);
        } catch(Exception e) {
            return ResponseEntity.ok(new SignUpResponse(false, "Qna search Fail"));
        }
    }

    @PostMapping("/qnaDownload")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
    public ResponseEntity<Resource> qnaDownload(@RequestParam String url) {

        String fileName = url;
        Resource resource = null;
        String realFileName = "";

        try {
            if (!fileName.startsWith("/openapi/upload/")) {
                log.error("파일경로 에러 : " + fileName);
                throw new BusinessException("E026",messageSource.getMessage("E026"));
            }
            resource = fileService.loadFileAsResource(fileName);
            realFileName = URLEncoder.encode(resource.getFilename(), "utf-8");
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + realFileName + "\"")
                .body(resource);
    }

    @PostMapping("/deleteQna")
    public ResponseEntity<?> deleteQna(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody QnaRequest.QnaDeleteRequest qnaDeleteRequest) {

        try {
            QnaRequest qnaRequest = new QnaRequest();
            qnaRequest.setModId(currentUser.getUserKey());

            for(String seqNo : qnaDeleteRequest.getSeqList())
            {
                qnaRequest.setSeqNo(seqNo);
                settingRepository.deleteQna(qnaRequest);
            }

            return ResponseEntity.ok(new SignUpResponse(true, "Qna Delete successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "Qna Delete Fail"));
        }
    }

    @PostMapping("/updateAnswer")
    public ResponseEntity<?> updateAnswer(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody QnaRequest qnaRequest) {

        try {
            qnaRequest.setModId(currentUser.getUserKey());
            settingRepository.updateAnswer(qnaRequest);
            return ResponseEntity.ok(new SignUpResponse(true, "Answer Update successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "Answer Update Fail"));
        }
    }

    @PostMapping("/downloadAttachment")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
    public ResponseEntity<Resource> downloadAttachment(@CurrentUser UserPrincipal currentUser, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @RequestParam String seqNo, @RequestParam String att) {
        QnaRequest req = new QnaRequest();
        req.setSeqNo(seqNo);

        String fileName = null;
        if(StringUtils.equals(att, "att1")) {
            fileName = settingRepository.selectQnaAttachment01(req);
        } else if(StringUtils.equals(att, "att2")) {
            fileName = settingRepository.selectQnaAttachment02(req);
        }
        log.debug("@@FilePath = " + fileName);

        Resource resource = null;
        try {
            resource = fileService.loadFileAsResource(fileName);
            log.debug("File is " + fileName);
        } catch(Exception e){
            log.error("File exist error :" + fileName );
            throw new BusinessException("E026","파일이 존재하지 않습니다.");
        }

        String contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/downloadManual")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition "})
    public ResponseEntity<Resource> downloadManual(@CurrentUser UserPrincipal currentUser, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if(StringUtils.equals(currentUser.getSiteCd(), "adminPortal")) {
            Resource resource = null;

            try {
                String filePath = "/openapi/upload/manual/HFN_OpenAPI_AdminPortal_Manual_v3.0.doc";
                resource = fileService.loadFileAsResource(filePath);
            } catch(Exception e) {
                log.error("@@ Manual exist error" + e.toString());
                throw new BusinessException("E026","관리자포탈 이용 매뉴얼 다운로드 에러");
            }

            String contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            log.error("허용되지 않는 접근입니다.");
            throw new BusinessException("E026","허용되지 않는 접근입니다.");
        }
    }

    @PostMapping("/hfnLoginLockReleasse")
    public ResponseEntity<?> hfnLoginLockRelease(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserLoginRequest.HfnLoginLockCheckRequest hfnLoginLockCheckRequest) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                try {
                    settingRepository.hfnLoginLockRelease(hfnLoginLockCheckRequest);
                    return ResponseEntity.ok(new SignUpResponse(true, "Login Lockk Release Success"));
                } catch (Exception e) {
                    return ResponseEntity.ok(new SignUpResponse(false, "Login Lockk Release fail"));
                }
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
    }

    @PostMapping("/userLoginLockRelease")
    public ResponseEntity<?> userLoginLockRelease(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                try {
                    settingRepository.userLoginLockRelease(userLoginLockCheckRequest);
                    return ResponseEntity.ok(new SignUpResponse(true, "Login Lockk Release Success"));
                } catch (Exception e) {
                    return ResponseEntity.ok(new SignUpResponse(false, "Login Lockk Release fail"));
                }
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
    }

    @PostMapping("/chargeDiscountRateList")
    public  ResponseEntity<?> chargeDiscountRateList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest.ApiDetailRequest request) {
        request.setUseFl("Y");
        List<RequestApiVO> requestApiList = settingRepository.getRequestList(request);

        return ResponseEntity.ok(requestApiList);
    }

    @PostMapping("/saveChargeDiscountRateList")
    public  ResponseEntity<?> saveChargeDiscountRateList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ChargeDiscountRateListRequest request) {
        request.setRegUser(currentUser.getUsername());
        request.setModUser(currentUser.getUsername());
        settingService.saveChargeDiscountRateList(request);

        return ResponseEntity.ok("");
    }

    @PostMapping("/fetchApi")
    public  ResponseEntity<?> fetchFeeAmount(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest.ApiDetailRequest request) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setApiId(request.getApiId());
        ApiVO apiInfo = appsRepository.selectApi(apiRequest);

        return ResponseEntity.ok(apiInfo);
    }

    @PostMapping("/deleteChargeDiscountRate")
    public  ResponseEntity<?> deleteChargeDiscountRate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody RequestApiVO request) {
        settingRepository.deleteChargeDiscountRate(request);

        return ResponseEntity.ok("");
    }

    /**
     * 개인 정보 해제 로그 등록
     * @param currentUser
     * @param request
     * @return
     */
    @PostMapping("/registerPrivacyReleaseLog")
    public ResponseEntity<?> registerPrivacyReleaseLog(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody PrivacyReleaseLogRequest request) {
        request.setRegId(currentUser.getUserKey());
        settingRepository.insertPrivacyReleaseLog(request);

        return ResponseEntity.ok("");
    }

    /** 인증 관련 메일 리스트 조회 **/
    @PostMapping("/selectCertMailList")
    public ResponseEntity<?> selectCertMailList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody SndCertMgntRequest request) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                try {
                    SndCertMgntResponse sndCertMgntResponse = settingService.selectCertMailList(request);
                    return ResponseEntity.ok(sndCertMgntResponse);
                } catch (Exception e) {
                    return ResponseEntity.ok(new SignUpResponse(false, "인증메일 리스트 조회 에러"));
                }
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));

    }

    @PostMapping("/detailCertMail")
    public ResponseEntity<?> detailCertMail(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody SndCertMgntRequest request) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                try {
                    SndCertMgntResponse sndCertMgntResponse = new SndCertMgntResponse();
                    CertMailVO certMail = settingRepository.detailCertMail(request);
                    sndCertMgntResponse.setCertMail(certMail);
                    return ResponseEntity.ok(sndCertMgntResponse);
                } catch (Exception e) {
                    return ResponseEntity.ok(new SignUpResponse(false, "인증메일 상세 조회 에러"));
                }
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
    }

    @PostMapping("/fetchBatchIdList")
    public ResponseEntity<?> fetchBatchIdList() {
        List<BatchLogVO> batchLogVOList = settingRepository.selectBatchIdList();

        return ResponseEntity.ok(batchLogVOList);
    }

    @PostMapping("/batchLogList")
    public ResponseEntity<?> fetchBatchLogList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody BatchLogListRequest request) {
        BatchLogListResponsePaging data = settingService.selectBatchLogListPaging(request);

        return ResponseEntity.ok(data);
    }

    /** 관리자 - 트랜잭션 관리 **/
    @PostMapping("/selectTrx")
    public ResponseEntity<?> selectTrx(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody TrxRequest request) {
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                try {
                    TrxResponse response = settingService.selectTrx(request);
                    return ResponseEntity.ok(response);
                } catch (Exception e) {
                    return ResponseEntity.ok(new SignUpResponse(false, "트랜잭션 리스트 조회 에러"));
                }
            }
        }
        return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
    }

    @PostMapping("/regTrx")
    public ResponseEntity<?> regTrx(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody TrxRequest request) {
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                request.setRegUser(currentUser.getUsername());
                try {
                    settingRepository.regTrx(request);
                } catch (Exception e) {
                    return ResponseEntity.ok(new SignUpResponse(false, "Transaction Register Fail"));
                }
                return ResponseEntity.ok(new SignUpResponse(true, "Transaction Register Success"));
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
    }

    @PostMapping("/detailTrx")
    public ResponseEntity<?> detailTrx(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody TrxRequest request) {
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                try {
                    TrxVO trx = settingRepository.detailTrx(request);
                    return ResponseEntity.ok(trx);
                } catch (Exception e) {
                    return ResponseEntity.ok(new SignUpResponse(false, "트랜잭션 상세 조회 에러"));
                }
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
    }

    @PostMapping("/updateTrx")
    public ResponseEntity<?> updateTrx(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody TrxRequest request) {
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() == "ROLE_SYS_ADMIN") {
                request.setModUser(currentUser.getUsername());
                try {
                    settingRepository.updateTrx(request);
                    return ResponseEntity.ok(new SignUpResponse(true, "Transaction Update Success"));
                } catch (Exception e) {
                    return ResponseEntity.ok(new SignUpResponse(false, "Transaction Update Fail"));
                }
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
    }

}
