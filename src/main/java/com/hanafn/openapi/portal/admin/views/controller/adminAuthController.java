package com.hanafn.openapi.portal.admin.views.controller;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.SignUpResponse;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.dto.HfnUserRequest;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.UserPwHisVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/adminAuth")
@Slf4j
public class adminAuthController {
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

    /***********  관리자 비밀번호변경 *************/
    @PostMapping("/hfnUserPwdUpdate")
    public ResponseEntity<?> hfnUserUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUserRequest.HfnUserPwdUpdateRequest request) {

        System.out.println("★userPrin:"+currentUser);
        // 비밀번호 검증
        List<UserPwHisVO> userList = settingRepository.getHfnIdPw(request.getHfnId());

        for (UserPwHisVO userData : userList) {
            if(!commonUtil.compareWithShaStringsPw(request.getUserPwd(), userData.getUserPwd())) {
                log.error("관리자 비밀번호 업데이트 검증에러[불일치]");
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
}
