package com.hanafn.openapi.portal.security.service;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.ResourceNotFoundException;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.HfnLoginRequest;
import com.hanafn.openapi.portal.security.dto.SSOLoginResponse;
import com.hanafn.openapi.portal.security.model.HfnUser;
import com.hanafn.openapi.portal.security.model.Login;
import com.hanafn.openapi.portal.security.model.Useorg;
import com.hanafn.openapi.portal.security.model.User;
import com.hanafn.openapi.portal.security.repository.*;
import com.hanafn.openapi.portal.util.DateUtil;
import com.hanafn.openapi.portal.views.dto.UserLoginRequest;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    MessageSourceAccessor messageSource;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UseorgRepository useorgRepository;
    @Autowired
    LoginRepository loginRepository;
    @Autowired
    HfnUserRepository hfnUserRepository;
    @Autowired
    SignupRepository signupRepository;
    @Autowired
    SettingRepository settingRepository;

    private String siteCd;

    private UserPrincipal userPrincipal = new UserPrincipal();

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userNm) throws UsernameNotFoundException {

        if("adminPortal".equals(this.siteCd)) {
            String hfnCd = userNm.substring(0,2);
            String hfnId = StringUtils.trim(userNm.substring(2));
            HfnUser hfnUser = hfnUserRepository.findByHfnCdAndHfnId(hfnCd, hfnId)
                    .orElseThrow(() -> new BusinessException("L002",messageSource.getMessage("L002")));

            if(StringUtils.equals("N", hfnUser.getUserStatCd())){
                log.error("사용자 상태코드 확인[" + hfnUser.getUserStatCd() + "]");
                throw new BusinessException("L004",messageSource.getMessage("L004"));
            }

            userPrincipal = UserPrincipal.create(hfnUser);

        } else {

            Login login = loginRepository.findByUserIdAndUserStatCd(userNm, "APLV")
                    .orElseThrow(() -> new BusinessException("L002",messageSource.getMessage("L002")));

            if (StringUtils.equals("ORGM", login.getUserType())) {
                Useorg useorg = useorgRepository.findByUseorgId(userNm)
                        .orElseThrow(() -> new BusinessException("L005",messageSource.getMessage("L005")));

                String pwHisDate = settingRepository.getPwHisDate(useorg.getUseorgId());

                userPrincipal = UserPrincipal.create(useorg, pwHisDate);

                if(StringUtils.equals(useorg.getUseorgStatCd(), "WAIT")){

                    UserVO user = settingRepository.selectLoginCertMgnt(userPrincipal.getUserKey());

                    // 유효기간검사
                    if (DateUtil.getDayDiff(DateUtil.getCurrentDate(), user.getExpireDttm()) > 0) {
                        log.error("이용기관 상태코드 확인[" + useorg.getUseorgStatCd() + "]");
                        throw new BusinessException("L006",messageSource.getMessage("L006"));
                    } else {
                        log.error("유효기간 경과 : 이용기관 상태코드 확인[" + useorg.getUseorgStatCd() + "]");
                        user.setExpire(false);
                        user.setUserGb(userPrincipal.getUserKey().startsWith("USEORG") ? "G" : "F");
                        throw new BusinessException("L010",messageSource.getMessage("L010"), user);
                    }

                } else if(StringUtils.equals(useorg.getUseorgStatCd(), "CLOSE")){
                    log.error("이용기관 상태코드 확인[" + useorg.getUseorgStatCd() + "]");
                    throw new BusinessException("L004",messageSource.getMessage("L004"));
                }
            } else {
                User user = userRepository.findByUserIdAndUserAplvStatNot(userNm, "REJECT")
                    .orElseThrow(() -> new BusinessException("L002",messageSource.getMessage("L002")));

                String pwHisDate = settingRepository.getPwHisDate(user.getUserId());

                userPrincipal = UserPrincipal.create(user, pwHisDate);

                if(!StringUtils.equals("OK", user.getStatCd()) && !StringUtils.equals("WAIT",user.getStatCd())){
                    log.error("사용자 상태코드 확인[" + user.getStatCd() + "]");
                    throw new BusinessException("L004",messageSource.getMessage("L004"));
                } else if(StringUtils.equals("WAIT",user.getStatCd())) {
                    log.error("사용자 상태코드 확인[" + user.getStatCd() + "]");
                    throw new BusinessException("L006",messageSource.getMessage("L006"));
                }
            }
        }

        return userPrincipal;
    }

    @Transactional
    public UserDetails loadUserById(String userKey, String userType) {

        if("Hfn".equals(userType)) {
            HfnUser hfnUser = hfnUserRepository.findById(userKey).orElseThrow(
                    () -> new ResourceNotFoundException("HfnUser", "userKey", userKey)
            );
            userPrincipal = UserPrincipal.create(hfnUser);
        } else if ("ORGM".equals(userType)) {
            Useorg useorg = useorgRepository.findById(userKey).orElseThrow(
                    () -> new ResourceNotFoundException("Useorg", "userKey", userKey)
            );

            String pwHisDate = settingRepository.getPwHisDate(useorg.getUseorgId());

            userPrincipal = UserPrincipal.create(useorg, pwHisDate);
        } else {
            User user = userRepository.findById(userKey).orElseThrow(
                    () -> new ResourceNotFoundException("User", "userKey", userKey)
            );

            String pwHisDate = settingRepository.getPwHisDate(user.getUserId());

            userPrincipal = UserPrincipal.create(user, pwHisDate);
        }
        return userPrincipal;
    }

    public void loadBySiteCd(Authentication authentication) {
        this.siteCd = authentication.getDetails().toString();
    }

}
