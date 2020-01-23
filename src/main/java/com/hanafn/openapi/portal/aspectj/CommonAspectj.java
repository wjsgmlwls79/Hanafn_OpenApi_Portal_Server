package com.hanafn.openapi.portal.aspectj;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.hanafn.openapi.portal.exception.*;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.util.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hanafn.openapi.portal.views.dto.LogRequest;
import com.hanafn.openapi.portal.views.service.LogService;

@Aspect
@Component
public class CommonAspectj {
	private static final Logger logger = LoggerFactory.getLogger(CommonAspectj.class);

	private final MessageSourceAccessor messageSource;
	private final LogService logService;

	@Autowired
	public CommonAspectj(MessageSourceAccessor messageSource, LogService logService) {
		this.messageSource = messageSource;
		this.logService = logService;
	}

	@Autowired
	CommonUtil commonUtil;

	@Around("(execution(* com.hanafn.openapi.portal.views.controller.*Controller.*(..)) " +
			"|| execution(public * com.hanafn.openapi.portal.security.controller.*Controller.*(..)))")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

		String reqUri = request.getRequestURI();
		String method = request.getMethod();
		String contentType = request.getHeader("Content-Type");
		String remoteAddr = request.getRemoteAddr();
		String clientIpAddr = commonUtil.getIp(request);

		UserPrincipal userPrincipal = null;
		String inputCtnt = "";
		String userKey = "";

		for(int i=0; i<joinPoint.getArgs().length; i++){
			Object arg = joinPoint.getArgs()[i];
			if(arg instanceof UserPrincipal){
				userPrincipal = (UserPrincipal)arg;

				if (userPrincipal.getUserType().equals("Hfn")) {
					userKey = userPrincipal.getUserKey();
				} else {
					userKey = userPrincipal.getUserId();
				}
			}
		}

		logger.info("[API]["+clientIpAddr+"|"+userKey+"]" + messageSource.getMessage("logger.aspect.1"));
		logger.info("[API]["+clientIpAddr+"|"+userKey+"]Class      : {}", joinPoint.getTarget());
		logger.info("[API]["+clientIpAddr+"|"+userKey+"]Method     : {}", method);
		logger.info("[API]["+clientIpAddr+"|"+userKey+"]RequestURI : {}", reqUri);
		logger.info("[API]["+clientIpAddr+"|"+userKey+"]ContentType: {}", contentType);
		logger.info("[API]["+clientIpAddr+"|"+userKey+"]RemoteAddr : {}", remoteAddr);

		logger.debug("["+clientIpAddr+"|"+userKey+"]" + "============================ Parameter ==============================");

		for(int i=0; i<joinPoint.getArgs().length; i++){
			inputCtnt += joinPoint.getArgs()[i];
			logger.debug("[API]["+clientIpAddr+"|"+userKey+"]" + joinPoint.getArgs()[i]);
		}

		logger.info("[API]["+clientIpAddr+"|"+userKey+"]" + "====================================================================");
		String trxId = UUIDUtils.generateUUID();
		LogRequest logRequest = new LogRequest();
		logRequest.setTrxId(trxId);
		logRequest.setTrxCd(reqUri);

		String msg = "";

		if(inputCtnt.length() >1500){
			inputCtnt = inputCtnt.substring(0,1500) + "...";
		}

		logRequest.setInputCtnt(inputCtnt);

		if(userPrincipal != null){
			logRequest.setUserId(userPrincipal.getUserKey());
			Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
			for(GrantedAuthority ga : authorities){
				logRequest.setRoleCd(ga.getAuthority());
			}
		}

		Object result = null;
		try {
			result = joinPoint.proceed(joinPoint.getArgs());

			// 포털로그 등록
			String outputCtnt = result.toString();
			if(outputCtnt.length() >1500){
				outputCtnt = outputCtnt.substring(0,1500) + "...";
			}

			logger.debug("[API]["+clientIpAddr+"|"+userKey+"]outputCtnt="+outputCtnt);
			logRequest.setOutputCtnt(outputCtnt);
			logRequest.setProcStatCd("OK");
			logService.insertPortalLog(logRequest);
		} catch (Throwable e) {
			if(e.getCause() != null){
				String outputCtnt = e.getCause().getMessage();
				if(outputCtnt.length() >1500){
					outputCtnt = outputCtnt.substring(0,1500) + "...";
				}
				logRequest.setOutputCtnt(outputCtnt);
			}
			else{
				String outputCtnt = ""+e;
				if(outputCtnt.length() >1500){
					outputCtnt = outputCtnt.substring(0,1500) + "...";
				}
				logRequest.setOutputCtnt(outputCtnt);
			}

			logRequest.setProcStatCd("ERROR");
			logService.insertPortalLog(logRequest);

			if (e instanceof BusinessException) {
				logger.error(" 비즈니스 예외 발생 : [" + ((ErrorCodeException) e).getErrorCode() + "] : " + e.getMessage());
				return ResponseEntity.ok(new ErrorResponse((ErrorCodeException) e));
			} else if (e instanceof Exception){
                logger.error(commonUtil.stackTraceToString((Exception) e));
				logger.error("처리하지 않은 예외객체 발생 : [" + e.getMessage() + "] : " + e.toString());
				throw new ServerException(messageSource.getMessage("C002"));
			} else {
				logger.error(commonUtil.stackTraceToString((Exception) e));
				logger.error("처리하지 않은 예외 발생 : [" + e.getMessage() + "] : " + e.toString());
				throw new ServerException(messageSource.getMessage("C002"));
			}
		}

		long timeTaken = System.currentTimeMillis() - startTime;
		logger.info("[API]["+clientIpAddr+"|"+userKey+"]" + messageSource.getMessage("logger.aspect.2") + messageSource.getMessage("logger.aspect.3") + "[{}]", timeTaken);
		logger.debug("[API]["+clientIpAddr+"|"+userKey+"]" + "Result : {}", result);

		return result;
	}
}