package com.hanafn.openapi.portal.aspectj;

import com.hanafn.openapi.portal.aspectj.aspectComponent.TrxInfoBean;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.views.dto.RoleEnum;
import com.hanafn.openapi.portal.views.repository.LogRepository;
import com.hanafn.openapi.portal.views.vo.TrxInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class UriAuthAspect {
    private static final Logger logger = LoggerFactory.getLogger(UriAuthAspect.class);

    private final MessageSourceAccessor messageSource;
    private final LogRepository logRepository;
    private final TrxInfoBean trxInfoBean;

    @Autowired
    public UriAuthAspect(MessageSourceAccessor messageSource, LogRepository logRepository, TrxInfoBean trxInfoBean) {
        this.messageSource = messageSource;
        this.logRepository = logRepository;
        this.trxInfoBean = trxInfoBean;
    }

    @Before("execution(* com.hanafn.openapi.portal.views.controller.SettingController.*(..)) || " +
            "execution(* com.hanafn.openapi.portal.views.controller.ApiController.*(..)) || " +
            "execution(* com.hanafn.openapi.portal.views.controller.AppsController.*(..)) ||" +
            "execution(* com.hanafn.openapi.portal.views.controller.GuideController.*(..)) ||" +
            "execution(* com.hanafn.openapi.portal.views.controller.logController.*(..)) ||" +
            "execution(* com.hanafn.openapi.portal.views.controller.StatsController.*(..)) || " +
            "execution(* com.hanafn.openapi.portal.views.controller.CommunityController.*(com.hanafn.openapi.portal.security.UserPrincipal, ..))"
            )
    private void before(JoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        List<TrxInfoVO> trxInfoList;

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String reqUri = request.getRequestURI();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal currentUser = null;
        Collection<? extends GrantedAuthority> authorities = null;
        String roleCd = "";

        if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            logger.error(messageSource.getMessage("AM01"));
            throw new BusinessException("AM01", messageSource.getMessage("AM01"));
        } else {
            currentUser = (UserPrincipal) auth.getPrincipal();
            authorities = currentUser.getAuthorities();
            for(GrantedAuthority ga : authorities){
                roleCd = ga.getAuthority();
            }
            logger.debug("★인증받은 유저:["+currentUser.getUserId());
        }

        if(this.trxInfoBean.getTrxInfoList().isEmpty()) {
            logger.error("★trxInfoBean Emptry Issue occured DB ACCESS START");
            trxInfoList = logRepository.selectTrxInfoAll();
        } else {
            trxInfoList = this.trxInfoBean.getTrxInfoList();
        }

        boolean trxMatch = false;

        for(TrxInfoVO t : trxInfoList) {
            if(StringUtils.equals(t.getTrxCd(), reqUri)) {
                if(StringUtils.isNotBlank(t.getTrxGrant())) {
                    String [] a = t.getTrxGrant().split(",");
                    ArrayList<String> trxGrants = new ArrayList<>(Arrays.asList(a));
                    logger.debug("★trxGrants"+trxGrants);

                    if(trxGrants.contains(RoleEnum.resolve(roleCd).value())) {
                        trxMatch = true;
                    }

                } else {
                    logger.debug("★trxGrant가 null 이므로 권한 획득. 로직 진행["+t.getTrxGrant());
                    trxMatch = true;
                }

                break;
            }
        }

        if(!trxMatch) {
            logger.error(messageSource.getMessage("AM02") + reqUri);
            throw new BusinessException("AM02",messageSource.getMessage("AM02"));
        }
    }
}
