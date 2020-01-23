package com.hanafn.openapi.portal.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Autowired
    MessageSourceAccessor messageSource;

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {

        final String attr = (String) httpServletRequest.getAttribute("JwtException");

        logger.error("attr - {}", attr);
        logger.error("Responding with unauthorized error. Message - {}", e.getMessage());

        if (attr == null || attr.length() == 0) {
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } else if (attr.equals("expired")) {
            //TODO:YYJ:190923:메시지 처리
            httpServletResponse.setHeader("ErrorCode", "expired");
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인하십시오.");
        } else if (attr.equals("invalid")) {
            httpServletResponse.setHeader("ErrorCode", "unauthorized");
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "처리 중 오류가 발생했습니다.다시 로그인하십시오.");
        }
    }
}