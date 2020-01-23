package com.hanafn.openapi.portal.security.controller;

import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.UserRequestDTO;
import com.hanafn.openapi.portal.util.AES256Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @PostMapping("/select")
    public ResponseEntity<?> authenticateUser(@CurrentUser UserPrincipal currentUser, @RequestBody UserRequestDTO request) {

        // 복호화
        boolean isEncrypted = true;

        if (StringUtils.equals(currentUser.getUserType(), "USER")
                || StringUtils.equals(currentUser.getUserType(), "ORGD")) {
            try {
                // 이름
                String decryptedUseorgUserNm = AES256Util.decrypt(currentUser.getUsername());
            } catch ( Exception e ) {
                log.error("복호화 에러 : " + e.toString());
                isEncrypted = false;
            } finally {
                if (isEncrypted) {
                    String decryptedUseorgUserNm = null;
                    try {
                        decryptedUseorgUserNm = AES256Util.decrypt(currentUser.getUsername());
                    } catch (GeneralSecurityException e) {
                        log.error("GeneralSecurityException");
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        log.error("UnsupportedEncodingException");
                        e.printStackTrace();
                    }
                    currentUser.setUsername(decryptedUseorgUserNm);
                    log.debug("복호화 - 이름: {}", decryptedUseorgUserNm);
                }
            }
        }
        return ResponseEntity.ok(currentUser);
    }
}
