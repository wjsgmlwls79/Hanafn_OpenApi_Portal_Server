package com.hanafn.openapi.portal.admin.views.controller;

import com.hanafn.openapi.portal.admin.views.dto.MenuRequest;
import com.hanafn.openapi.portal.admin.views.repository.adminRepository;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminMenuController {

    @Autowired
    CommonUtil commonUtil;

    @Autowired
    adminRepository adminRepository;

    @PostMapping("/webAccess")
    public ResponseEntity<?> getWebAccess(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody MenuRequest request) throws Exception {

        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        request.setUserKey( (currentUser != null) ? currentUser.getUserKey() : null);
        request.setUserIp(commonUtil.getIp(httpServletRequest));

        adminRepository.insertWebPageAccessLog(request);

        return ResponseEntity.ok("true");
    }
}
