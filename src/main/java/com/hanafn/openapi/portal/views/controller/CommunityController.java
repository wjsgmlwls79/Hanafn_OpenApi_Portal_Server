package com.hanafn.openapi.portal.views.controller;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.SignUpResponse;
import com.hanafn.openapi.portal.util.SSOUtil;
import com.hanafn.openapi.portal.views.dto.FaqRequest;
import com.hanafn.openapi.portal.views.dto.FaqResponse;
import com.hanafn.openapi.portal.views.dto.NoticeRequest;
import com.hanafn.openapi.portal.views.dto.NoticeResponse;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.PortalLogVO;
import com.initech.eam.api.NXNLSAPI;
import com.initech.eam.nls.CookieManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/community")
@Slf4j
@RequiredArgsConstructor
public class CommunityController {

    private final SettingService settingService;
    private final SettingRepository settingRepository;
    private final MessageSourceAccessor messageSource;

    @PostMapping("/selectNoticeList")
    public ResponseEntity<?> selectNoticeList(@Valid @RequestBody NoticeRequest.NoticeListRequest noticeListRequest, @CurrentUser UserPrincipal currentUser) {
        log.debug("###################### 개발자공간-공지사항LIST ######################");

        // 비로그인 시에는 정상 공지사항만 조회
        if(currentUser == null) {
            noticeListRequest.setStatCd("OK");
        }
        else { // 로그인 시, 관리자포탈에서 포탈관리자 일때만 정상 공지사항 및 삭제된 공지사항을 볼 수 있음
            if(StringUtils.equals(currentUser.getSiteCd(), "adminPortal")) {
                Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
                for(GrantedAuthority ga : authorities){
                    if(!ga.getAuthority().contains("ROLE_SYS_ADMIN")){
                        noticeListRequest.setStatCd("OK");
                        break;
                    }
                }
            } else { // 로그인 시 이용자 포탈에서는 정상 공지사항만 볼 수 있음
                noticeListRequest.setStatCd("OK");
            }
        }

        NoticeResponse.NoticeListResponse data = settingService.selectNoticeList(noticeListRequest);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/detailNotice")
    public ResponseEntity<?> detailNotice(@Valid @RequestBody NoticeRequest noticeRequest, @CurrentUser UserPrincipal currentUser) {

        NoticeResponse data;
        try {
            if(currentUser == null) {
                data = settingService.detailNotice("", noticeRequest);
            }
            else {
                data = settingService.detailNotice(currentUser.getSiteCd(), noticeRequest);
            }
        }catch(Exception e) {
            log.error("delete Noitcie error");
            throw new BusinessException("E020",messageSource.getMessage("E020"));
        }


        return ResponseEntity.ok(data);
    }

    @PostMapping("/insertNotice")
    public ResponseEntity<?> insertNotice(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody NoticeRequest noticeRequest) {

        try {
            noticeRequest.setRegId(currentUser.getUsername());
            noticeRequest.setRegUserId(currentUser.getUserKey());
            settingRepository.insertNotice(noticeRequest);

            return ResponseEntity.ok(new SignUpResponse(true, "Notice Regist successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "Notice Regist Fail"));
        }
    }

    @PostMapping("/deleteNotice")
    public ResponseEntity<?> deleteNotice(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody NoticeRequest noticeRequest) {

        try {
            noticeRequest.setModId(currentUser.getUserId());
            settingRepository.deleteNotice(noticeRequest);
            return ResponseEntity.ok(new SignUpResponse(true, "Notice Delete successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "Notice Delete Fail"));
        }
    }

    @PostMapping("/updateNotice")
    public ResponseEntity<?> updateNotice(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody NoticeRequest noticeRequest) {

        try {
            noticeRequest.setModId(currentUser.getUsername());
            noticeRequest.setModUserId(currentUser.getUserKey());
            settingRepository.updateNotice(noticeRequest);
            return ResponseEntity.ok(new SignUpResponse(true, "Notice Update successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "Notice Update Fail"));
        }
    }

    @PostMapping("/selectFaqList")
    public ResponseEntity<?> selectFaqList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody FaqRequest faqRequest) {

        try {
            FaqResponse data = settingService.selectFaqList(faqRequest);
            return ResponseEntity.ok(data);
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "FAQ search Fail"));
        }
    }

    @PostMapping("/detailFaq")
    public ResponseEntity<?> detailFaq(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody FaqRequest faqRequest) {

        try {
            FaqResponse data = settingService.detailFaq(faqRequest);
            return ResponseEntity.ok(data);
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "FAQ search Fail"));
        }
    }

    @PostMapping("/insertFaq")
    public ResponseEntity<?> insertFaq(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody FaqRequest faqRequest) {

        try {
            settingRepository.insertFaq(faqRequest);
            return ResponseEntity.ok(new SignUpResponse(true, "Faq Regist successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "Faq Regist Fail"));
        }
    }

    @PostMapping("/updateFaq")
    public ResponseEntity<?> updateFaq(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody FaqRequest faqRequest) {

        try {
            settingRepository.updateFaq(faqRequest);
            return ResponseEntity.ok(new SignUpResponse(true, "Faq Update successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "Faq Update Fail"));
        }
    }

    @PostMapping("/deleteFaq")
    public ResponseEntity<?> deleteFaq(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody FaqRequest faqRequest) {

        try {
            settingRepository.deleteFaq(faqRequest);
            return ResponseEntity.ok(new SignUpResponse(true, "Faq Delete successfully"));
        }catch(Exception e){
            return ResponseEntity.ok(new SignUpResponse(false, "Faq Delete Fail"));
        }
    }

    // 개발자공간 - 공지사항 최근 공지사항 1개 출력
    @PostMapping("/getNoticeFirstOne")
    public ResponseEntity<?> getNoticeFirstOne() {
        return ResponseEntity.ok(settingService.getNoticeFirstOne());
    }

}
