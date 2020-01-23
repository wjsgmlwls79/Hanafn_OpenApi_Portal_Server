package com.hanafn.openapi.portal.admin.views.controller;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.SignUpResponse;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.dto.HfnUserRequest;
import com.hanafn.openapi.portal.views.dto.QnaRequest;
import com.hanafn.openapi.portal.views.dto.QnaResponse;
import com.hanafn.openapi.portal.views.dto.UserRequest;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/admin")
@Slf4j
public class adminSettingController {
    @Autowired
    SettingService settingService;
    @Autowired
    SettingRepository settingRepository;
    @Autowired
    MessageSourceAccessor messageSource;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CommonUtil commonUtil;

    @PostMapping("/selectQnaList")
    public ResponseEntity<?> selectQnaList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody QnaRequest qnaRequest) {

        if(currentUser.getSiteCd().equals("adminPortal")) {
            if(!commonUtil.superAdminCheck(currentUser)) {
                log.info(" selectQnaList hfnCd 자기 관계사로 변경처리" + qnaRequest.getHfnCd() + " => " + currentUser.getHfnCd());
                qnaRequest.setHfnCd(currentUser.getHfnCd());
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
                    throw new BusinessException("E026","올바른 사용자정보가 아닙니다.");
                    //★ message 추가필요
                }

                QnaResponse data = settingService.selectQnaList(qnaRequest);
                return ResponseEntity.ok(data);
            }catch(Exception e){
                return ResponseEntity.ok(new SignUpResponse(false, "QnA search Fail"));
            }
        }

        return ResponseEntity.ok(new SignUpResponse(false, "올바르지 않은 접근입니다."));
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
                    log.error("UserInfoUpdate 1 잘못된 조작입니다.");
                    throw new BusinessException("E026","잘못된 조작입니다.");
                    // mg추가필요
                }
            }
        } else if (request.getRoleCd().equals("2")) {
            for(GrantedAuthority ga : authorities) {
                if((ga.getAuthority() != ("ROLE_SYS_ADMIN")) && (ga.getAuthority() != ("ROLE_HFN_ADMIN"))) {
                    log.error("UserInfoUpdate 2 잘못된 조작입니다.");
                    throw new BusinessException("E026","잘못된 조작입니다.");
                    // mg추가필요
                }
            }
        } else if (request.getRoleCd().equals("3")) {

        } else {
            log.error("UserInfoUpdate else 잘못된 조작입니다.");
            throw new BusinessException("E026","잘못된 조작입니다.");
            // 추가필요
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
            log.error("UserInfoUpdate error");
            throw new BusinessException("L003",messageSource.getMessage("L003"));
        }
    }
}
