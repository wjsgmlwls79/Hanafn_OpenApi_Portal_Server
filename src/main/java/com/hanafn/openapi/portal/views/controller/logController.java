package com.hanafn.openapi.portal.views.controller;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.service.LogService;
import com.hanafn.openapi.portal.views.vo.ApiLogVO;
import com.hanafn.openapi.portal.views.vo.PortalLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class logController {

    private final LogService logService;
    private final MessageSourceAccessor messageSource;

    @PostMapping("/portalLog")
    public ResponseEntity<?> portalLog(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody PortalLogRequest.PortalLogDetailRequest request) {
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities){
            if(ga.getAuthority().contains("ROLE_SYS_ADMIN") || ga.getAuthority().contains("ROLE_HFN_ADMIN")){

                PortalLogVO data = logService.selectPortalLog(request);
                return ResponseEntity.ok(data);
            }
        }
        log.error(messageSource.getMessage("E021"));
        throw new BusinessException("E021",messageSource.getMessage("E021"));
    }
    
    @PostMapping("/portalLogs")
    public ResponseEntity<?> portalLogList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody PortalLogRequest request) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities){
            if(ga.getAuthority().contains("ROLE_SYS_ADMIN") || ga.getAuthority().contains("ROLE_HFN_ADMIN")){
                PortalLogRsponsePaging data = logService.selectPortalLogListPaging(request);
                return ResponseEntity.ok(data);
            }
        }

        log.error(messageSource.getMessage("E021"));
        throw new BusinessException("E021",messageSource.getMessage("E021"));
    }

    @PostMapping("/apiLog")
    public ResponseEntity<?> apiLog(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiLogRequest.ApiLogDetailRequest request) {

        ApiLogVO data = logService.selectApiLog(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/apiLogs")
    public ResponseEntity<?> apiLogList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiLogRequest request) {
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities){
            if(ga.getAuthority().contains("ROLE_SYS_ADMIN") || ga.getAuthority().contains("ROLE_HFN_ADMIN")){
                ApiLogRsponsePaging data = logService.selectApiLogListPaging(request);
                return ResponseEntity.ok(data);
            }
        }

        log.error(messageSource.getMessage("E021"));
        throw new BusinessException("E021",messageSource.getMessage("E021"));
    }
}
