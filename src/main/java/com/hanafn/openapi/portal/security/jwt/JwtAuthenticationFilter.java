package com.hanafn.openapi.portal.security.jwt;

import com.hanafn.openapi.portal.cmct.RedisCommunicater;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    MessageSourceAccessor messageSource;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userId = tokenProvider.getUserIdFromJWT(jwt);
                String userType = tokenProvider.getUserTypeFromJWT(jwt);

                UserDetails userDetails = customUserDetailsService.loadUserById(userId, userType);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                RedisCommunicater.validToken(jwt);

                String logout = request.getHeader("logout");

                if ("Y".equals(logout)) {
                    // 기존에 있던 토큰 삭제
                    RedisCommunicater.delToken(jwt);
                }

                String tokenTimeContinue = request.getHeader("tokenTimeContinue");

                if ("Y".equals(tokenTimeContinue)) {
                    // 기존에 있던 토큰 삭제
                    RedisCommunicater.delToken(jwt);

                    // 새로운 토큰 발급
                    String newJwt = tokenProvider.generateToken(authentication);

                    // 새로운 토큰 저장
                    RedisCommunicater.setToken(newJwt);

                    response.setHeader("AccessToken", newJwt);
                }
            }
        } catch (ExpiredJwtException eje) {
            logger.error("Unable to validate the access token : {}", eje.getMessage());
            request.setAttribute("JwtException", "expired");
            response.setHeader("AccessToken", "expired");
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            logger.error("Unable to validate the access token : {}", e.getMessage());
            request.setAttribute("JwtException", "invalid");
        } catch (BusinessException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인하십시오.");
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context {}", e);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}