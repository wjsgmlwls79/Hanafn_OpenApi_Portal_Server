package com.hanafn.openapi.portal.views.controller;

import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.service.StatsService;
import com.hanafn.openapi.portal.views.vo.UseorgVO;
import com.hanafn.openapi.portal.views.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final SettingService settingService;

    @PostMapping("/dashBoard")
    public ResponseEntity<?> dashBoard(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody DashBoardRequest request) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() != "ROLE_SYS_ADMIN") {
                request.setHfnCd(currentUser.getHfnCd());
                request.setUserKey(currentUser.getUserKey());
                break;
            }
        }
        DashBoardRsponse data = statsService.dashBoard(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/userorgDashBoard")
    public ResponseEntity<?> userorgDashBoard(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody DashBoardRequest.UseorgDashBoardRequest request) {
        request.setEntrCd(currentUser.getEntrCd());

        if ("ORGD".equals(currentUser.getUserType())) {
            request.setUserKey(request.getEntrCd());
        } else {
            request.setUserKey(currentUser.getUserKey());
        }

        DashBoardRsponse.UseorgDashBoardRsponse data = statsService.useorgDashBoard(request);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/apiStats")
    public ResponseEntity<?> apiStats(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody StatsRequest request) {

        StatsRsponse data = statsService.apiStats(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/useorgStats")
    public ResponseEntity<?> useorgStats(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody StatsRequest request) {

        StatsRsponse.UseorgStatsRsponse data = statsService.useorgStats(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/useorgAppDetailStats")
    public ResponseEntity<?> useorgAppDetailStats(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody StatsRequest.AppDetailStatsRequest request) {

        StatsRsponse.AppApiDetailStatsRsponse data = statsService.useorgAppDetailStats(request);

        return ResponseEntity.ok(data);
    }

}
