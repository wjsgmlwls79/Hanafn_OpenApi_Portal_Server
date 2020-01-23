package com.hanafn.openapi.portal.security.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.views.dto.UserLoginRequest;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final MessageSourceAccessor messageSource;
    private final CustomUserDetailsService customUserDetailsService;
    private final SettingRepository settingRepository;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		String username = authentication.getName();
		String password = (String) authentication.getCredentials();
		Collection<? extends GrantedAuthority> authorities;

        if(StringUtils.equals(authentication.getDetails().toString(), "adminPortal"))
        {
            if(authentication.getCredentials().equals("SSO")) { // SSO 통합인증 로그인
                UserLoginRequest.HfnLoginLockCheckRequest hfnLoginLockCheckRequest = new UserLoginRequest.HfnLoginLockCheckRequest();
                hfnLoginLockCheckRequest.setHfnCd(username.substring(0,2));
                hfnLoginLockCheckRequest.setHfnId(username.substring(2, username.length()));
                String loginLockYn = settingRepository.hfnLoginLockYn(hfnLoginLockCheckRequest);

                // 유효한 id 여부 체크
                if(loginLockYn == null) {
                    log.error(messageSource.getMessage("L002"));
                    throw new BusinessException("L002",messageSource.getMessage("L002"));
                }

                UserVO user = settingRepository.getAdminAuth(hfnLoginLockCheckRequest);

//                if ("1".equals(user.getPwChangeDt())) {
//                    log.error(messageSource.getMessage("L008"));
//                    throw new BusinessException("L008",messageSource.getMessage("L008"));
//                }

                // 로그인 잠금 여부 체크
                if(StringUtils.equals(loginLockYn, "N")){
                    try{
                        customUserDetailsService.loadBySiteCd(authentication);
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                        authorities = userDetails.getAuthorities();
                        return new UsernamePasswordAuthenticationToken(userDetails, password, authorities);

                    } catch (Exception e ) {
                        log.error("관리자포탈 SSO 통합로그인 에러 : " + e.toString());
                        throw new BusinessException("L002",messageSource.getMessage("L002"));
                    }
                }else{ // 로그인 잠금 상태면 에러 메시지 리턴
                    log.error(messageSource.getMessage("E022"));
                    throw new BusinessException("E022",messageSource.getMessage("E022"));
                }
            } else { // 일반 관계사 로그인
                UserLoginRequest.HfnLoginLockCheckRequest hfnLoginLockCheckRequest = new UserLoginRequest.HfnLoginLockCheckRequest();
                hfnLoginLockCheckRequest.setHfnCd(username.substring(0,2));
                hfnLoginLockCheckRequest.setHfnId(username.substring(2, username.length()));
                String loginLockYn = settingRepository.hfnLoginLockYn(hfnLoginLockCheckRequest);

                // 유효한 id 여부 체크
                if(loginLockYn == null)
                {
                    log.error(messageSource.getMessage("L002"));
                    throw new BusinessException("L002",messageSource.getMessage("L002"));
                }

                UserVO user = settingRepository.getAdminAuth(hfnLoginLockCheckRequest);

//                if ("1".equals(user.getPwChangeDt())) {
//                    log.error(messageSource.getMessage("L008"));
//                    throw new BusinessException("L008",messageSource.getMessage("L008"));
//                }

                // 로그인 잠금 여부 체크
                if(StringUtils.equals(loginLockYn, "N")){
                    try{
                        customUserDetailsService.loadBySiteCd(authentication);
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                        if (!passwordEncoder().matches(password, userDetails.getPassword())) { // 로그인 실패 시

                            // 로그인 실패 체크
                            int cnt = settingRepository.hfnLoginFailCntCheck(hfnLoginLockCheckRequest);
                            if(cnt < 4) { // 로그인 실패 횟수가 4회 이하면 로그인 실패 횟수 증가
                                hfnLoginLockCheckRequest.setLoginFailCnt(cnt+1);
                                settingRepository.hfnLoginFailCntSet(hfnLoginLockCheckRequest);
                            }else if(cnt == 4){ // 로그인 실패 횟수가 5이면 해당 계정 로그인 잠금
                                hfnLoginLockCheckRequest.setLoginFailCnt(cnt+1);
                                settingRepository.hfnLoginFailCntSet(hfnLoginLockCheckRequest);
                                settingRepository.hfnLoginLockChange(hfnLoginLockCheckRequest);
                            }

                            log.error(messageSource.getMessage("L003"));
                            throw new BusinessException("L003",messageSource.getMessage("L003"));
                        }

                        // 로그인 성공시 로그인잠금 관련 세팅 초기화
                        settingRepository.hfnLoginLockRelease(hfnLoginLockCheckRequest);

                        authorities = userDetails.getAuthorities();
                        return new UsernamePasswordAuthenticationToken(userDetails, password, authorities);

                    } catch( BusinessException e) {
                        log.error("관리자 포탈 로그인에러");
                        throw new BusinessException(e.getErrorCode(),messageSource.getMessage(e.getErrorCode()));
                    } catch (Exception e ) {
                        log.error("관리자 포탈 로그인 로직관련 에러 : " + e.toString());
                        throw new BusinessException("E026",messageSource.getMessage("E026"));
                    }
                }else{ // 로그인 잠금 상태면 에러 메시지 리턴
                    log.error(messageSource.getMessage("E022"));
                    throw new BusinessException("E022",messageSource.getMessage("E022"));
                }
            }

        }else if(StringUtils.equals(authentication.getDetails().toString(), "userPortal")) // 이용자 포탈 로그인 시
        {
            UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest = new UserLoginRequest.UserLoginLockCheckRequest();
            userLoginLockCheckRequest.setUserId(username);
            String loginLockYn = settingRepository.userLoginLockYn(userLoginLockCheckRequest);

            // 유효한 id 여부 체크
            if(loginLockYn == null)
            {
                log.error(messageSource.getMessage("L002"));
                throw new BusinessException("L002",messageSource.getMessage("L002"));
            }

            UserVO user = settingRepository.getUserAuth(userLoginLockCheckRequest);

//            if ("1".equals(user.getPwChangeDt())) {
//                log.error(messageSource.getMessage("L008"));
//                throw new BusinessException("L008",messageSource.getMessage("L008"));
//            }

            // 로그인 잠금 여부 체크
            if(StringUtils.equals(loginLockYn, "N")){
                try{
                    customUserDetailsService.loadBySiteCd(authentication);
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    if (!passwordEncoder().matches(password, userDetails.getPassword())) { // 로그인 실패 시

                        // 로그인 실패 체크
                        int cnt = settingRepository.userLoginFailCntCheck(userLoginLockCheckRequest);
                        if(cnt < 4) { // 로그인 실패 횟수가 4회 이하면 로그인 실패 횟수 증가
                            userLoginLockCheckRequest.setLoginFailCnt(cnt+1);
                            settingRepository.userLoginFailCntSet(userLoginLockCheckRequest);
                        }else if(cnt == 4){ // 로그인 실패 횟수가 5이면 해당 계정 로그인 잠금
                            userLoginLockCheckRequest.setLoginFailCnt(cnt+1);
                            settingRepository.userLoginFailCntSet(userLoginLockCheckRequest);
                            settingRepository.userLoginLockChange(userLoginLockCheckRequest);
                        }

                        log.error(messageSource.getMessage("L003"));
                        throw new BusinessException("L003",messageSource.getMessage("L003"));
                    }

                    // 로그인 성공시 로그인잠금 관련 세팅 초기화
                    settingRepository.userLoginLockRelease(userLoginLockCheckRequest);

                    authorities = userDetails.getAuthorities();
                    return new UsernamePasswordAuthenticationToken(userDetails, password, authorities);

                } catch( BusinessException e) {
                    log.error("이용자 포탈 로그인에러");
                    throw new BusinessException(e.getErrorCode(),messageSource.getMessage(e.getErrorCode()), e.getDetailMessage());
                }
                catch (Exception e ) {
                    log.error("이용자 포탈 로그인 잠금 로직관련 에러 : " + e.toString());
                    throw new BusinessException("E026",messageSource.getMessage("E026"));
                }
            }else{ // 로그인 잠금 상태면 에러 메시지 리턴
                log.error(messageSource.getMessage("E023"));
                throw new BusinessException("E023",messageSource.getMessage("E023"));
            }
        }
        log.error(messageSource.getMessage("L003"));
        throw new BusinessException("L003",messageSource.getMessage("L003"));
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	@Bean
    public PasswordEncoder passwordEncoder() {
		Map encoders = new HashMap<>();
		encoders.put("sha256", new StandardPasswordEncoder());

		return new DelegatingPasswordEncoder("sha256", encoders);
    }
}
