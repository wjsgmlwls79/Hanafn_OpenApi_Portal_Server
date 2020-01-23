package com.hanafn.openapi.portal.views.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanafn.openapi.portal.cmct.GWCommunicater;
import com.hanafn.openapi.portal.cmct.HubCommunicator;
import com.hanafn.openapi.portal.cmct.RedisCommunicater;
import com.hanafn.openapi.portal.cmct.dto.GWResponse;
import com.hanafn.openapi.portal.event.AppApiChangeDTO;
import com.hanafn.openapi.portal.event.AppApiEventHandler;
import com.hanafn.openapi.portal.event.UseorgEventHandler;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.file.FileService;
import com.hanafn.openapi.portal.security.dto.SignUpRequest;
import com.hanafn.openapi.portal.security.repository.SignupRepository;
import com.hanafn.openapi.portal.util.*;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.ApiRepository;
import com.hanafn.openapi.portal.views.repository.AppsRepository;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.repository.SettlementRepository;
import com.hanafn.openapi.portal.views.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class SettingService {

	@Autowired
	FileService fileService;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	CommonUtil commonUtil;
	@Autowired
	AppsRepository appsRepository;
	@Autowired
    SettingRepository settingRepository;
	@Autowired
	MessageSourceAccessor messageSource;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	UseorgEventHandler useorgEventHandler;
	@Autowired
	AppApiEventHandler appApiEventHandler;
	@Autowired
	AppsService appsService;
	@Autowired
	ApiRepository apiRepository;
	@Autowired
	SignupRepository signupRepository;
	@Autowired
	SettlementRepository settlementRepository;
	@Autowired
	HubCommunicator hubCommunicator;
	@Autowired
	SettlementService settlementService;


	@Autowired
	public MailUtils sendMail;

	private static int aplvSvcAddYear;

	@Value("${authUrl}")
	private String authUrl;

	@Value("${spring.profiles.active}")
	private String thisServer;

	// 보내는 이메일 주소
	private final static String SEND_EMAIL_URL = "openapimarket@hanafn.com";

	// 승인분류코드 단건
	private final static String FEE_DIV_CD_SINGLE = "FEE_S";

	// 승인분류코드 다건
	private final static String FEE_DIV_CD_MULTIPLE = "FEE_M";

	@Value("${aplv.svc.add.year}")
	public void setAplvSvcAddYear (int aplvSvcAddYear) {
		this.aplvSvcAddYear = aplvSvcAddYear;
	}

	public UseorgVO selectUseorg(UseorgRequest.UseorgDetailRequest useorgDetailRequest){
		UseorgVO useorgVO = settingRepository.selectUseorg(useorgDetailRequest);

		// 복호화
		try {
			// 이름
			if (useorgVO.getUseorgUserNm() != null && !"".equals(useorgVO.getUseorgUserNm())) {
				String decryptedUseorgUserNm = AES256Util.decrypt(useorgVO.getUseorgUserNm());
				useorgVO.setUseorgUserNm(decryptedUseorgUserNm);
				log.debug("복호화 - 이름: {}", decryptedUseorgUserNm);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 계좌번호
			if (useorgVO.getUseorgBankNo() != null && !"".equals(useorgVO.getUseorgBankNo())) {
				String decryptedUseorgBankNo = AES256Util.decrypt(useorgVO.getUseorgBankNo());
				useorgVO.setUseorgBankNo(decryptedUseorgBankNo);
				log.debug("복호화 - 계좌번호: {}", decryptedUseorgBankNo);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 휴대전화번호
			if (useorgVO.getUseorgUserTel() != null && !"".equals(useorgVO.getUseorgUserTel())) {
				String decryptedUseorgUserTel = AES256Util.decrypt(useorgVO.getUseorgUserTel());
				useorgVO.setUseorgUserTel(decryptedUseorgUserTel);
				log.debug("복호화 - 휴대전화번호: {}", decryptedUseorgUserTel);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}
		try {
			// 이메일
			if (useorgVO.getUseorgUserEmail() != null && !"".equals(useorgVO.getUseorgUserEmail())) {
				String decryptedUseorgUserEmail = AES256Util.decrypt(useorgVO.getUseorgUserEmail());
				useorgVO.setUseorgUserEmail(decryptedUseorgUserEmail);
				log.debug("복호화 - 이메일: {}", decryptedUseorgUserEmail);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		UserRequest.UserSecedeRequest userSecedeRequest = settingRepository.selectUserWithDrawReason(useorgDetailRequest.getUserKey());
		if (userSecedeRequest != null) {
			if (userSecedeRequest.getReasonGb() != null) {
				useorgVO.setReasonGb(userSecedeRequest.getReasonGb());
			} else {
				useorgVO.setReasonGb("");
			}

			if (userSecedeRequest.getReasonDetail() != null) {
				useorgVO.setReasonDetail(userSecedeRequest.getReasonDetail());
			} else {
				useorgVO.setReasonDetail("");
			}
		}
		return useorgVO;
	}

	public UseorgRsponsePaging selectUseorgListPaging(UseorgRequest useorgRequest){
		if(useorgRequest.getPageIdx() == 0)
			useorgRequest.setPageIdx(useorgRequest.getPageIdx() + 1);

		if(useorgRequest.getPageSize() == 0){
			useorgRequest.setPageSize(20);
		}

		useorgRequest.setPageOffset((useorgRequest.getPageIdx()-1)*useorgRequest.getPageSize());

		int totCnt = settingRepository.countUseorgList(useorgRequest);
		List<UseorgVO> list = settingRepository.selectUseorgListHfn(useorgRequest);

		for (UseorgVO useorgVO : list) {
			// 복호화
			try {
				// 이름
				if (useorgVO.getUseorgUserNm() != null && !"".equals(useorgVO.getUseorgUserNm())) {
					String decryptedUseorgUserNm = AES256Util.decrypt(useorgVO.getUseorgUserNm());
					useorgVO.setUseorgUserNm(decryptedUseorgUserNm);
					log.debug("복호화 - 이름: {}", decryptedUseorgUserNm);
				}
			} catch ( Exception e ) {
				log.error("이용기관 정보 조회 에러 : " + e.toString());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}
		}

		UseorgRsponsePaging pagingData = new UseorgRsponsePaging();
		pagingData.setPageIdx(useorgRequest.getPageIdx());
		pagingData.setPageSize(useorgRequest.getPageSize());
		pagingData.setList(list);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	public UseorgRsponse.UseorgDupCheckResponse useorgBrnDupCheck(UseorgRequest.UseorgDupCheckRequest useorgDupCheckRequest){

		int brnDupCheck = settingRepository.useorgDupCheckForBrn(useorgDupCheckRequest);
		if(brnDupCheck > 0){
			log.error("사업자 번호 중복 에러");
			throw new BusinessException("E002",messageSource.getMessage("E002"));
		}

		if (thisServer.equals("production")) {
			if (!isBrnId(useorgDupCheckRequest.getBrn())) {
				log.error("사업자 번호 에러 유효성 에러 ");
				throw new BusinessException("E019",messageSource.getMessage("E019"));
			}
		}

		UseorgRsponse.UseorgDupCheckResponse data = new UseorgRsponse.UseorgDupCheckResponse();
		data.setBrnDupYn("N");

		return data;
	}

	public boolean isBrnId (String str) {

		int hap = 0;
		int temp = 0;

		int check[] = {1,3,7,1,3,7,1,3,5};

		if(str.length() != 10) return false;

		for(int i=0; i < 9; i++){
			if(str.charAt(i) < '0' || str.charAt(i) > '9') 	return false;
			hap = hap + (Character.getNumericValue(str.charAt(i)) * check[temp]);
			temp++;
		}

		hap += (Character.getNumericValue(str.charAt(8))*5)/10;

		if ((10 - (hap%10))%10 == Character.getNumericValue(str.charAt(9))) {
			return true;
		} else {
			return false;
		}
	}

	public void insertUseorg(UseorgRequest.UseorgRegistRequest useorgRegistRequest){

		UseorgRequest.UseorgDupCheckRequest useorgDupRequest = new UseorgRequest.UseorgDupCheckRequest();
		useorgDupRequest.setBrn(useorgRegistRequest.getBrn());
		UseorgRsponse.UseorgDupCheckResponse useorgDupResponse = useorgBrnDupCheck(useorgDupRequest);

		if(StringUtils.equals(useorgDupResponse.getBrnDupYn(), "Y")){
			log.error("사업자 번호 중복 에러 ");
			throw new BusinessException("E002", messageSource.getMessage("E002"));
		}
		settingRepository.insertUseorg(useorgRegistRequest);
	}

	public void updateUseorg(UseorgRequest.UseorgUpdateRequest useorgUpdateRequest){

		if(StringUtils.equals(useorgUpdateRequest.getUseorgStatCd(), "CLOSE")){
			if(useorgUpdateRequest.getErrorMsg() == null || StringUtils.equals(useorgUpdateRequest.getErrorMsg(), "")){
				log.error("updateUseorg 에러 발생");
				throw new BusinessException("E024",messageSource.getMessage("E024"));
			}
		}

		UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
		useorgDetailRequest.setUserKey(useorgUpdateRequest.getUserKey());

		// 수정시 API
		useorgEventHandler.eventCreate("update", useorgDetailRequest, useorgUpdateRequest.getHfnCd());

		UseorgVO useorgInfo = settingRepository.selectUseorg(useorgDetailRequest);

		if(StringUtils.equals(useorgInfo.getUseorgStatCd(), "OK") && StringUtils.equals(useorgUpdateRequest.getUseorgStatCd(), "CLOSE")){
			RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), useorgUpdateRequest.getHfnCd(),"false");
		}else if(StringUtils.equals(useorgInfo.getUseorgStatCd(), "CLOSE") && StringUtils.equals(useorgUpdateRequest.getUseorgStatCd(), "OK")){
			RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), useorgUpdateRequest.getHfnCd(),"true");
		}

		// 암호화
		try {
			// 이름
			if (useorgUpdateRequest.getUseorgUserNm() != null && !"".equals(useorgUpdateRequest.getUseorgUserNm())) {
				String encryptedUseorgUserNm = AES256Util.encrypt(useorgUpdateRequest.getUseorgUserNm());
				useorgUpdateRequest.setUseorgUserNm(encryptedUseorgUserNm);
				log.debug("암호화 - 이름: {}", encryptedUseorgUserNm);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 계좌번호
			if (useorgUpdateRequest.getUseorgBankNo() != null && !"".equals(useorgUpdateRequest.getUseorgBankNo())) {
				String encryptedUseorgBankNo = AES256Util.encrypt(useorgUpdateRequest.getUseorgBankNo());
				useorgUpdateRequest.setUseorgBankNo(encryptedUseorgBankNo);
				log.debug("암호화 - 계좌번호: {}", encryptedUseorgBankNo);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 휴대전화번호
			if (useorgUpdateRequest.getUseorgUserTel() != null && !"".equals(useorgUpdateRequest.getUseorgUserTel())) {
				String encryptedUseorgUserTel = AES256Util.encrypt(useorgUpdateRequest.getUseorgUserTel());
				useorgUpdateRequest.setUseorgUserTel(encryptedUseorgUserTel);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 이메일
			if (useorgUpdateRequest.getUseorgUserEmail() != null && !"".equals(useorgUpdateRequest.getUseorgUserEmail())) {
				String encryptedUseorgUserEmail = AES256Util.encrypt(useorgUpdateRequest.getUseorgUserEmail());
				useorgUpdateRequest.setUseorgUserEmail(encryptedUseorgUserEmail);
				log.debug("암호화 - 이메일: {}", encryptedUseorgUserEmail);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			settingRepository.updateUseorg(useorgUpdateRequest);
			if ("1".equals(useorgUpdateRequest.getPwChange())) {
				settingRepository.updateLoginUser(useorgUpdateRequest);
				settingRepository.insertPwHisUseorg(useorgUpdateRequest);
			}

		} catch(Exception e){
			log.error("이용기관 수정 에러");
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		// 본인인증, 운영서버 반영
		if(StringUtils.equals(useorgUpdateRequest.getUseorgGb(), "SOLE") && StringUtils.isNotBlank(useorgUpdateRequest.getUserDi())){
			try {
				SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
				sndCertMgntRequest.setSendCd("S1");	// 본인인증 코드
				sndCertMgntRequest.setResultCd(useorgUpdateRequest.getUserResSeq());
				sndCertMgntRequest.setSendNo(useorgUpdateRequest.getUserDi());
				sndCertMgntRequest.setUserKey(useorgUpdateRequest.getUserKey());
				signupRepository.insertSndCertMgntForSelfAuth(sndCertMgntRequest);
			} catch (Exception e){
				log.error(e.toString());
				throw new BusinessException("E026", messageSource.getMessage("E026"));
			}
		}
	}

	// 이용기관 상태 변경
	public void updateUseorgStatCdChange(UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest){

		UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
		useorgDetailRequest.setUserKey(useorgStatCdChangeRequest.getUserKey());

		UseorgVO useorgInfo = settingRepository.selectUseorg(useorgDetailRequest);

		if(StringUtils.equals(useorgInfo.getUseorgStatCd(), "OK")){

			if(useorgStatCdChangeRequest.getErrorMsg() == null || StringUtils.equals(useorgStatCdChangeRequest.getErrorMsg(), "")){
				log.error("updateUseorgStatCdChange 에러 발생");
				throw new BusinessException("E024",messageSource.getMessage("E024"));
			}

			useorgStatCdChangeRequest.setUseorgStatCd("CLOSE");
			RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), useorgStatCdChangeRequest.getHfnCd(),"false");

		}else{
			useorgStatCdChangeRequest.setErrorMsg("");
			useorgStatCdChangeRequest.setUseorgStatCd("OK");
			RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), useorgStatCdChangeRequest.getHfnCd(),"true");
		}
		settingRepository.updateUseorgStatCdChange(useorgStatCdChangeRequest);
	}

	public UseorgRsponse selectUseorgAllList(UseorgRequest useorgRequest) {

		List<UseorgVO> list = settingRepository.selectUseorgAllList(useorgRequest);

		UseorgRsponse data = new UseorgRsponse();
		data.setList(list);

		return data;
	}

	public UseorgRsponse selectUseorgAllListByHfn(UseorgRequest useorgRequest) {

		List<UseorgVO> list = settingRepository.selectUseorgAllListByHfn(useorgRequest);

		UseorgRsponse data = new UseorgRsponse();
		data.setList(list);

		return data;
	}

	public void useorgHfnAplv(UseorgRequest.HfnAplvRequest request, MultipartFile file, boolean fileChangedCheck) {
		// 파일저장
		String fileName = null;
		if(fileChangedCheck) {
			try {
				fileName = fileService.fileSave(file);
				UseorgRequest.UseorgUploadRequest uploadRequest = new UseorgRequest.UseorgUploadRequest();
				uploadRequest.setUserKey(request.getUserKey());
				uploadRequest.setUseorgUpload(fileName);
				settingRepository.updateUseorgUpload(uploadRequest);
			} catch ( IOException e ) {
				log.error("API 이용신청 에러 : " + e.toString());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}
		}

		AplvRequest.AplvRegistRequest aplvRequest = new AplvRequest.AplvRegistRequest();
		aplvRequest.setAplvReqCd(request.getUserKey());
		if (StringUtils.equals(request.getStatCd(), "N")) {
			aplvRequest.setAplvDivCd("HFNAPLV");
		} else {
			aplvRequest.setAplvDivCd("HFNDEL");
		}

		aplvRequest.setAplvReqCtnt(request.getUseorgNm());
		aplvRequest.setRegUserName(request.getUseorgUserNm());
		aplvRequest.setRegUserId(request.getUseorgId());
		aplvRequest.setHfnCd(request.getHfnCd());

		settingRepository.useorgHfnAplv(request);

		//승인 요청 등록 to PORTAL_APLV_INFO
		settingRepository.insertAplv(aplvRequest);

		aplvLineSettingRequest(aplvRequest);
	}

	/*
	 * ******************************사용자******************************
	 * */

	public UserVO selectUser(UserRequest.UserDetailRequest userDetailRequest){
		UserVO userVO = settingRepository.selectUser(userDetailRequest);

		// 복호화
		try {
			// 이름
			if (userVO.getUserNm() != null && !"".equals(userVO.getUserNm())) {
				String decryptedUserNm = AES256Util.decrypt(userVO.getUserNm());
				userVO.setUserNm(decryptedUserNm);
				log.debug("복호화 - 이름: {}", decryptedUserNm);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 전화번호
			if (userVO.getUserTel() != null && !"".equals(userVO.getUserTel())) {
				String decryptedUserTel = AES256Util.decrypt(userVO.getUserTel());
				userVO.setUserTel(decryptedUserTel);
				log.debug("복호화 - 이름: {}", decryptedUserTel);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 이메일
			if (userVO.getUserEmail() != null && !"".equals(userVO.getUserEmail())) {
				String decryptedUserEmail = AES256Util.decrypt(userVO.getUserEmail());
				userVO.setUserEmail(decryptedUserEmail);
				log.debug("복호화 - 이름: {}", decryptedUserEmail);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		if(userVO != null) {
			if(StringUtils.equals(userVO.getRoleCd(),"5")) {
				userVO.setUserCompany(userVO.getUseorgNm());
			}
		}
		return userVO;
	}

	public UseorgVO selectUseorgData(UserRequest.UserDetailRequest userDetailRequest){
		return settingRepository.selectUseorgData(userDetailRequest);
	}

	public UserRsponsePaging selectUserListPaging(UserRequest userRequest){
		if(userRequest.getPageIdx() == 0)
			userRequest.setPageIdx(userRequest.getPageIdx() + 1);

		if(userRequest.getPageSize() == 0){
			userRequest.setPageSize(20);
		}

		userRequest.setPageOffset((userRequest.getPageIdx()-1)*userRequest.getPageSize());

		int totCnt = settingRepository.countUserList(userRequest);
		List<UserVO> list = settingRepository.selectUserList(userRequest);

		// 복호화
		for (UserVO userVO : list) {
			try {
				// 이름
				if (userVO.getUserNm() != null && !"".equals(userVO.getUserNm())) {
					String decryptedUserNm = AES256Util.decrypt(userVO.getUserNm());
					userVO.setUserNm(decryptedUserNm);
					log.debug("복호화 - 이름: {}", decryptedUserNm);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}

			try {
				// 전화번호
				if (userVO.getUserTel() != null && !"".equals(userVO.getUserTel())) {
					String decryptedUserTel = AES256Util.decrypt(userVO.getUserTel());
					userVO.setUserTel(decryptedUserTel);
					log.debug("복호화 - 이름: {}", decryptedUserTel);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}

			try {
				// 이메일
				if (userVO.getUserEmail() != null && !"".equals(userVO.getUserEmail())) {
					String decryptedUserEmail = AES256Util.decrypt(userVO.getUserEmail());
					userVO.setUserEmail(decryptedUserEmail);
					log.debug("복호화 - 이름: {}", decryptedUserEmail);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}
		}

		UserRsponsePaging pagingData = new UserRsponsePaging();
		pagingData.setPageIdx(userRequest.getPageIdx());
		pagingData.setPageSize(userRequest.getPageSize());
		pagingData.setList(list);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	public void updateUser(UserRequest.UserUpdateRequest userUpdateRequest){
		// 암호화
		try {
			// 이름
			if (userUpdateRequest.getUserNm() != null && !"".equals(userUpdateRequest.getUserNm())) {
				String encryptedUserNm = AES256Util.encrypt(userUpdateRequest.getUserNm());
				userUpdateRequest.setUserNm(encryptedUserNm);
				log.debug("암호화 - 이름: {}", encryptedUserNm);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 휴대전화번호
			if (userUpdateRequest.getUserTel() != null && !"".equals(userUpdateRequest.getUserTel())) {
				String encryptedUserTel = AES256Util.encrypt(userUpdateRequest.getUserTel());
				userUpdateRequest.setUserTel(encryptedUserTel);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		try {
			// 이메일
			if (userUpdateRequest.getUserEmail() != null && !"".equals(userUpdateRequest.getUserEmail())) {
				String encryptedUserEmail = AES256Util.encrypt(userUpdateRequest.getUserEmail());
				userUpdateRequest.setUserEmail(encryptedUserEmail);
				log.debug("암호화 - 이메일: {}", encryptedUserEmail);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		settingRepository.updateUser(userUpdateRequest);

		// USER_ROLE_INFO 테이블 UPDATE
		HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest = new HfnUserRequest.HfnUserUpdateRequest();
		hfnUserUpdateRequest.setRoleCd(userUpdateRequest.getRoleCd());
		hfnUserUpdateRequest.setUserKey(userUpdateRequest.getUserKey());
		settingRepository.updateHfnUserRole(hfnUserUpdateRequest);

		// USER_LOGIN 테이블도 UPDATE
		UserLoginRequest.UpdateTypeRequest updateTypeRequest = new UserLoginRequest.UpdateTypeRequest();
		updateTypeRequest.setUserKey(userUpdateRequest.getUserKey());
		String requestRoleCd = userUpdateRequest.getRoleCd();
		String userType = Optional.ofNullable(commonUtil.getUserTypeByRoleCd(requestRoleCd)).orElse("");
		updateTypeRequest.setUserType(userType);
		settingRepository.updateUserLoginType(updateTypeRequest);

		// 본인인증도 업데이트 했을 시 (운영서버 반영)
		if(StringUtils.equals(userUpdateRequest.getUserGb(), "K") && StringUtils.isNotBlank(userUpdateRequest.getUserDi())){
			try {
				SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
				sndCertMgntRequest.setSendCd("S1");		// 본인인증 코드
				sndCertMgntRequest.setSendNo(userUpdateRequest.getUserDi());
				sndCertMgntRequest.setResultCd(userUpdateRequest.getUserResSeq());
				sndCertMgntRequest.setUserKey(userUpdateRequest.getUserKey());
				signupRepository.insertSndCertMgntForSelfAuth(sndCertMgntRequest);
			} catch (Exception e){
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}
		}
	}

	public UserRsponse.UserDupCheckResponse userEmailDupCheck(UserRequest.UserDupCheckRequest userDupCheckRequest){
		// 암호화
		try {
			// 이메일
			if (userDupCheckRequest.getUserEmail() != null && !"".equals(userDupCheckRequest.getUserEmail())) {
				String encryptedUseorgUserEmail = AES256Util.encrypt(userDupCheckRequest.getUserEmail());
				userDupCheckRequest.setUserEmail(encryptedUseorgUserEmail);
				log.debug("암호화 - 이메일: {}", encryptedUseorgUserEmail);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		int userEmailDupCheck = settingRepository.userEmailDupCheck(userDupCheckRequest);

		String userEmailDupYn = "N";

		if(userEmailDupCheck > 0){
			userEmailDupYn = "Y";
		}

		UserRsponse.UserDupCheckResponse data = new UserRsponse.UserDupCheckResponse();
		data.setUserEmailDupYn(userEmailDupYn);

		return data;
	}

	public UserRsponse.UserDupCheckResponse useorgEmailDupCheck(UserRequest.UserDupCheckRequest userDupCheckRequest){
		// 암호화
		try {
			// 이메일
			if (userDupCheckRequest.getUserEmail() != null && !"".equals(userDupCheckRequest.getUserEmail())) {
				String encryptedUseorgUserEmail = AES256Util.encrypt(userDupCheckRequest.getUserEmail());
				userDupCheckRequest.setUserEmail(encryptedUseorgUserEmail);
				log.debug("암호화 - 이메일: {}", encryptedUseorgUserEmail);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		int userEmailDupCheck = settingRepository.useorgEmailDupCheck(userDupCheckRequest);

		String userEmailDupYn = "N";

		if(userEmailDupCheck > 0){
			userEmailDupYn = "Y";
		}

		UserRsponse.UserDupCheckResponse data = new UserRsponse.UserDupCheckResponse();
		data.setUserEmailDupYn(userEmailDupYn);

		return data;
	}

	public UserRsponse.UserDupCheckResponse userEmailDupCheckUpdate(UserRequest.UserDupCheckRequest userDupCheckRequest){
        // 암호화
        try {
            // 이메일
			if (userDupCheckRequest.getUserEmail() != null && !"".equals(userDupCheckRequest.getUserEmail())) {
				String encryptedUserEmail = AES256Util.encrypt(userDupCheckRequest.getUserEmail());
				userDupCheckRequest.setUserEmail(encryptedUserEmail);
				log.debug("암호화 - 이메일: {}", encryptedUserEmail);
			}
        } catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
        }

		int userEmailDupCheck = settingRepository.userEmailDupCheckUpdate(userDupCheckRequest);

		String userEmailDupYn = "N";

		if(userEmailDupCheck > 0){
			userEmailDupYn = "Y";
		}

		UserRsponse.UserDupCheckResponse data = new UserRsponse.UserDupCheckResponse();
		data.setUserEmailDupYn(userEmailDupYn);

		return data;
	}

	public void updateUserStatCdChange(UserRequest.UserStatCdChangeRequest userStatCdChangeRequest){
		UserRequest.UserDetailRequest userDetailRequest = new UserRequest.UserDetailRequest();
		userDetailRequest.setUserKey(userStatCdChangeRequest.getUserKey());

		UserVO userInfo = settingRepository.selectUser(userDetailRequest);

		// 복호화
		try {
			// 이름
			if (userInfo.getUserNm() != null && !"".equals(userInfo.getUserNm())) {
				String decryptedUseorgUserNm = AES256Util.decrypt(userInfo.getUserNm());
				userInfo.setUserNm(decryptedUseorgUserNm);
				log.debug("복호화 - 이름: {}", decryptedUseorgUserNm);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		if(StringUtils.equals(userInfo.getUserStatCd(), "WAIT")){
			throw new BusinessException("E014",messageSource.getMessage("E014"));
		}

		if(StringUtils.equals(userInfo.getUserStatCd(), "OK")){
			userStatCdChangeRequest.setUserStatCd("CLOSE");
		}else if(StringUtils.equals(userInfo.getUserStatCd(), "CLOSE")){
			userStatCdChangeRequest.setUserStatCd("OK");
		}

		settingRepository.updateUserStatCdChange(userStatCdChangeRequest);
	}
	public void userPwdUpdate(UserRequest.UserPwdUpdateRequest userPwdUpdateRequest){
		passwordCheck(userPwdUpdateRequest.getUserPwd());

		userPwdUpdateRequest.setUserPwd(passwordEncoder.encode(userPwdUpdateRequest.getUserPwd()));
		settingRepository.userPwdUpdate(userPwdUpdateRequest);
	}

	public void userPwdAndTosUpdate(UserRequest.UserPwdAndTosUpdateRequest userPwdAndTosUpdateRequest){
		passwordCheck(userPwdAndTosUpdateRequest.getUserPwd());

		if(StringUtils.equals(userPwdAndTosUpdateRequest.getPortalTosYn(), "N")){
			throw new BusinessException("E015",messageSource.getMessage("E015"));
		}

		if(StringUtils.equals(userPwdAndTosUpdateRequest.getPrivacyTosYn(), "N")){
			throw new BusinessException("E015",messageSource.getMessage("E015"));
		}

		userPwdAndTosUpdateRequest.setUserPwd(passwordEncoder.encode(userPwdAndTosUpdateRequest.getUserPwd()));
		settingRepository.userPwdAndTosUpdate(userPwdAndTosUpdateRequest);
	}

	public UserRsponse.UserTmpPwdIssueResponse tmpPwdIssue(UserRequest.UserTmpPwdUpdateRequest userTmpPwdUpdateRequest){
		String tmpPwd = UUIDUtils.generateUUID();

		tmpPwd = StringUtils.substring(StringUtils.upperCase(tmpPwd),0,10);

		userTmpPwdUpdateRequest.setUserPwd(passwordEncoder.encode(tmpPwd));
		userTmpPwdUpdateRequest.setTmpPwd(passwordEncoder.encode(tmpPwd));
		settingRepository.userTepPwdUpdate(userTmpPwdUpdateRequest);

		UserRsponse.UserTmpPwdIssueResponse userTmpPwdIssueResponse = new UserRsponse.UserTmpPwdIssueResponse();
		userTmpPwdIssueResponse.setTepPwd(tmpPwd);

		return userTmpPwdIssueResponse;
	}

	public UserWithdrawVO selectUserWithdraw(UserRequest request) {
		UserWithdrawVO userWithdrawVO = settingRepository.selectUserWithDraw(request);
		return userWithdrawVO;
	}

	public void secedeUser(UserRequest.UserSecedeRequest request){
		settingRepository.secedeUser(request);
		settingRepository.secedeUserLogin(request);
		settingRepository.secedeUserWithDraw(request);
	}

	public void secedeUseorg(UseorgRequest.UseorgSecedeRequest request) {
		UseorgVO userData = settingRepository.selectSecedeUseorg(request);

		request.setHbnUseYn(userData.getHbnUseYn());
		request.setHnwUseYn(userData.getHnwUseYn());
		request.setHlfUseYn(userData.getHlfUseYn());
		request.setHcpUseYn(userData.getHcpUseYn());
		request.setHcdUseYn(userData.getHcdUseYn());
		request.setHsvUseYn(userData.getHsvUseYn());
		request.setHmbUseYn(userData.getHmbUseYn());

		settingRepository.secedeUseorgInfo(request);
		settingRepository.secedeUseorgLogin(request);
		settingRepository.secedeUseorgWithDraw(request);
	}

	/*
	 * ******************************관계사******************************
	 * */

	public void insertHfnUser(HfnUserRequest.HfnUserRegistRequest hfnUserRegistRequest){

		HfnUserRequest.HfnUserDupCheckRequest userDupCheckRequest = new HfnUserRequest.HfnUserDupCheckRequest();
		userDupCheckRequest.setHfnId(hfnUserRegistRequest.getHfnId());
		userDupCheckRequest.setHfnCd(hfnUserRegistRequest.getHfnCd());

		UserRsponse.UserDupCheckResponse userDupCheckResponse = hfnIdDupCheck(userDupCheckRequest);
		if(StringUtils.equals(userDupCheckResponse.getHfnIdDupYn(), "Y")){
			throw new BusinessException("E028",messageSource.getMessage("E028"));
		}

		settingRepository.insertHfnUser(hfnUserRegistRequest);

		// user_role_info에 id,role_Cd 정보 담기
		settingRepository.insertHfnUserRole(hfnUserRegistRequest);

		// 결재자 일시 결재선 테이블에도 해당유저 정보 삽입
		if(StringUtils.equals(hfnUserRegistRequest.getSignUserYn(), "Y")){
			log.debug("해당 유저는 결재자입니다. :["+hfnUserRegistRequest.getUserNm()+"]");
			settingRepository.insertHfnLine(hfnUserRegistRequest);
		}
	}

	public UserRsponse.UserDupCheckResponse hfnIdDupCheck(HfnUserRequest.HfnUserDupCheckRequest userDupCheckRequest){
		int hfnIdDupCheck = settingRepository.hfnIdDupCheck(userDupCheckRequest);

		String hfnIdDupYn = "N";

		if(hfnIdDupCheck > 0){
			hfnIdDupYn = "Y";
		}

		UserRsponse.UserDupCheckResponse data = new UserRsponse.UserDupCheckResponse();
		data.setHfnIdDupYn(hfnIdDupYn);

		return data;
	}

	public void updateHfnUser(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest){

		// id 세팅
		HfnUserRequest.HfnUserRegistRequest hfnUserRegistRequest = new HfnUserRequest.HfnUserRegistRequest();
		hfnUserRegistRequest.setHfnId(hfnUserUpdateRequest.getHfnId());
		hfnUserRegistRequest.setHfnCd(hfnUserUpdateRequest.getHfnCd());
        hfnUserRegistRequest.setRegUserId(hfnUserUpdateRequest.getRegUser());
		HfnUserVO hfnUserInfo = settingRepository.selectHfnUserForId(hfnUserRegistRequest);
		hfnUserUpdateRequest.setUserKey(hfnUserInfo.getUserKey());

		// user_role_info에 id,role_Cd 정보 담기
		settingRepository.updateHfnUser(hfnUserUpdateRequest);
		settingRepository.updateHfnUserRole(hfnUserUpdateRequest);
		settingRepository.insertUserPwHis(hfnUserUpdateRequest);

		// 결재자 테이블 정보 get
		HfnUserRequest.HfnLineRequest hfnLineRequest = new HfnUserRequest.HfnLineRequest();
		hfnLineRequest.setUserKey(hfnUserInfo.getUserKey());

		Optional<HfnLineVO> hfnLineVO = Optional.ofNullable(settingRepository.selectHfnLineById(hfnLineRequest));

		// 결재자 라인에 정보가 있을 경우 무조건 update
		if(hfnLineVO.isPresent()){
			settingRepository.updateHfnLine(hfnUserUpdateRequest);
		} else { // 정보가 없을 경우에는 signUserYn 확인 후 insert
			if(StringUtils.equals(hfnUserUpdateRequest.getSignUserYn(),"Y")) {
				hfnUserRegistRequest = objectMapper.convertValue(hfnUserUpdateRequest, HfnUserRequest.HfnUserRegistRequest.class);
				hfnUserRegistRequest.setRegUserId(hfnUserUpdateRequest.getRegUser());
				settingRepository.insertHfnLine(hfnUserRegistRequest);
			}
			else {	// 정보가 없고 N일땐 아무것도 하지 않는다.

			}
		}

		// 대직자
		if(StringUtils.equals(hfnUserUpdateRequest.getAltYn(), "Y")){
			settingRepository.updateHfnLine(hfnUserUpdateRequest);
			settingRepository.insertHfnAltInfo(hfnUserUpdateRequest);
		}

		if(!StringUtils.equals(hfnUserUpdateRequest.getExAltId(), "")){
			settingRepository.updateHfnAltInfo(hfnUserUpdateRequest);
		}

		// 대직자 테이블도 update
		settingRepository.updateHfnAltInfoByHfnEdit(hfnUserUpdateRequest);
	}


	public void updateHfnUserPwd(HfnUserRequest.HfnUserPwdUpdateRequest request) {
		settingRepository.updateHfnUserPwd(request);
		settingRepository.setHfnUserPwdHis(request);
	}

	public HfnUserRsponsePaging selectHfnUserListPaging(HfnUserRequest hfnUserRequest){
		if(hfnUserRequest.getPageIdx() == 0)
			hfnUserRequest.setPageIdx(hfnUserRequest.getPageIdx() + 1);

		if(hfnUserRequest.getPageSize() == 0){
			hfnUserRequest.setPageSize(20);
		}

		hfnUserRequest.setPageOffset((hfnUserRequest.getPageIdx()-1)*hfnUserRequest.getPageSize());

		int totCnt = settingRepository.countHfnUserList(hfnUserRequest);
		List<HfnUserVO> list = settingRepository.selectHfnUserList(hfnUserRequest);

		HfnUserRsponsePaging hfnUserRsponsePaging = new HfnUserRsponsePaging();
		hfnUserRsponsePaging.setPageIdx(hfnUserRequest.getPageIdx());
		hfnUserRsponsePaging.setPageSize(hfnUserRequest.getPageSize());
		hfnUserRsponsePaging.setList(list);
		hfnUserRsponsePaging.setTotCnt(totCnt);
		hfnUserRsponsePaging.setSelCnt(list.size());

		return hfnUserRsponsePaging;
	}

	public void updateHfnUserStatCdChange(HfnUserRequest.HfnUserStatCdChangeRequest hfnUserStatCdChangeRequest){
		HfnUserRequest.HfnUserDetailRequest hfnUserDetailRequest = new HfnUserRequest.HfnUserDetailRequest();
		hfnUserDetailRequest.setUserKey(hfnUserStatCdChangeRequest.getUserKey());

		HfnUserVO hfnUserInfo = settingRepository.selectHfnUser(hfnUserDetailRequest);

		if(StringUtils.equals(hfnUserInfo.getUserStatCd(), "WAIT")){
			throw new BusinessException("E014",messageSource.getMessage("E014"));
		}

		if(StringUtils.equals(hfnUserInfo.getUserStatCd(), "Y")){
			hfnUserStatCdChangeRequest.setUserStatCd("N");
		}else if(StringUtils.equals(hfnUserInfo.getUserStatCd(), "N")){
			hfnUserStatCdChangeRequest.setUserStatCd("Y");
		}
		settingRepository.updateHfnUserStatCdChange(hfnUserStatCdChangeRequest);
	}

	public void updateHfnAltInfo(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest){
		settingRepository.updateHfnLine(hfnUserUpdateRequest);
		settingRepository.updateHfnAltInfo(hfnUserUpdateRequest);
	}

	public HfnUserRsponse.HfnAltUsersResponse getHfnAltUsers(HfnUserRequest.HfnAltUserRequest request){
		HfnUserRsponse.HfnAltUsersResponse response = new HfnUserRsponse.HfnAltUsersResponse();
		response.setHfnUserAvaliableAltList(settingRepository.selectHfnAltUsers(request));
		return response;
	}

	public HfnUserVO selectHfnUserById(HfnUserRequest.HfnUserDetailRequest hfnUserDetailRequest){

		HfnUserVO hfnUserVO = settingRepository.selectHfnUser(hfnUserDetailRequest);
		return hfnUserVO;
	}

	// 관계사 나의 정보
	public HfnUserRsponse selectHfnUser(HfnUserRequest.HfnUserDetailRequest hfnUserDetailRequest){

		HfnUserVO hfnUserVO = settingRepository.selectHfnUser(hfnUserDetailRequest);
		HfnUserVO tmp = settingRepository.selectHfnLine(hfnUserDetailRequest);

		HfnUserRsponse data = new HfnUserRsponse();
		data.setHfnUserVO(hfnUserVO);

		if(StringUtils.equals(hfnUserVO.getSignUserYn(), "Y") && tmp != null ){
			HfnUserRequest.HfnAltUserRequest request = new HfnUserRequest.HfnAltUserRequest();
			request.setHfnCd(tmp.getHfnCd());
			request.setHfnId(tmp.getHfnId());
			HfnLineVO hfnLineVO = settingRepository.selectHfnAltUser(request);
			data.setHfnLineVO(hfnLineVO);
		}
		return data;
	}

	public HfnUserRsponse selectMyHfnMember(HfnInfoRequest hfnInfoRequest){

		List<HfnUserVO> list = settingRepository.selectMyHfnMember(hfnInfoRequest);
		HfnUserRsponse data = new HfnUserRsponse();
		data.setList(list);

		return data;
	}

	public void hfnUserPwdUpdate(HfnUserRequest.HfnUserPwdUpdateRequest hfnUserPwdUpdateRequest){
		passwordCheck(hfnUserPwdUpdateRequest.getUserPwd());
		hfnUserPwdUpdateRequest.setUserPwd(passwordEncoder.encode(hfnUserPwdUpdateRequest.getUserPwd()));

		settingRepository.hfnUserPwdUpdate(hfnUserPwdUpdateRequest);
		settingRepository.hfnUserPwdHisUpdate(hfnUserPwdUpdateRequest);
	}

	public void hfnUserPwdAndTosUpdate(HfnUserRequest.HfnUserPwdAndTosUpdateRequest hfnUserPwdAndTosUpdateRequest){
		passwordCheck(hfnUserPwdAndTosUpdateRequest.getUserPwd());

		if(StringUtils.equals(hfnUserPwdAndTosUpdateRequest.getPortalTosYn(), "N")){
			throw new BusinessException("E015",messageSource.getMessage("E015"));
		}

		if(StringUtils.equals(hfnUserPwdAndTosUpdateRequest.getPrivacyTosYn(), "N")){
			throw new BusinessException("E015",messageSource.getMessage("E015"));
		}

		hfnUserPwdAndTosUpdateRequest.setUserPwd(passwordEncoder.encode(hfnUserPwdAndTosUpdateRequest.getUserPwd()));
		settingRepository.hfnUserPwdAndTosUpdate(hfnUserPwdAndTosUpdateRequest);
	}

	/*
	 * ******************************승인******************************
	 * */

	public AplvRsponse.AplvDetilResponse selectAplv(AplvRequest.AplvDetailRequest aplvDetailRequest){

		AplvRsponse.AplvDetilResponse response = new AplvRsponse.AplvDetilResponse();

		// 승인정보
		AplvVO aplvInfo = settingRepository.selectAplv(aplvDetailRequest);

		// 복호화
		try {
			// 이메일
			if (aplvInfo.getUseorgUserEmail() != null && !"".equals(aplvInfo.getUseorgUserEmail())) {
				String decryptedUseorgUserEmail = AES256Util.decrypt(aplvInfo.getUseorgUserEmail());
				aplvInfo.setUseorgUserEmail(decryptedUseorgUserEmail);
				log.debug("복호화 - 이메일: {}", decryptedUseorgUserEmail);
			}
		} catch ( Exception e ) {
			log.error(e.getMessage());
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}

		if (StringUtils.equals(aplvInfo.getUserNmEncrypted(), "Y")) {
			try {
				// 이름
				if (aplvInfo.getRegUser() != null && !"".equals(aplvInfo.getRegUser())) {
					String decryptedUserNm = AES256Util.decrypt(aplvInfo.getRegUser());
					aplvInfo.setRegUser(decryptedUserNm);
					log.debug("복호화 - 이름: {}", decryptedUserNm);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}
		}

		if (StringUtils.equals(aplvInfo.getProcUserNmEncrypted(), "Y")) {
			try {
				// 이름
				if (aplvInfo.getProcUser() != null && !"".equals(aplvInfo.getProcUser())) {
					String decryptedUserNm = AES256Util.decrypt(aplvInfo.getProcUser());
					aplvInfo.setProcUser(decryptedUserNm);
					log.debug("복호화 - 이름: {}", decryptedUserNm);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}
		}

		aplvInfo.setAplvBtnYn(settingRepository.selectAplvBtnYn(aplvDetailRequest));

		log.debug("aplvInfo.getAplvDivCd():"+aplvInfo.getAplvDivCd());
		if(StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORG")
				|| StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORGEXP")
				|| StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNAPLV")
				||  StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNDEL")){

			UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
			useorgDetailRequest.setUserKey(aplvInfo.getAplvReqCd());
			UseorgVO useorg = settingRepository.selectUseorg(useorgDetailRequest);

			// 복호화
			try {
				// 이름
				if (useorg.getUseorgUserNm() != null && !"".equals(useorg.getUseorgUserNm())) {
					String decryptedUseorgUserNm = AES256Util.decrypt(useorg.getUseorgUserNm());
					useorg.setUseorgUserNm(decryptedUseorgUserNm);
					log.debug("복호화 - 이름: {}", decryptedUseorgUserNm);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}

			try {
				// 계좌번호
				if (useorg.getUseorgBankNo() != null && !"".equals(useorg.getUseorgBankNo())) {
					String decryptedUseorgBankNo = AES256Util.decrypt(useorg.getUseorgBankNo());
					useorg.setUseorgBankNo(decryptedUseorgBankNo);
					log.debug("복호화 - 계좌번호: {}", decryptedUseorgBankNo);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}

			try {
				// 휴대전화번호
				if (useorg.getUseorgUserTel() != null && !"".equals(useorg.getUseorgUserTel())) {
					String decryptedUseorgUserTel = AES256Util.decrypt(useorg.getUseorgUserTel());
					useorg.setUseorgUserTel(decryptedUseorgUserTel);
					log.debug("복호화 - 휴대전화번호: {}", decryptedUseorgUserTel);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}

			try {
				// 이메일
				if (useorg.getUseorgUserEmail() != null && !"".equals(useorg.getUseorgUserEmail())) {
					String decryptedUseorgUserEmail = AES256Util.decrypt(useorg.getUseorgUserEmail());
					useorg.setUseorgUserEmail(decryptedUseorgUserEmail);
					log.debug("복호화 - 이메일: {}", decryptedUseorgUserEmail);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}
			response.setUseorgInfo(useorg);

		} else if (StringUtils.equals(aplvInfo.getAplvDivCd(), FEE_DIV_CD_SINGLE) || StringUtils.equals(aplvInfo.getAplvDivCd(), FEE_DIV_CD_MULTIPLE)) {

			// 과금수수료결재
			SettlementRequest settlementRequest = new SettlementRequest();

			String bilMonth = aplvInfo.getAplvReqCd().split("_")[0];
			String hfnCd = aplvInfo.getAplvReqCd().split("_")[1];
			String appKey = "";

			settlementRequest.setBilMonth(bilMonth);
			settlementRequest.setHfnCd(hfnCd);
			settlementRequest.setPageSize(10000);

			if (aplvInfo.getAplvDivCd().equals(FEE_DIV_CD_SINGLE)) {
				appKey = aplvInfo.getAplvReqCd().split("_")[2];
				settlementRequest.setAppKey(appKey);
			}

			List<FeeCollectionInfoVO> feeRegList = settlementRepository.getFeeCollectionInfoList(settlementRequest);

			response.setFeeRegList(feeRegList);

		} else {
			AppsRequest appsRequest = new AppsRequest();
			appsRequest.setAppKey(aplvInfo.getAplvReqCd());
			appsRequest.setAplvSeqNo(aplvInfo.getAplvSeqNo());

			AppsVO appsVO;
			// 앱정보
			if(StringUtils.equals(aplvInfo.getAplvStatCd(), "APPROVAL") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEXP")) {
				appsVO = settingRepository.selectAppDetailInfo(appsRequest);
			} else if (StringUtils.equals(aplvInfo.getAplvStatCd(), "REJECT")){
				appsVO = settingRepository.selectAppHisDetail(appsRequest);
			} else {
				appsVO = settingRepository.aplvReqAppDetailInfo(appsRequest);
			}

			// 복호화
			try {
				// 계좌번호
				if (appsVO.getAccNo() != null && !"".equals(appsVO.getAccNo())) {
					String decryptedAccNo = AES256Util.decrypt(appsVO.getAccNo());
					appsVO.setAccNo(decryptedAccNo);
					log.debug("복호화 - 계좌번호: {}", decryptedAccNo);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}

			response.setAppInfo(appsVO);

			if (appsVO != null) {
				if(StringUtils.equals(appsVO.getAppAplvStatCd(), "CANCEL")){
					response.setRejectCtnt(appsVO.getReqCancelCtnt());
				}
			}
			if(StringUtils.equals(aplvInfo.getAplvStatCd(), "APPROVAL")){
				String appSvcAddEnDt = DateUtil.formatDateTime(appsVO.getAppSvcEnDt(), "yyyy-MM-dd");
				response.setAppSvcAddDt(appSvcAddEnDt);
			}else{
				// appSvcvEnDt null 일수도 있으므로 null 처리
				if (appsVO != null) {
					if (appsVO.getAppSvcEnDt() != null && !"".equals(appsVO.getAppSvcEnDt())) {
						String appSvcAddEnDt = DateUtil.formatDateTime(DateUtil.getCalDateYear(appsVO.getAppSvcEnDt(), aplvSvcAddYear), "yyyy-MM-dd");
						response.setAppSvcAddDt(appSvcAddEnDt);
					}
				}
			}

			ApiRequest.ApiDetailRequest apiDetailRequest = new ApiRequest.ApiDetailRequest();
			apiDetailRequest.setAppKey(aplvInfo.getAplvReqCd());
			if(StringUtils.equals(aplvInfo.getAplvStatCd(), "APPROVAL") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEXP")) {
				apiDetailRequest.setUseFl("Y");
			} else {
				apiDetailRequest.setUseFl("WAIT");
			}

			List<ApiVO> ApiList;

			if (StringUtils.equals(aplvInfo.getAplvStatCd(), "REJECT")){
				apiDetailRequest.setAplvSeqNo(aplvInfo.getAplvSeqNo());
				ApiList = settingRepository.selectAppApiRejectList(apiDetailRequest);
				apiDetailRequest.setUseFl("N");
			} else {
				ApiList = settingRepository.selectAppApiDetailList(apiDetailRequest);
			}

			List<ApiVO> ipList = settingRepository.getIpList(apiDetailRequest);

			response.setApiList(ApiList);
			response.setIpList(ipList);

            if(StringUtils.equals(aplvInfo.getAplvStatCd(), "APPROVAL") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEXP")) {
                apiDetailRequest.setUseFl("Y");
            } else if(StringUtils.equals(aplvInfo.getAplvStatCd(), "REJECT")) {
				apiDetailRequest.setUseFl("REJECT");
			} else {
                apiDetailRequest.setUseFl(null);
            }
			List<RequestApiVO> requestApiList = settingRepository.getRequestList(apiDetailRequest);
			response.setRequestApiList(requestApiList);
		}

		List<AplvHisVO> aplvHisList = settingRepository.selectAplvHis(aplvDetailRequest);

		if(StringUtils.equals(aplvInfo.getAplvStatCd(), "REJECT")){
			for(AplvHisVO AplvHisInfo : aplvHisList){
				if(StringUtils.equals(AplvHisInfo.getAplvStatCd(), "REJECT")){
					response.setRejectCtnt(AplvHisInfo.getRejectCtnt());
				}
			}
		}

		response.setAplvSeqNo(aplvInfo.getAplvSeqNo());
		response.setAplvReqCd(aplvInfo.getAplvReqCd());
		response.setAplvStatCd(aplvInfo.getAplvStatCd());
		response.setAplvDivCd(aplvInfo.getAplvDivCd());
		response.setAplvReqCtnt(aplvInfo.getAplvReqCtnt());
		response.setRegDttm(aplvInfo.getRegDttm());
		response.setRegUser(aplvInfo.getRegUser());
		response.setRegId(aplvInfo.getRegId());
		response.setProcDttm(aplvInfo.getProcDttm());
		response.setProcUser(aplvInfo.getProcUser());
		response.setAplvBtnYn(aplvInfo.getAplvBtnYn());
        response.setProcId(aplvInfo.getProcId());

		response.setAplvHisList(aplvHisList);

		return response;
	}
	public AplvRsponsePaging selectAplvListPaging(AplvRequest aplvRequest){
		if(aplvRequest.getPageIdx() == 0)
			aplvRequest.setPageIdx(aplvRequest.getPageIdx() + 1);

		if(aplvRequest.getPageSize() == 0){
			aplvRequest.setPageSize(20);
		}

		aplvRequest.setPageOffset((aplvRequest.getPageIdx()-1)*aplvRequest.getPageSize());

		int totCnt = settingRepository.countAplvList(aplvRequest);
		List<AplvVO> list = settingRepository.selectAplvList(aplvRequest);

		// 복호화: 이메일
		for(AplvVO vo : list) {
			try {
				// 이메일
				if (vo.getUseorgUserEmail() != null && !"".equals(vo.getUseorgUserEmail())) {
					String decryptedUseorgUserEmail = AES256Util.decrypt(vo.getUseorgUserEmail());
					vo.setUseorgUserEmail(decryptedUseorgUserEmail);
					log.debug("복호화 - 이메일: {}", decryptedUseorgUserEmail);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}

            if (StringUtils.equals(vo.getUserNmEncrypted(), "Y")) {
                try {
                    // 이름
                    if (vo.getRegUser() != null && !"".equals(vo.getRegUser())) {
                        String decryptedUserNm = AES256Util.decrypt(vo.getRegUser());
                        vo.setRegUser(decryptedUserNm);
                        log.debug("복호화 - 이름: {}", decryptedUserNm);
                    }
                } catch ( Exception e ) {
                    log.error(e.getMessage());
                    throw new BusinessException("E026",messageSource.getMessage("E026"));
                }
            }
		}

		// 승인권자 인지 확인 - 각 승인레벨 및 이전단계 승인정도 체크
		for(AplvVO vo : list) {
			AplvRequest.AplvDetailRequest req = new AplvRequest.AplvDetailRequest();
			req.setAplvSeqNo(vo.getAplvSeqNo());
			req.setProcUser(aplvRequest.getProcUser());
			req.setHfnCd(aplvRequest.getSearchHfnCd());
			req.setProcId(aplvRequest.getProcId());
			String s = settingRepository.selectAplvBtnYn(req);
			vo.setAplvBtnYn(s);
		}

		AplvRsponsePaging pagingData = new AplvRsponsePaging();
		pagingData.setPageIdx(aplvRequest.getPageIdx());
		pagingData.setPageSize(aplvRequest.getPageSize());
		pagingData.setList(list);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	public void insertAplv(AplvRequest.AplvRegistRequest aplvRegistRequest){
		//승인 요청 등록 to PORTAL_APLV_INFO
//		settingRepository.insertAplv(aplvRegistRequest);

		// 삽입한 승인요청의 APLV_SEQ_NO를 가져옴
//		AplvVO aplv = settingRepository.selectAplvForAplvReqCd(aplvRegistRequest);

		// 관계사 담당자에게 결재선 등록 승인 생성
//		aplvRegistRequest.setAplvSeqNo(aplvRegistRequest.getAplvSeqNo());
		aplvLineSettingRequest(aplvRegistRequest);

	}

	// 관계사 담당자에게 결재선 등록 승인 생성
	public void aplvLineSettingRequest(AplvRequest.AplvRegistRequest request) {

		List<HfnInfoVO> hfnList = new ArrayList<HfnInfoVO>();
		// 관련된 관계사 리스트 불러오기
		if(StringUtils.contains(request.getAplvDivCd(), "APP"))
		{
			hfnList = settingRepository.selectedAppRelatedHfn(request);
		}
		else if(StringUtils.equals(request.getAplvDivCd(), "USEORG"))
		{
			hfnList = settingRepository.selectedUseorgRelatedHfn(request);
		}
		else if(StringUtils.equals(request.getAplvDivCd(), "HFNAPLV") || StringUtils.equals(request.getAplvDivCd(), "HFNDEL"))
		{
			hfnList = settingRepository.selectHfnByHfnCd(request.getHfnCd());
		}
		else if(StringUtils.equals(request.getAplvDivCd(), "USEORGEXP"))
        {
            hfnList = settingRepository.selectedUseorgExpRelatedHfn(request);
        }

		List<String> userparamList = new ArrayList<>();
		// 관계사 별 담당자에게 승인(결재선 등록) 지정
		for(HfnInfoVO info : hfnList) {
			HfnInfoRequest r = new HfnInfoRequest();
			r.setHfnCd(info.getHfnCd());
			List<HfnUserVO> userList = settingRepository.selectHfnManager(r);

			settingRepository.insertAplv(request);

			request.setAplvSeqNo(request.getAplvSeqNo());

			for(HfnUserVO user : userList) {
				AplvRequest.AplvHisRegistRequest aplvHisRegistRequest = new AplvRequest.AplvHisRegistRequest();
				aplvHisRegistRequest.setAplvSeqNo(request.getAplvSeqNo());
				aplvHisRegistRequest.setProcUser(user.getUserNm());
				aplvHisRegistRequest.setProcId(user.getUserKey());
				aplvHisRegistRequest.setRegUserName("APLV_SYSTEM");
				aplvHisRegistRequest.setRegUserId("APLV_SYSTEM");
				aplvHisRegistRequest.setHfnCd(user.getHfnCd());

				settingRepository.insertAplvHis(aplvHisRegistRequest);
				if(thisServer.equals("production")) {
					userparamList.add(userParamGen(user.getHfnCd(), user.getHfnId(), user.getUserNm()));
				}
			}
		}

		// 승인 정보 가져옴
		AplvVO aplvInfo = settingRepository.selectAplvDetail(request.getAplvSeqNo());

		if(thisServer.equals("development") || thisServer.equals("staging")) {
			userparamList.add(userParamGen("07", "2144", "이혜성"));
			userparamList.add(userParamGen("07", "2744", "반성욱"));
			userparamList.add(userParamGen("07", "2676", "최진현"));
			userparamList.add(userParamGen("07", "2600", "염유진"));
		}

		String title = "";
		switch(aplvInfo.getAplvDivCd()) {
			case "USEORG":
				title = "이용기관 등록";
				break;
			case "APP":
				title = "APP 등록";
				break;
			case "APPEXP":
				title = "APP 기간연장";
				break;
			case "APPDEL":
				title = "APP 삭제";
				break;
			case "APPEDIT":
				title = "APP 수정";
				break;
			case "HFNAPLV":
				title = "API 이용 신청";
				break;
			case "HFNDEL":
				title = "API 이용 중지 신청";
				break;
			case "FEE_S":
				title = "과금 수수료 결재(단일)";
				break;
			case "FEE_M":
				title = "과금 수수료 결재(다중)";
				break;
			case "HFNFEE":
				title = "관계사 과금내역 결재";
				break;
			default:
				log.error("@@ Aplv Div Cd 에러");
		}

		String name = "";
		String userKey = "";
		if(StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORG") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNAPLV") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNDEL")) {
			UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
			useorgDetailRequest.setUserKey(aplvInfo.getAplvReqCd());
			name = "이용기관명: " + settingRepository.selectUseorg(useorgDetailRequest).getUseorgNm();
			userKey = aplvInfo.getAplvReqCd();
		} else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEXP")) {
			AppsRequest appsRequest = new AppsRequest();
			appsRequest.setAppKey(aplvInfo.getAplvReqCd());
			AppsVO app = appsRepository.selectAppDetail(appsRequest);
			UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
			useorgDetailRequest.setUserKey(app.getUserKey());
			UseorgVO useorg = settingRepository.selectUseorg(useorgDetailRequest);

			name = "앱 명: " + app.getAppNm() + "\n" + "이용기관명: " + useorg.getUseorgNm();
			userKey = useorg.getUserKey();

		} else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_S")) {
			String[] tmp = aplvInfo.getAplvReqCd().split("_");
			String billMonth = tmp[0];
			String hfnCd =  tmp[1];
			String appKey = tmp[2];

			String hfnNm = commonUtil.hfnCd2hfnNm(hfnCd);

			AppsRequest appsRequest = new AppsRequest();
			appsRequest.setAppKey(appKey);
			AppsVO app = appsRepository.selectAppDetail(appsRequest);

			UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
			useorgDetailRequest.setUserKey(app.getUserKey());
			UseorgVO useorg = settingRepository.selectUseorg(useorgDetailRequest);

			name = "앱 명: " + app.getAppNm() + "\n" + "이용기관명: " + useorg.getUseorgNm() + "\n" + "관계사: " + hfnNm + "\n" + "청구월 : " + billMonth;
			userKey = useorg.getUserKey();

		} else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_M")) {
			String[] tmp = aplvInfo.getAplvReqCd().split("_");
			String billMonth = tmp[0];
			String hfnCd =  tmp[1];

			String hfnNm = commonUtil.hfnCd2hfnNm(hfnCd);

			name = "관계사: " + hfnNm + "\n" + "청구월 : " + billMonth;
			userKey = request.getRegUserId();

		} else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNFEE")) {
			// TODO:LHS 관계사 과금 관련 승인에 대한 처리 필요

			name = "아직 미구현 기능입니다.";
			userKey = request.getRegUserId();

		} else {
			AppsRequest appsRequest = new AppsRequest();
			appsRequest.setAppKey(aplvInfo.getAplvReqCd());
			AppsVO app = appsRepository.selectAppModDetail(appsRequest);
			UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
			useorgDetailRequest.setUserKey(app.getUserKey());
			UseorgVO useorg = settingRepository.selectUseorg(useorgDetailRequest);

			name = "앱 명: " + app.getAppNm() + "\n" + "이용기관명: " + useorg.getUseorgNm();
			userKey = useorg.getUserKey();
		}

		String ctntMsg = "승인요청: " + title + "\n" +
						 name + "\n" +
					 	 "신청자: " + aplvInfo.getRegUser() + "\n" +
						 "신청일: " + aplvInfo.getRegDttm().substring(0,19);

		log.debug("@@ hub send Msg = " + ctntMsg);

		if(thisServer.equals("development") || thisServer.equals("staging") || thisServer.equals("production")) {
			// 개발, 품질, 운영일 경우 하나 허브 메시지 발송
			hubCommunicator.HubMsgCommunicator(title, ctntMsg, userparamList, userKey, "H1");
		}
	}

	// 메시지허브 user parameter 생성
	public String userParamGen(String hfnCd, String hfnId, String userNm) {
		String pad = "";
		int len = 7 - hfnId.length();
		for(int i=0; i<len; i++){
			pad += "0";
		}
		return hfnCd + pad + hfnId + "_" + userNm ;
	}

	// 결재 승인
	public void aplvApproval(AplvRequest.AplvApprovalRequest aplvApprovalRequest) throws Exception {

		// 결재 히스토리에서 해당 결재 정보 가져옴
		AplvRequest.AplvHisDetailRequest aplvHisDetailRequest = new AplvRequest.AplvHisDetailRequest();
		aplvHisDetailRequest.setAplvSeqNo(aplvApprovalRequest.getAplvSeqNo());
		aplvHisDetailRequest.setProcUser(aplvApprovalRequest.getProcId());

        String hfnCdValue = settingRepository.selectHfnCdByUserKey(aplvApprovalRequest.getProcId());

		AplvHisVO aplvHisInfo = settingRepository.selectAplvHisDetailInfo(aplvHisDetailRequest);

		AplvRequest.AplvDetailRequest aplvDetailRequest = new AplvRequest.AplvDetailRequest();
		aplvDetailRequest.setAplvSeqNo(aplvApprovalRequest.getAplvSeqNo());
		aplvDetailRequest.setProcUser(aplvApprovalRequest.getProcUser());
		aplvDetailRequest.setHfnCd(aplvApprovalRequest.getHfnCd());

		//승인 정보 조회
		AplvVO aplvInfo = settingRepository.selectAplv(aplvDetailRequest);

		//승인 내역에 해당 처리자 승인 으로 변경
		AplvRequest.AplvApprovalRequest aplvUpdateRequest = new AplvRequest.AplvApprovalRequest();
		aplvUpdateRequest.setAplvSeqNo(aplvApprovalRequest.getAplvSeqNo());
		aplvUpdateRequest.setRejectCtnt(aplvApprovalRequest.getRejectCtnt());
		aplvUpdateRequest.setProcUser(aplvApprovalRequest.getProcUser());
		aplvUpdateRequest.setProcId(aplvApprovalRequest.getProcId());

		// 승인 정보에 해당 승인내역 업데이트
		settingRepository.updateAplvInfo(aplvUpdateRequest);

		aplvUpdateRequest.setAplvStatCd("APPROVAL");
		// 승인 히스토리에도 해당 승인내역 업데이트
		settingRepository.updateAplvHis(aplvUpdateRequest);

		//남은 승인자가 있는지 체크
		int aplvCnt = settingRepository.countAplvRest(aplvApprovalRequest);

		//잔여 승인이 없으면 승인으로 변경
		if(aplvCnt == 0){

			aplvInfo.setHfnCd(aplvApprovalRequest.getHfnCd());

			String userKey = "";
			if(StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORG") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNAPLV") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNDEL")) {
				userKey = aplvInfo.getAplvReqCd();
			} else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APP") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEXP") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPDEL") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEDIT")) {
				AppsRequest appsRequest = new AppsRequest();
				appsRequest.setAppKey(aplvInfo.getAplvReqCd());
				userKey = appsRepository.selectAppModDetail(appsRequest).getUserKey();
			}

			// 승인결과 이메일전송
			aplvEmail(aplvInfo, userKey, "승인", "");

			settingRepository.updateAplvStatCdChange(aplvUpdateRequest);

			if(StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORG") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNAPLV")){
				//이용 기관 승인 전 정보 세팅
				UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest = new UseorgRequest.UseorgStatCdChangeRequest();
				useorgStatCdChangeRequest.setUserKey(aplvInfo.getAplvReqCd());
				useorgStatCdChangeRequest.setUseorgStatCd("OK");
				useorgStatCdChangeRequest.setRegUserName(aplvApprovalRequest.getProcUser());
				useorgStatCdChangeRequest.setRegUserId(aplvApprovalRequest.getProcId());

				UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
				useorgDetailRequest.setUserKey(aplvInfo.getAplvReqCd());
				UseorgVO useorgInfo = settingRepository.selectUseorg(useorgDetailRequest);

				// 이용기관이 하나은행을 이용할 경우
				if (!StringUtils.equals(hfnCdValue, "01")) {
					useorgInfo.setHbnUseYn("N");
				}
				// 이용기관이 하나금융투자를 이용할 경우
				if (!StringUtils.equals(hfnCdValue, "02")) {
					useorgInfo.setHnwUseYn("N");
				}
				// 이용기관이 하나생명을 이용할 경우
				if (!StringUtils.equals(hfnCdValue, "04")) {
					useorgInfo.setHlfUseYn("N");
				}
				// 이용기관이 하나캐피탈을 이용할 경우
				if (!StringUtils.equals(hfnCdValue, "05")) {
					useorgInfo.setHcpUseYn("N");
				}
				// 이용기관이 하나카드를 이용할 경우
				if (!StringUtils.equals(hfnCdValue, "12")) {
					useorgInfo.setHcdUseYn("N");
				}
				// 이용기관이 하나저축은행을 이용할 경우
				if (!StringUtils.equals(hfnCdValue, "14")) {
					useorgInfo.setHsvUseYn("N");
				}
				// 이용기관이 하나멤버스를 이용할 경우
				if (!StringUtils.equals(hfnCdValue, "99")) {
					useorgInfo.setHmbUseYn("N");
				}

				useorgStatCdChangeRequest.setHbnUseYn(useorgInfo.getHbnUseYn());
				useorgStatCdChangeRequest.setHnwUseYn(useorgInfo.getHnwUseYn());
				useorgStatCdChangeRequest.setHlfUseYn(useorgInfo.getHlfUseYn());
				useorgStatCdChangeRequest.setHcpUseYn(useorgInfo.getHcpUseYn());
				useorgStatCdChangeRequest.setHcdUseYn(useorgInfo.getHcdUseYn());
				useorgStatCdChangeRequest.setHsvUseYn(useorgInfo.getHsvUseYn());
				useorgStatCdChangeRequest.setHmbUseYn(useorgInfo.getHmbUseYn());

				// 이용기관 승인 완료시, 관계사 USE_YN=W -> Y 로 변경
				settingRepository.updateUseorgStatCdAfterAplv(useorgStatCdChangeRequest);

				UseorgRequest.UseorgDetailRequest request = new UseorgRequest.UseorgDetailRequest();
				request.setUserKey(useorgInfo.getUserKey());

				/*** Redis ENTR_CD SET ***/
				// 이용기관이 하나은행을 이용할 경우
                if( StringUtils.equals(useorgInfo.getHbnUseYn(), "W") && StringUtils.equals(hfnCdValue, "01")){
                    useorgEventHandler.eventCreate("create", request, "01");
                    RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), hfnCdValue, "true");
                }

                // 이용기관이 하나금융투자를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHnwUseYn(), "W") && StringUtils.equals(hfnCdValue, "02")){
                    //useorgEventHandler.eventCreate("create", request, "02");
                    RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), hfnCdValue, "true");
                }

                // 이용기관이 하나생명을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHlfUseYn(), "W") && StringUtils.equals(hfnCdValue, "04")){
                    RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), hfnCdValue, "true");
                }

                // 이용기관이 하나캐피탈을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHcpUseYn(), "W") && StringUtils.equals(hfnCdValue, "05")){
					useorgEventHandler.eventCreate("create", request, HfnEnum.HCP.value());
                    RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), hfnCdValue, "true");
                }

                // 이용기관이 하나카드를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHcdUseYn(), "W") && StringUtils.equals(hfnCdValue, "12")){
                    RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), hfnCdValue, "true");
                }

                // 이용기관이 하나저축은행을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHsvUseYn(), "W") && StringUtils.equals(hfnCdValue, "14")){
                    RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), hfnCdValue, "true");
                }

                // 이용기관이 하나멤버스를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHmbUseYn(), "W") && StringUtils.equals(hfnCdValue, "99")){
                    RedisCommunicater.useorgRedisSet(useorgInfo.getEntrCd(), hfnCdValue, "true");
                }

				/*** Redis ENC_KEY SET ***/
				try {
					RedisCommunicater.encKeyRedisSet(useorgInfo.getEntrCd(), useorgInfo.getEncKey());
				} catch (Exception e) {
					log.error("encKeyRedisSet 에러");
					throw new BusinessException("E026",messageSource.getMessage("E026"));
				}

            } else if (StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNDEL")) {
                //이용 기관 승인 전 정보 세팅
                UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest = new UseorgRequest.UseorgStatCdChangeRequest();
                useorgStatCdChangeRequest.setUserKey(aplvInfo.getAplvReqCd());
                useorgStatCdChangeRequest.setUseorgStatCd("OK");
                useorgStatCdChangeRequest.setRegUserName(aplvApprovalRequest.getRegUserName());
                useorgStatCdChangeRequest.setRegUserId(aplvApprovalRequest.getProcId());

                UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
                useorgDetailRequest.setUserKey(aplvInfo.getAplvReqCd());
                UseorgVO useorgInfo = settingRepository.selectUseorg(useorgDetailRequest);

                // 이용기관이 하나은행을 이용할 경우
                if (StringUtils.equals(hfnCdValue, "01")) {
                    useorgInfo.setHbnUseYn("Y");
                } else {
					useorgInfo.setHbnUseYn("N");
				}
                // 이용기관이 하나금융투자를 이용할 경우
                if (StringUtils.equals(hfnCdValue, "02")) {
                    useorgInfo.setHnwUseYn("Y");
                } else {
					useorgInfo.setHnwUseYn("N");
				}
                // 이용기관이 하나생명을 이용할 경우
                if (StringUtils.equals(hfnCdValue, "04")) {
                    useorgInfo.setHlfUseYn("Y");
                } else {
					useorgInfo.setHlfUseYn("N");
				}
                // 이용기관이 하나캐피탈을 이용할 경우
                if (StringUtils.equals(hfnCdValue, "05")) {
                    useorgInfo.setHcpUseYn("Y");
                } else {
					useorgInfo.setHcpUseYn("N");
				}
                // 이용기관이 하나카드를 이용할 경우
                if (StringUtils.equals(hfnCdValue, "12")) {
                    useorgInfo.setHcdUseYn("Y");
                } else {
					useorgInfo.setHcdUseYn("N");
				}
                // 이용기관이 하나저축은행을 이용할 경우
                if (StringUtils.equals(hfnCdValue, "14")) {
                    useorgInfo.setHsvUseYn("Y");
                } else {
					useorgInfo.setHsvUseYn("N");
				}
                // 이용기관이 하나멤버스를 이용할 경우
                if (StringUtils.equals(hfnCdValue, "99")) {
                    useorgInfo.setHmbUseYn("Y");
                } else {
					useorgInfo.setHmbUseYn("N");
				}

                useorgStatCdChangeRequest.setHbnUseYn(useorgInfo.getHbnUseYn());
                useorgStatCdChangeRequest.setHnwUseYn(useorgInfo.getHnwUseYn());
                useorgStatCdChangeRequest.setHlfUseYn(useorgInfo.getHlfUseYn());
                useorgStatCdChangeRequest.setHcpUseYn(useorgInfo.getHcpUseYn());
                useorgStatCdChangeRequest.setHcdUseYn(useorgInfo.getHcdUseYn());
                useorgStatCdChangeRequest.setHsvUseYn(useorgInfo.getHsvUseYn());
                useorgStatCdChangeRequest.setHmbUseYn(useorgInfo.getHmbUseYn());

                // 이용기관 탈퇴 승인 완료시, 관계사 USE_YN=W,Y -> N 로 변경
                settingRepository.updateUseorgStatCdAfterDelAplv(useorgStatCdChangeRequest);

                /*** Redis ENTR_CD DEL ***/
                // 이용기관이 하나은행을 이용할 경우
                String hfnCd = "";
                if(StringUtils.equals(useorgInfo.getHbnUseYn(), "Y")){
                    hfnCd = "hbk";
					useorgEventHandler.eventCreate("delete", useorgDetailRequest, hfnCdValue);
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나금융투자를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHnwUseYn(), "Y")){
                    hfnCd = "hnw";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나생명을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHlfUseYn(), "Y")){
                    hfnCd = "hlf";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나캐피탈을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHcpUseYn(), "Y")){
                    hfnCd = "hcp";
					useorgEventHandler.eventCreate("delete", useorgDetailRequest, hfnCdValue);
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나카드를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHcdUseYn(), "Y")){
                    hfnCd = "hcd";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나저축은행을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHsvUseYn(), "Y")){
                    hfnCd = "hsv";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나멤버스를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHmbUseYn(), "Y")){
                    hfnCd = "hmb";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

			} else if (StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORGEXP")) {
                //이용 기관 승인 전 정보 세팅
                UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest = new UseorgRequest.UseorgStatCdChangeRequest();
                useorgStatCdChangeRequest.setUserKey(aplvInfo.getAplvReqCd());
                useorgStatCdChangeRequest.setUseorgStatCd("OK");
                useorgStatCdChangeRequest.setRegUserName(aplvApprovalRequest.getRegUserName());
                useorgStatCdChangeRequest.setRegUserId(aplvApprovalRequest.getProcId());

                UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
                useorgDetailRequest.setUserKey(aplvInfo.getAplvReqCd());
                UseorgVO useorgInfo = settingRepository.selectUseorg(useorgDetailRequest);

                // 이용기관이 하나은행을 이용할 경우
                if (StringUtils.equals(hfnCdValue, "01")) {
                    useorgInfo.setHbnUseYn("Y");
                }
                // 이용기관이 하나금융투자를 이용할 경우
                if (StringUtils.equals(hfnCdValue, "02")) {
                    useorgInfo.setHnwUseYn("Y");
                }
                // 이용기관이 하나생명을 이용할 경우
                if (StringUtils.equals(hfnCdValue, "04")) {
                    useorgInfo.setHlfUseYn("Y");
                }
                // 이용기관이 하나캐피탈을 이용할 경우
                if (StringUtils.equals(hfnCdValue, "05")) {
                    useorgInfo.setHcpUseYn("Y");
                }
                // 이용기관이 하나카드를 이용할 경우
                if (StringUtils.equals(hfnCdValue, "12")) {
                    useorgInfo.setHcdUseYn("Y");
                }
                // 이용기관이 하나저축은행을 이용할 경우
                if (StringUtils.equals(hfnCdValue, "14")) {
                    useorgInfo.setHsvUseYn("Y");
                }
                // 이용기관이 하나멤버스를 이용할 경우
                if (StringUtils.equals(hfnCdValue, "99")) {
                    useorgInfo.setHmbUseYn("Y");
                }

				useorgStatCdChangeRequest.setHbnUseYn(useorgInfo.getHbnUseYn());
				useorgStatCdChangeRequest.setHnwUseYn(useorgInfo.getHnwUseYn());
				useorgStatCdChangeRequest.setHlfUseYn(useorgInfo.getHlfUseYn());
				useorgStatCdChangeRequest.setHcpUseYn(useorgInfo.getHcpUseYn());
				useorgStatCdChangeRequest.setHcdUseYn(useorgInfo.getHcdUseYn());
				useorgStatCdChangeRequest.setHsvUseYn(useorgInfo.getHsvUseYn());
				useorgStatCdChangeRequest.setHmbUseYn(useorgInfo.getHmbUseYn());

                // 이용기관 탈퇴 승인 완료시, 관계사 USE_YN=W,Y -> N 로 변경
                settingRepository.updateUseorgStatCdAfterExpireAplv(useorgStatCdChangeRequest);

                /*** Redis ENTR_CD Del ***/
                // 이용기관이 하나은행을 이용할 경우
                String hfnCd = "";
                if(StringUtils.equals(useorgInfo.getHbnUseYn(), "Y")){
                    hfnCd = "hbk";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나금융투자를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHnwUseYn(), "Y")){
                    hfnCd = "hnw";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나생명을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHlfUseYn(), "Y")){
                    hfnCd = "hlf";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나캐피탈을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHcpUseYn(), "Y")){
                    hfnCd = "hcp";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나카드를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHcdUseYn(), "Y")){
                    hfnCd = "hcd";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나저축은행을 이용할 경우
                if(StringUtils.equals(useorgInfo.getHsvUseYn(), "Y")){
                    hfnCd = "hsv";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                // 이용기관이 하나멤버스를 이용할 경우
                if(StringUtils.equals(useorgInfo.getHmbUseYn(), "Y")){
                    hfnCd = "hmb";
                    RedisCommunicater.useorgRedisDel(useorgInfo.getEntrCd(), hfnCd);
                }

                /*** Redis ENC_KEY Del ***/
                try {
                    RedisCommunicater.encKeyRedisDel(useorgInfo.getEntrCd(), useorgInfo.getEncKey());
                } catch (Exception e) {
					log.error("encKeyRedisDel 에러");
					throw new BusinessException("E026",messageSource.getMessage("E026"));
                }

            } else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APP") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEDIT")){

				AppsRequest appsRequest = new AppsRequest();
				appsRequest.setAppKey(aplvInfo.getAplvReqCd());

				// MOD TABLE
				// 앱 수정 승인 요청시 mod 테이블에 client id 세팅을 안하기 때문에(appsRepository.insertAppMod)
				// mod 테이블의 client id 유무로 등록/수정을 구분할 수 없음
                // 앱수정 구분자 필요
				AppsVO appInfo = settingRepository.selectAppDetailFromModTable(appsRequest);

				// api과금정책 update
                RequestApiVO requestApiVO = new RequestApiVO();
				requestApiVO.setAppKey(appInfo.getAppKey());

                settingRepository.updateDiscount(requestApiVO);

				// 앱 등록/수정 구분 (client id나 app secret이 없으면 앱 등록)
				if (StringUtils.equals(aplvInfo.getAplvDivCd(), "APP"))  {

					//클라이언트 ID, 시크릿 발급
					AppsVO appsVO = settingRepository.selectUseorgEntrCdFromMod(appsRequest);
					System.out.println("[클라이언트ID,시크릿KEY 발급] HFN_CD  : "+appsVO.getHfnCd());
					System.out.println("[클라이언트ID,시크릿KEY 발급] ENTR_CD : "+appsVO.getEntrCd());

					GWResponse oAuthResponse = GWCommunicater.createClientInfo(appsVO.getHfnCd(), appInfo.getAppSvcEnDt(), appsVO.getEntrCd());
					Map<String, Object> OAuthBodyResponse = oAuthResponse.getDataBody();

					String encryptScr	= "";
					String decryptId	= "";
					String buf			= "";

                    String encKey = settingRepository.selectEncKeyFromModByAppKey(appsRequest);

					try {

						encKey = AES256Util.decrypt(encKey);

						buf = (String)OAuthBodyResponse.get("clientId");
						decryptId = AES256Util.decrypt(encKey, buf).split(":")[0];
                        buf = (String)OAuthBodyResponse.get("clientSecret");
                        String decryptScr = AES256Util.decrypt(encKey, buf).split(":")[0];
						encryptScr = AES256Util.encrypt(decryptScr);

					} catch (Exception e) {
						log.error("Client Secret AES256 Decrypt or Encrypt Error", e);
						throw new BusinessException("E026",messageSource.getMessage("E026"));
					}

					AppsRegRequest appsRegRequest = new AppsRegRequest();
					appsRegRequest.setAppKey(appInfo.getAppKey());
					appsRegRequest.setAppNm(appInfo.getAppNm());
                    appsRegRequest.setAccCd(appInfo.getAccCd());
                    appsRegRequest.setAccNo(appInfo.getAccNo());
					appsRegRequest.setAppStatCd("UNUSED");
					appsRegRequest.setAppAplvStatCd("APLV");
					appsRegRequest.setAppClientId(decryptId);
					appsRegRequest.setAppScr(encryptScr);
					appsRegRequest.setAppScrVldDttm(appInfo.getAppSvcEnDt());
					appsRegRequest.setAppSvcStDt(appInfo.getAppSvcStDt());
					appsRegRequest.setAppSvcEnDt(appInfo.getAppSvcEnDt());
					appsRegRequest.setAppCtnt(appInfo.getAppCtnt());
					appsRegRequest.setUserKey(appInfo.getUserKey());
					appsRegRequest.setTermEtdYn(appInfo.getTermEtdYn());
					appsRegRequest.setAppScrReisuYn(appInfo.getAppScrReisuYn());
					appsRegRequest.setReqCancelCtnt(appInfo.getReqCancelCtnt());
					appsRegRequest.setRegDttm(appInfo.getRegDttm());
					appsRegRequest.setRegUser(appInfo.getRegUser());
					appsRegRequest.setRegUserId(appInfo.getRegUser());
					appsRegRequest.setOfficialDocNo(appInfo.getOfficialDocNo());

					// 앱등록 승인 후 처리
					appsService.insertAppAfterAplv(appsRegRequest);

					//api정보, 채널정보를 사용중으로 상태변경
					AppsApiInfoRequest aar = new AppsApiInfoRequest();
					AppsCnlKeyRequest acr = new AppsCnlKeyRequest();
					aar.setAppKey(appsRequest.getAppKey());
					acr.setAppKey(appsRequest.getAppKey());
					appsRepository.updateAppApiInfo(aar);
					appsRepository.updateAppCnlInfo(acr);

					/*** Redis 등록 ***/

					ApiRequest.ApiDetailRequest apiDetailRequest = new ApiRequest.ApiDetailRequest();
					apiDetailRequest.setAppKey(aplvInfo.getAplvReqCd());

					if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APP")){
						// 앱 등록 NOTICE API
						appsRequest.setAppKey(appsRegRequest.getAppKey());
						AppsRsponse appsRsponse = appsService.selectAppDetail(appsRequest);
						appApiEventHandler.eventCreate("create", appsRsponse, aplvApprovalRequest.getHfnCd());
					}

					//Redis에 API SET
					List<ApiVO> apiList = settingRepository.selectAppApiList(apiDetailRequest);

					// Redis에 app 정보 세팅
					RedisCommunicater.appRedisSet(decryptId, "true");

					for(ApiVO apiInfo : apiList){
						RedisCommunicater.appApiRedisSet(decryptId, apiInfo.getApiUrl(), "true");
					}

					//Redis에 APP IP 등록
					List<AppCnlInfoVO> apiChannlList = settingRepository.selectAppChannlList(apiDetailRequest);
					for(AppCnlInfoVO appCnlInfo : apiChannlList){
						RedisCommunicater.appIpRedisSet(decryptId, appCnlInfo.getCnlKey(), "true");
					}

					// API 호출 되지 않도록 Redis set
					RedisCommunicater.appRedisSet(appsRegRequest.getAppClientId(), "false");
					if (null != appInfo.getNewAppClientId() && "" != appInfo.getNewAppClientId()){
						RedisCommunicater.appRedisSet(appInfo.getNewAppClientId(), "false");
					}
				}
				else { // client id나 app secret이 있으면 앱 수정
					// 삭제된 API의 할인 과금 정책 삭제
					settingRepository.deleteDiscountForN(requestApiVO);

					AppsRegRequest regRequest = new AppsRegRequest();
					regRequest.setAppKey(appInfo.getAppKey());
					regRequest.setModUser(appInfo.getRegUser());

					// 메인 테이블에서 앱 정보 가져와서 백업 테이블에 저장
					appsRepository.backupAppInfo(regRequest);

					// 메인 테이블에 있던 기존 앱 정보 삭제
                    appsRepository.deleteApp(regRequest);

					// 수정 테이블에서 앱 정보를 가져와서 메인 테이블에 저장
					appsRepository.insertApp(regRequest);
					AppsRegRequest rr = new AppsRegRequest();
					rr.setAppKey(regRequest.getAppKey());
					rr.setAppAplvStatCd("APLV");
					appsRepository.appStatChange(rr);

					// 수정 테이블에 있던 기존 앱 정보 삭제
					appsRepository.deleteAppMod(regRequest);

					// app_api 테이블, app_channl 테이블에서 api정보, 채널 정보 가져옴
					AppsRequest appsReq = new AppsRequest();
					appsReq.setAppKey(appInfo.getAppKey());

					List<AppApiInfoVO> newApiList = appsRepository.selectAppApiInfoWait(appsReq);
					List<AppsApiInfoRequest> newAPIs = new ArrayList<AppsApiInfoRequest>();
					for(AppApiInfoVO a : newApiList) {
						AppsApiInfoRequest r = new AppsApiInfoRequest();
						r.setSeqNo(a.getSeqNo());
						r.setAppKey(a.getAppKey());
						r.setApiId(a.getApiId());
						r.setHfnCd(a.getHfnId());
						r.setRegDttm(a.getRegDttm());
						r.setRegUser(a.getRegUser());
						r.setModDttm(a.getModDttm());
						r.setModUser(a.getModUser());
						r.setUseFl(a.getUseFl());

						newAPIs.add(r);
					}

					List<AppApiInfoVO> oldApiList = appsRepository.selectAppApiInfo(appsReq);
					List<AppsApiInfoRequest> oldAPIs = new ArrayList<AppsApiInfoRequest>();
					for(AppApiInfoVO a : oldApiList) {
						AppsApiInfoRequest r = new AppsApiInfoRequest();
						r.setSeqNo(a.getSeqNo());
						r.setAppKey(a.getAppKey());
						r.setApiId(a.getApiId());
						r.setHfnCd(a.getHfnId());
						r.setRegDttm(a.getRegDttm());
						r.setRegUser(a.getRegUser());
						r.setModDttm(a.getModDttm());
						r.setModUser(a.getModUser());
						r.setUseFl(a.getUseFl());

						oldAPIs.add(r);
					}

					List<AppCnlInfoVO> newCnlList = appsRepository.selectAppCnlInfoWait(appsReq);
					List<AppsCnlKeyRequest> newCNLs = new ArrayList<AppsCnlKeyRequest>();
					for(AppCnlInfoVO a : newCnlList) {
						AppsCnlKeyRequest r = new AppsCnlKeyRequest();
						r.setSeqNo(a.getSeqNo());
						r.setCnlKey(a.getCnlKey());
						r.setAppKey(a.getAppKey());
						r.setRegDttm(a.getRegDttm());
						r.setRegUser(a.getRegUser());
						r.setModDttm(a.getModDttm());
						r.setModUser(a.getModUser());
						r.setUseFl(a.getUseFl());

						newCNLs.add(r);
					}

					List<AppCnlInfoVO> oldCnlList = appsRepository.selectAppCnlInfo(appsReq);
					List<AppsCnlKeyRequest> oldCNLs = new ArrayList<AppsCnlKeyRequest>();
					for(AppCnlInfoVO a : oldCnlList) {
						AppsCnlKeyRequest r = new AppsCnlKeyRequest();
						r.setSeqNo(a.getSeqNo());
						r.setCnlKey(a.getCnlKey());
						r.setAppKey(a.getAppKey());
						r.setRegDttm(a.getRegDttm());
						r.setRegUser(a.getRegUser());
						r.setModDttm(a.getModDttm());
						r.setModUser(a.getModUser());
						r.setUseFl(a.getUseFl());

						oldCNLs.add(r);
					}

					// API 상세정보
					List<ApiVO> newAPIList = new ArrayList<>();
					for (AppApiInfoVO appApiInfo : newApiList) {
						ApiRequest apiRequest = new ApiRequest();
						apiRequest.setApiId(appApiInfo.getApiId());
						ApiVO apiInfo = apiRepository.selectApiDetalInfo(apiRequest);
						newAPIList.add(apiInfo);
					}

					// API 상세정보
					List<ApiVO> oldAPIList = new ArrayList<>();
					for (AppApiInfoVO appApiInfo : oldApiList) {
						ApiRequest apiRequest = new ApiRequest();
						apiRequest.setApiId(appApiInfo.getApiId());
						ApiVO apiInfo = apiRepository.selectApiDetalInfo(apiRequest);
						oldAPIList.add(apiInfo);
					}

					// 앱수정 승인 API
					if(StringUtils.equals(aplvApprovalRequest.getHfnCd(),"01") || StringUtils.equals(aplvApprovalRequest.getHfnCd(),HfnEnum.HCP.value())) {
						AppsRsponse appsRsponse = appsService.selectAppDetail(appsRequest);
						AppApiChangeDTO appApiChangeDTO = new AppApiChangeDTO(newAPIList, oldAPIList, appsRsponse);
						appApiEventHandler.eventCreate("update", appApiChangeDTO, aplvApprovalRequest.getHfnCd());
					}


					// Redis에서 api 정보, 채널 정보 등록
					AppsRegRequest regR = new AppsRegRequest();
					regR.setAppKey(appInfo.getAppKey());
					regR.setRegUserId(appInfo.getRegUser());
                    regR.setRegUser(appInfo.getRegUser());
					regR.setAppClientId(appInfo.getAppClientId());
					regR.setApiList(newAPIs);
					regR.setExApiList(oldAPIs);
					regR.setCnlKeyList(newCNLs);
					regR.setExCnlKeyList(oldCNLs);
					regR.setRedisApiList(newAPIList);
					regR.setRedisExApiList(oldAPIList);

					// 기존 api정보, 채널정보 삭제하고, 새로운 정보로 업데이트
					appsService.delAppCnlInfo(regR);
					appsService.delAppApiInfo(regR);
					appsService.insertAppCnlKey(regR, "Y");
					appsService.insertAppApiInfo(regR, "Y");
					appsRepository.updateAppApiInfoByNForWait(regR);
					appsService.updateRedisInfo(regR);

					// App 상태에 따라 redis set
					if(StringUtils.equals(appInfo.getAppStatCd(), "CLOSE") || StringUtils.equals(appInfo.getAppStatCd(), "UNUSED")) {
						RedisCommunicater.appRedisSet(appInfo.getAppClientId(), "false");
						if (null != appInfo.getNewAppClientId() && "" != appInfo.getNewAppClientId()){
							RedisCommunicater.appRedisSet(appInfo.getNewAppClientId(), "false");
						}
					} else {
						RedisCommunicater.appRedisSet(appInfo.getAppClientId(), "true");
						if ( null != appInfo.getNewAppClientId() && "" !=  appInfo.getNewAppClientId()) {
							RedisCommunicater.appRedisSet(appInfo.getNewAppClientId(), "true");
						}
					}
				}

			}else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APPDEL")) { // 앱 삭제 승인일때

				// 변경 테이블에서 앱 정보 가져옴
				AppsRequest appsRequest = new AppsRequest();
				appsRequest.setAppKey(aplvInfo.getAplvReqCd());
				AppsVO appInfo = settingRepository.selectAppDetailFromModTable(appsRequest);

				AppsRegRequest appsRegRequest = new AppsRegRequest();
				appsRegRequest.setAppKey(appInfo.getAppKey());
				appsRegRequest.setAppNm(appInfo.getAppNm());
				appsRegRequest.setAppStatCd("DEL");
				appsRegRequest.setAppAplvStatCd("APLV");
				appsRegRequest.setAppClientId(appInfo.getAppClientId());
				appsRegRequest.setNewAppClientId(appInfo.getNewAppClientId());
				appsRegRequest.setAppScr(appInfo.getAppScr());
				appsRegRequest.setNewAppScr(appInfo.getNewAppScr());
				appsRegRequest.setAppSvcStDt(appInfo.getAppSvcStDt());
				appsRegRequest.setAppSvcEnDt(appInfo.getAppSvcEnDt());
				appsRegRequest.setAppCtnt(appInfo.getAppCtnt());
				appsRegRequest.setUserKey(appInfo.getUserKey());
				appsRegRequest.setTermEtdYn(appInfo.getTermEtdYn());
				appsRegRequest.setAppScrReisuYn(appInfo.getAppScrReisuYn());
				appsRegRequest.setReqCancelCtnt(appInfo.getReqCancelCtnt());
				appsRegRequest.setRegDttm(appInfo.getRegDttm());
				appsRegRequest.setRegUser(appInfo.getRegUser());

				// 앱 삭제 승인 NOTICE API : APP_API REDIS 상태 변경되기전 호출
				appsRequest.setAppKey(appsRegRequest.getAppKey());
				AppsRsponse appsRsponse = appsService.selectAppDetail(appsRequest);

				//원복
				appsRequest.setAppKey(aplvInfo.getAplvReqCd());
				appApiEventHandler.eventCreate("delete", appsRsponse, aplvApprovalRequest.getHfnCd());

				// 메인테이블 상태 CLOSE->DEL 변경
				appInfo.setAppStatCd("DEL");
				appsRepository.appStatChange(appsRegRequest);

				// 히스토리 테이블에 이력 삽입
				appsRepository.backupAppInfo(appsRegRequest);

				// 변경 테이블에 있는 내용 삭제
				appsRepository.deleteAppMod(appsRegRequest);

				// Redis app api del
				List<AppCnlInfoVO> appCnlInfoList = appsRepository.selectAppCnlInfo(appsRequest);
				List<AppApiInfoVO> appApiInfoList = appsRepository.selectAppApiInfo(appsRequest);

				// API 상세정보
				List<ApiVO> list = new ArrayList<>();
				for (AppApiInfoVO appApiInfo : appApiInfoList) {
					ApiRequest apiRequest = new ApiRequest();
					apiRequest.setApiId(appApiInfo.getApiId());
					ApiVO apiInfo = apiRepository.selectApiDetalInfo(apiRequest);
					list.add(apiInfo);
				}

				// api과금정책 update
				RequestApiVO requestApiVO = new RequestApiVO();
				requestApiVO.setAppKey(appInfo.getAppKey());

				settingRepository.updateDelDiscount(requestApiVO);

				// Redis Delete
				try {
					for(ApiVO apiInfo : list){
						RedisCommunicater.appApiRedisDel(appInfo.getAppClientId(), apiInfo.getApiUrl());
					}
				} catch (Exception e) {
					for(ApiVO apiInfo : list){
						RedisCommunicater.appApiRedisSet(appInfo.getAppClientId(), apiInfo.getApiUrl(), "true");
					}
				}
				try {
					for(AppCnlInfoVO cnlInfo : appCnlInfoList){
						RedisCommunicater.appIpRedisDel(appInfo.getAppClientId(), cnlInfo.getCnlKey());
					}
				} catch (Exception e) {
					for(AppCnlInfoVO cnlInfo : appCnlInfoList){
						RedisCommunicater.appIpRedisSet(appInfo.getAppClientId(), cnlInfo.getCnlKey(), "true");
					}
				}
				try {
					RedisCommunicater.appRedisDel(appInfo.getAppClientId());
				} catch (Exception e) {
					RedisCommunicater.appRedisSet(appInfo.getAppClientId(),"true");
				}
			}
			else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEXP")){
				//앱 기간 연장
				AppsRequest appsRequest = new AppsRequest();
				appsRequest.setAppKey(aplvInfo.getAplvReqCd());
				appsRequest.setAppAplvStatCd("APLV");
				appsRequest.setModUser(aplvApprovalRequest.getRegUserName());
				appsRequest.setModUserId(aplvApprovalRequest.getProcId());


				AppsVO appInfo = settingRepository.selectAppDetailInfo(appsRequest);

				String appSvcEnDt = DateUtil.getCalDateYear(appInfo.getAppSvcEnDt(), aplvSvcAddYear);
				appsRequest.setAppSvcEnDt(appSvcEnDt);

				settingRepository.updateAppsSvcEnDt(appsRequest);

				List<AppsVO> appScrList = settingRepository.selectAppSrcHisList(appsRequest);

				for( AppsVO appScrInfo : appScrList){
					AppsModRequest appsModRequest = new AppsModRequest();
					appsModRequest.setAppKey(aplvInfo.getAplvReqCd());
					appsModRequest.setAppClientId(appScrInfo.getAppClientId());
					appsModRequest.setAppScr(appScrInfo.getAppScr());
					appsModRequest.setAppScrVldDttm(appSvcEnDt);
					appsModRequest.setModUser(aplvApprovalRequest.getRegUserName());
					appsModRequest.setModUserId(aplvApprovalRequest.getProcId());
					settingRepository.updateAppsScrHis(appsModRequest);

					//앱 기간 연장
					AppsVO appsVO = settingRepository.selectUseorgEntrCdFromInfo(appsRequest);
					GWCommunicater.createClientExpire(appsVO.getHfnCd(), appScrInfo.getAppClientId(), appSvcEnDt, appsVO.getEntrCd());
				}
			} else if (StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_M") || StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_S")) {

				// 과금결재신청
				SettlementRequest settlementRequest = new SettlementRequest();

				String bilMonth = aplvInfo.getAplvReqCd().split("_")[0];
				String hfnCd = aplvInfo.getAplvReqCd().split("_")[1];
				String appKey = "";

				settlementRequest.setBilMonth(bilMonth);
				settlementRequest.setHfnCd(hfnCd);

				if (StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_S")) {
					appKey = aplvInfo.getAplvReqCd().split("_")[2];
					settlementRequest.setAppKey(appKey);
				}

				settlementRepository.feeApprovalRequestConfirm(settlementRequest);

				// 수수료징수정보 history 저장
				settlementService.setFeeHis(settlementRequest);
			}
		}
	}

	/*********** 이메일(가입승인_사업자) *************/
	public void sendEmailForUseorg(UseorgVO user) throws Exception {

		if (!thisServer.equals("local")) {
			sendMail.initMail();
			String uuid = UUID.randomUUID().toString();
			String authId = String.format("%s", uuid);

			sendMail.setSubject("[하나금융그룹 API Market] 회원가입 승인 메일입니다.");
			String context = setSendContentForUseorg(user.getUseorgId(), authId, "ORG");
			sendMail.setText(context);
			sendMail.addInline();
			sendMail.setFrom(SEND_EMAIL_URL, "관리자");

			String to = "";
			if (thisServer.equals("development") || thisServer.equals("staging")) {
				to = "hsung1004@hanafn.com";
			} else {
				to = user.getUseorgUserEmail();
			}
			sendMail.setTo(to);
			sendMail.send();

			SndCertMgntRequest request = new SndCertMgntRequest();

			request.setUserKey(user.getUserKey());
			request.setCertValidTm("7");
			request.setSendCd("E1");
			request.setSendNo(authId);
			request.setRecvInfo(to);
			request.setSendCtnt(context);

			settingRepository.insertEmailData(request);
		}
	}

	/*********** 이메일(가입승인_개인외국인) *************/
	public void sendEmailForFUser(SignUpRequest.UserSingnUpRequest user) throws Exception {
		if (!thisServer.equals("local")) {
			sendMail.initMail();
			String uuid = UUID.randomUUID().toString();
			String authId = String.format("%s", uuid);

			sendMail.setSubject("[하나금융그룹 API Market] 회원가입 승인 메일입니다.");
			String context = setSendContentForUseorg(user.getUserId(), authId, "F");
			sendMail.setText(context);
			sendMail.addInline();
			sendMail.setFrom(SEND_EMAIL_URL, "관리자");

			String to = "";
			if (thisServer.equals("development") || thisServer.equals("staging")) {
				to = "hsung1004@hanafn.com";
			} else {
				to = user.getUserEmail();
			}
			sendMail.setTo(to);
			sendMail.send();

			SndCertMgntRequest request = new SndCertMgntRequest();

			request.setUserKey(user.getUserKey());
			request.setCertValidTm("7");
			request.setSendCd("E1");
			request.setSendNo(authId);
			request.setRecvInfo(to);
			request.setSendCtnt(context);

			settingRepository.insertEmailData(request);
		}
	}

	private String setSendContentForUseorg(String id, String authId, String userGb) {

		StringBuffer content = new StringBuffer();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, +7);
        Date date = calendar.getTime();
        String days7 = new SimpleDateFormat("yyyy년 MM월 dd일").format(date);

        String authUrlSetting = authUrl + "";

		authUrlSetting = authUrlSetting + "/#/emailAplv?authId=" + authId+"&userGb="+ userGb;

		content.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"padding:0; margin:0; font-size:0; line-height:0;\">");
		content.append("	<tr>");
		content.append("		<td><img src=\"cid:mail_logo.png\"></td>");
		content.append("	</tr>");
		content.append("	<tr>");
		content.append("		<td>");
		content.append("			<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:480px; padding:0; margin:auto; font-size:0; line-height:0;\">");
		content.append("				<tr>");
		content.append("					<td style=\"padding:52px 0 26px 0; border-bottom:1px solid #eaeaea; font-size:30px; line-height:1; color:#000000; text-align:center; letter-spacing:-1px; font-family:'나눔고딕',NanumGothic,'맑은고딕','Malgun Gothic','돋움',Dotum,Helvetica,'Apple SD Gothic Neo',Sans-serif;\">회원 가입 승인 메일입니다</td>");
		content.append("				</tr>");
		content.append("				<tr>");
		content.append("					<td style=\"padding:23px 0 100px 0; font-size:14px; line-height:1.7; color:#8d8d8f; text-align:left; font-family:'나눔고딕',NanumGothic,'맑은고딕','Malgun Gothic','돋움',Dotum,Helvetica,'Apple SD Gothic Neo',Sans-serif;\">");
		content.append("						<p style=\"padding:0; margin:0;\">");
		content.append("				API Market회원으로 가입해주셔서 감사드립니다.<br>");
		content.append("				API Market에서 제공하는 다양한 서비스를 이용하실 수 있습니다.");
		content.append("				</p>");
		content.append("						<p style=\"padding:15px 0 15px; margin:0;\">가입 승인을 위해 아래 링크를 클릭하세요.</p>");
		content.append("						<p style=\"padding:0; margin:0;\">");
		content.append("							<a href=\""+authUrlSetting+"\" target=\"_blank\" style=\"color:#ca874b\">"+authUrlSetting+"</a>");
		content.append("						</p>");
		content.append("						<p style=\"padding:15px 0 20px 0; margin:0;\">링크 클릭이 안되면, URL전체를 복사하셔서 주소창에 붙여넣기 해주시기 바랍니다.</p>");
		content.append("						<p style=\"padding:15px 20px; margin:0; color:#333333; line-height:1.9; background:#f4f4f4\">");
		content.append("				- ID : "+id+"<br>");
		content.append("		        - 가입 승인기간 : "+days7+"");
		content.append("				</p>");
		content.append("						<p style=\"padding:20px 0 15px; margin:0;\">가입 승인 기간내에 승인을 하지 않으시면, 회원 가입이 취소됩니다.</p>");
		content.append("						<p style=\"padding:0; margin:0;\">감사합니다.</p>");
		content.append("					</td>");
		content.append("				</tr>");
		content.append("			</table>");
		content.append("		</td>");
		content.append("	</tr>");
		content.append("	<tr>");
		content.append("		<td><img src=\"cid:mail_footer.png\"></td>");
		content.append("	</tr>");
		content.append("</table>");

		return content.toString();
	}

	/*********** 가입완료 *************/
	public boolean authComplete(UserRequest.UserAuthRequest userRequest) {

		SndCertMgntRequest request = new SndCertMgntRequest();
		request.setSendNo(userRequest.getAuthKey());

		UserVO userData = settingRepository.selectCertMgnt(request);

		if (userData.getSendNo().equals(userRequest.getAuthKey())) {
			// 유효기간 체크
			if (DateUtil.getDayDiff(DateUtil.getCurrentDate(), userData.getExpireDttm()) > 0) {

				request.setUserKey(userData.getUserKey());

				settingRepository.authCompleteCertMgnt(request);
				if ("F".equals(userRequest.getUserGb())) {
					settingRepository.authCompleteUserInfo(request);
				} else {
					settingRepository.authCompleteUseorgInfo(request);
				}
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	/*********** 이메일 재전송 *************/
	public void sendEmail(UserRequest.UserAuthRequest userRequest) throws Exception {

		SndCertMgntRequest request = new SndCertMgntRequest();
		request.setSendNo(userRequest.getAuthKey());

		UserVO userData = settingRepository.selectCertMgnt(request);

		if ("F".equals(userRequest.getUserGb())) {
			UserRequest.UserRegistRequest userSelect = new UserRequest.UserRegistRequest();
			userSelect.setUserKey(userData.getUserKey());

			UserVO uservo = settingRepository.selectUserForId(userSelect);

			SignUpRequest.UserSingnUpRequest user = new SignUpRequest.UserSingnUpRequest();

			user.setUserKey(uservo.getUserKey());
			user.setUserId(uservo.getUserId());
			user.setUserEmail(uservo.getUserEmail());

			sendEmailForFUser(user);
		} else {
			UseorgRequest.UseorgDetailRequest useorg = new UseorgRequest.UseorgDetailRequest();

			useorg.setUserKey(userData.getUserKey());

			UseorgVO user = settingRepository.selectUseorg(useorg);

			sendEmailForUseorg(user);
		}
	}

	public void aplvReject(AplvRequest.AplvRejectRequest aplvRejectRequest) throws Exception {
		AplvRequest.AplvHisDetailRequest aplvHisDetailRequest = new AplvRequest.AplvHisDetailRequest();
		aplvHisDetailRequest.setAplvSeqNo(aplvRejectRequest.getAplvSeqNo());
		aplvHisDetailRequest.setProcUser(aplvRejectRequest.getProcId());

		AplvHisVO aplvHisInfo = settingRepository.selectAplvHisDetailInfo(aplvHisDetailRequest);

		//승인 내역에 로그인한 계정을 반려 상태로 변경
		AplvRequest.AplvApprovalRequest aplvUpdateRequest = new AplvRequest.AplvApprovalRequest();
		aplvUpdateRequest.setAplvSeqNo(aplvRejectRequest.getAplvSeqNo());
		aplvUpdateRequest.setSeqNo(aplvHisInfo.getSeqNo());
		aplvUpdateRequest.setAplvStatCd("REJECT");
		aplvUpdateRequest.setRejectCtnt(aplvRejectRequest.getRejectCtnt());
		aplvUpdateRequest.setRegUserName(aplvRejectRequest.getRegUserName());
		aplvUpdateRequest.setProcId(aplvRejectRequest.getProcId());
		aplvUpdateRequest.setProcUser(aplvRejectRequest.getProcUser());

		settingRepository.updateAplvHis(aplvUpdateRequest);
		settingRepository.updateAplvStatCdChange(aplvUpdateRequest);

		//이용기관, 앱 상태 변경
		AplvRequest.AplvDetailRequest aplvDetailRequest = new AplvRequest.AplvDetailRequest();
		aplvDetailRequest.setAplvSeqNo(aplvRejectRequest.getAplvSeqNo());
		aplvDetailRequest.setHfnCd(aplvRejectRequest.getHfnCd());

		AplvVO aplvInfo = settingRepository.selectAplv(aplvDetailRequest);

		String userKey = "";
		if(StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORG") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNAPLV") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNDEL")) {
			userKey = aplvInfo.getAplvReqCd();
		} else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APP") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEXP") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPDEL") || StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEDIT")) {
			AppsRequest appsRequest = new AppsRequest();
			appsRequest.setAppKey(aplvInfo.getAplvReqCd());
			userKey = appsRepository.selectAppModDetail(appsRequest).getUserKey();
		}

		log.debug("@@ aplvRejectRequest.getHfnCd(): >> " + aplvRejectRequest.getHfnCd());
		aplvInfo.setHfnCd(aplvRejectRequest.getHfnCd());
		log.debug("@@ aplvInfo.setHfnCd(): >> " + aplvInfo.getHfnCd());

		// 반려결과 이메일전송
		aplvEmail(aplvInfo, userKey, "반려", aplvRejectRequest.getRejectCtnt());

		if(StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORG")){
			//이용기관 상태 반려로 변경
			UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest = new UseorgRequest.UseorgStatCdChangeRequest();
			useorgStatCdChangeRequest.setUserKey(aplvInfo.getAplvReqCd());
			useorgStatCdChangeRequest.setRegUserName(aplvRejectRequest.getRegUserName());
			useorgStatCdChangeRequest.setRegUserId(aplvRejectRequest.getProcId());
			useorgStatCdChangeRequest.setHfnCd(aplvRejectRequest.getHfnCd());

			settingRepository.updateUseorgStatCdReject(useorgStatCdChangeRequest);

			UserRequest.UserIdUpdateRequest userIdUpdateRequest = new UserRequest.UserIdUpdateRequest();
			userIdUpdateRequest.setUserKey(aplvInfo.getRegId());
			settingRepository.updateUserReject(userIdUpdateRequest);
		}else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APP")
				|| StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEDIT")
				|| StringUtils.equals(aplvInfo.getAplvDivCd(), "APPDEL")){
			//앱 상태 반려로 변경
			AppsModRequest appsModRequest = new AppsModRequest();
			appsModRequest.setAppKey(aplvInfo.getAplvReqCd());
			appsModRequest.setAppAplvStatCd("REJECT");
			appsModRequest.setModUser(aplvRejectRequest.getRegUserName());
            appsModRequest.setModUserId(aplvRejectRequest.getProcId());
			settingRepository.updateAppsStatCdReject(appsModRequest);

			AppsRegRequest appsRegRequest = new AppsRegRequest();
			appsRegRequest.setAppKey(aplvInfo.getAplvReqCd());
			appsRegRequest.setModUser(aplvRejectRequest.getRegUserName());
            appsRegRequest.setModUserId(aplvRejectRequest.getProcId());
            appsRegRequest.setAplvSeqNo(aplvInfo.getAplvSeqNo());

			// 반려를 처리할 때 appApiInfo의 WAIT 상태의 값을 N으로 변경
			appsRepository.updateAppApiInfoByNForWait(appsRegRequest);
			// 반려를 처리할 때 appChannelInfo의 WAIT 상태의 값을 N으로 변경
			appsRepository.updateAppChannelInfoByNForWait(appsRegRequest);

			// 할인 정책 원복
			RequestApiVO requestApiVO = new RequestApiVO();
			requestApiVO.setAppKey(aplvInfo.getAplvReqCd());
			settingRepository.updateDiscountForWAIT(requestApiVO);
			settingRepository.updateDiscountByYForN(requestApiVO);

			appsRegRequest.setAppAplvStatCd("REJECT");
			appsRegRequest.setAplvSeqNo(aplvInfo.getAplvSeqNo());

			// 히스토리 테이블에 이력 삽입
			appsRepository.backupAppInfoReject(appsRegRequest);

			// 반려를 처리할 때 app_mod의 값을 삭제
			appsRepository.deleteAppMod(appsRegRequest);

		}else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "APPEXP")){
			//앱 승인 상태 반려로 변경 및 기간 연장 여부 변경
			AppsModRequest appsModRequest = new AppsModRequest();
			appsModRequest.setAppKey(aplvInfo.getAplvReqCd());
			appsModRequest.setAppAplvStatCd("REJECT");
			appsModRequest.setTermEtdYn("N");
			appsModRequest.setModUser(aplvRejectRequest.getRegUserName());
			appsModRequest.setModUserId(aplvRejectRequest.getProcId());
			settingRepository.updateAppsStatCdRejectAndTermEtdYn(appsModRequest);
		} else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNAPLV")){
			UseorgRequest.HfnAplvRejectRequest request = new UseorgRequest.HfnAplvRejectRequest();
			request.setUserKey(aplvInfo.getAplvReqCd());
			request.setHfnCd(aplvRejectRequest.getHfnCd());
			settingRepository.useorgHfnAplvReject(request);
		} else if(StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNDEL")){
			UseorgRequest.HfnAplvRejectRequest request = new UseorgRequest.HfnAplvRejectRequest();
			request.setUserKey(aplvInfo.getAplvReqCd());
			request.setHfnCd(aplvRejectRequest.getHfnCd());
			settingRepository.useorgHfnDelReject(request);
		} else if (StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_M") || StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_S")) {
			// 과금결재신청반려

			SettlementRequest settlementRequest = new SettlementRequest();

			String bilMonth = aplvInfo.getAplvReqCd().split("_")[0];
			String hfnCd = aplvInfo.getAplvReqCd().split("_")[1];
			String appKey = "";

			if (StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_S")) {
				appKey = aplvInfo.getAplvReqCd().split("_")[2];
				settlementRequest.setAppKey(appKey);
			}

			settlementRequest.setHfnCd(hfnCd);
			settlementRequest.setBilMonth(bilMonth);

			settlementRepository.setFeeCollectionReject(settlementRequest);

			// 수수료징수정보 history 저장
			settlementService.setFeeHis(settlementRequest);
		}
	}

	// 승인/반려 결과 이메일전송
	private void aplvEmail(AplvVO aplvInfo, String userKey, String aplvState, String rejectCtnt) throws Exception {
		if (!thisServer.equals("local")) {
			if (!StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_M") && !StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_S")) {

				String to = "";

				if (thisServer.equals("development") || thisServer.equals("staging")) {
					to = "hsung1004@hanafn.com";
				} else {
					to = aplvInfo.getUseorgUserEmail();
				}

				if (!"".equals(to) && to != null) {
					sendMail.initMail();
					sendMail.setSubject("[하나금융그룹 API Market] 승인 결과를 알려 드립니다.");
					String context = setSendContentForAplv(aplvInfo, aplvState, rejectCtnt);
					sendMail.setText(context);
					sendMail.addInline();
					sendMail.setFrom(SEND_EMAIL_URL, "관리자");
					sendMail.setTo(to);
					sendMail.send();
					String sendCd = "";
					if(StringUtils.equals(aplvState, "승인")) {
						sendCd = "E3";
					} else if(StringUtils.equals(aplvState, "반려")) {
						sendCd = "E4";
					}
					SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
					sndCertMgntRequest.setUserKey(userKey);
					sndCertMgntRequest.setRecvInfo(to);
					sndCertMgntRequest.setSendCd(sendCd);
					sndCertMgntRequest.setSendCtnt(context);

					settingRepository.insertMailSendData(sndCertMgntRequest);
				}
			}
		}
	}

	private String setSendContentForAplv(AplvVO aplvInfo, String aplvState, String rejectCtnt) {

		String aplvDivCd = aplvInfo.getAplvDivCd();
		String appUseorg = "";

		UseorgVO useorg = null;
		log.debug("useorg1 >> " + useorg);

		if (StringUtils.equals(aplvDivCd, "APP")
				|| StringUtils.equals(aplvDivCd, "APPEDIT")
				|| StringUtils.equals(aplvDivCd, "APPDEL")
				|| StringUtils.equals(aplvDivCd, "APPEXP")
		)
		{
			appUseorg = "APP";

			log.debug("useorg3 >> " + useorg);
			useorg = settingRepository.getAppUseorg(aplvInfo.getAplvReqCd());

			log.debug("useorg2 >> " + useorg);
		}
		else
		{
			appUseorg = "USEORG";
			useorg = settingRepository.getUseorg(aplvInfo.getAplvReqCd());
		}

		StringBuffer content = new StringBuffer();
		content.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"padding:0; margin:0; font-size:0; line-height:0;\">");
		content.append("  <tr>");
		content.append("    <td><img src=\"cid:mail_logo.png\"></td>");
		content.append("  </tr>");
		content.append("  <tr>");
		content.append("    <td>");
		content.append("      <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:480px; padding:0; margin:auto; font-size:0; line-height:0;\">");
		content.append("        <tr>");
		content.append("          <td style=\"padding:52px 0 26px 0; border-bottom:1px solid #eaeaea; font-size:30px; line-height:1; color:#000000; text-align:center; letter-spacing:-1px; font-family:'나눔고딕',NanumGothic,'맑은고딕','Malgun Gothic','돋움',Dotum,Helvetica,'Apple SD Gothic Neo',Sans-serif;\">승인결과 안내입니다.</td>");
		content.append("        </tr>");
		content.append("        <tr>");
		content.append("          <td style=\"padding:23px 0 100px 0; font-size:14px; line-height:1.7; color:#8d8d8f; text-align:center; font-family:'나눔고딕',NanumGothic,'맑은고딕','Malgun Gothic','돋움',Dotum,Helvetica,'Apple SD Gothic Neo',Sans-serif;\">");
		content.append("            <p style=\"padding:0 0 20px 0; margin:0;\">");
		content.append("        요청하신 승인 결과를 안내드립니다.");
		content.append("            </p>");
		content.append("						<p style=\"padding:15px 20px; margin:0; color:#333333; line-height:1.9; background:#f4f4f4\">");
		content.append("				- 승인구분 : "+getAplvCdTxt(aplvDivCd)+"<br>");

		if ("APP".equals(appUseorg)) {
			content.append("		        - 앱 명 : " + useorg.getAppNm() + "<br>");
		}
		content.append("		        - 관계사명 : "+HfnEnum.resolve(aplvInfo.getHfnCd()).getName()+"<br>");
		content.append("				- 이용기관명 : "+useorg.getUseorgNm()+"<br>");
		content.append("				- 승인결과 : "+aplvState+"<br>");

		if ("반려".equals(aplvState)) {
			content.append("				- 반려사유 : "+rejectCtnt+"<br>");
		}

		content.append("				</p>");
		content.append("            <p style=\"padding:20px 0 15px; margin:0;\">");
		content.append("        하나금융그룹 API Market을 이용해 주셔서 감사합니다.<br>");
		content.append("        더욱 편리한 서비스를 제공하기 위해 항상 최선을 다하겠습니다.");
		content.append("        </p>");
		content.append("            <p style=\"padding:0; margin:0;\">감사합니다.</p>");
		content.append("          </td>");
		content.append("        </tr>");
		content.append("      </table>");
		content.append("    </td>");
		content.append("  </tr>");
		content.append("  <tr>");
		content.append("    <td><img src=\"cid:mail_footer.png\"></td>");
		content.append("  </tr>");
		content.append("</table>");

		return content.toString();
	}

	private String getAplvCdTxt(String aplvDivCd) {

		String aplvCdTxt = "";

		switch(aplvDivCd) {
			case "USEORG":
				aplvCdTxt = "이용기관 등록";
				break;
			case "APP":
				aplvCdTxt = "APP 등록";
				break;
			case "APPEXP":
				aplvCdTxt = "APP 기간연장";
				break;
			case "APPDEL":
				aplvCdTxt = "APP 삭제";
				break;
			case "APPEDIT":
				aplvCdTxt = "APP 수정";
				break;
			case "HFNAPLV":
				aplvCdTxt = "API 이용 신청";
				break;
			case "HFNDEL":
				aplvCdTxt = "API 이용 중지 신청";
				break;
			default:
				log.error("@@ Aplv Div Cd 에러");
		}

		return aplvCdTxt;
	}

	/*
	 * ******************************유효성 검사******************************
	 * */

	public void passwordCheck(String password){

		if(!password.matches(".*[A-Z].*")){
			throw new BusinessException("E006",messageSource.getMessage("E006"));
		}

		if(!password.matches(".*[0-9].*")){
			throw new BusinessException("E007",messageSource.getMessage("E007"));
		}

		if(password.length() < 8 || password.length() > 16){
			throw new BusinessException("E007",messageSource.getMessage("E008"));
		}
	}

	public HfnUserVO userAplvLevelInfo (HfnUserRequest.HfnUserAplvLevelRequest request) {

		HfnUserVO user = settingRepository.userAplvLevelInfo(request);
		return user;
	}

	public void aplvLineSetting(AplvRequest.AplvLineSetRequest request) {

		// 해당 결재 정보에서 APLV_SEQ_NO를 가져옴
		AplvRequest.AplvRegistRequest req = new AplvRequest.AplvRegistRequest();
		req.setAplvReqCd(request.getAplvReqCd());
		req.setAplvDivCd(request.getAplvDivCd());
		req.setAplvSeqNo(request.getAplvSeqNo());
		AplvVO aplv = settingRepository.selectAplvForAplvReqCdForHfnAplv(req);

		AppsRegRequest appsRegRequest = new AppsRegRequest();

		if (StringUtils.equals(request.getAplvDivCd(), "APP") || StringUtils.equals(request.getAplvDivCd(), "APPEDIT")) {
			// 서비스 기간, 공문번호 업데이트
			String appKey = settingRepository.selectAppKeyByAplvSeqNo(aplv.getAplvSeqNo());
			appsRegRequest.setAppKey(appKey);
			appsRegRequest.setAppSvcStDt(request.getAppSvcStDt());
			appsRegRequest.setAppSvcEnDt(request.getAppSvcEnDt());
			appsRegRequest.setOfficialDocNo(request.getOfficialDocNo());

			appsRepository.updateAppModSvcTermAndOfficialDocNo(appsRegRequest);
			for (RequestApiVO api : request.getRequestApiList()) {
				api.setRegUserId(request.getRegId());
				settingRepository.updateApi(api);
			}
		}

		List<HfnUserVO> aplvLine = request.getAplvList();
		boolean check = false;

		List<String> userparamList = new ArrayList<>();

		Collections.reverse(aplvLine);
		// 해당 결제선 리스트에 있는 유저가 관계사 사람인지, 결재 권한이 있는지 확인
		for(HfnUserVO user : aplvLine)
		{
			user.setHfnCd(request.getHfnCd());
			HfnUserVO hfnUser = settingRepository.selectHfnUserFromHfnLine(user);

			// 결재선에 지정되어있고 결재권한이 있는 모든 SIGN_LEVEL>10 유저에게 승인 생성
			if(Integer.parseInt(hfnUser.getSignLevel()) > 10)
			{
				AplvRequest.AplvHisRegistRequest aplvHisRegistRequest = new AplvRequest.AplvHisRegistRequest();
				aplvHisRegistRequest.setAplvSeqNo(aplv.getAplvSeqNo());
				aplvHisRegistRequest.setRegUserName(request.getRegUser());
				aplvHisRegistRequest.setHfnCd(hfnUser.getHfnCd());
				aplvHisRegistRequest.setRegUserId(request.getRegId());
				aplvHisRegistRequest.setProcUser(hfnUser.getUserNm());
				aplvHisRegistRequest.setProcId(hfnUser.getUserKey());

				settingRepository.insertAplvHis(aplvHisRegistRequest);

				if(thisServer.equals("production")) {
					// 운영모드일 경우 메시지허브 발송대상 추가
					userparamList.add(userParamGen(hfnUser.getHfnCd(), hfnUser.getHfnId(), hfnUser.getUserNm()));
				}

				check = true;
			}
		}

		if(check == true) {

			// 결재 히스토리에서 해당 결재 정보 가져옴
			AplvRequest.AplvHisDetailRequest aplvHisDetailRequest = new AplvRequest.AplvHisDetailRequest();
			aplvHisDetailRequest.setAplvSeqNo(aplv.getAplvSeqNo());
			aplvHisDetailRequest.setProcUser(request.getRegId());

			AplvHisVO aplvHisInfo = settingRepository.selectAplvHisDetailInfo(aplvHisDetailRequest);

			// 접수자들 리스트 가져옴
			HfnInfoRequest hfnInfoRequest = new HfnInfoRequest();
			hfnInfoRequest.setHfnCd(request.getHfnCd());
			List<HfnUserVO> managerList = settingRepository.selectHfnManager(hfnInfoRequest);

			//승인 내역에 접수자들을 승인 처리로 변경
			AplvRequest.AplvApprovalRequest aplvUpdateRequest = new AplvRequest.AplvApprovalRequest();
			aplvUpdateRequest.setAplvSeqNo(aplv.getAplvSeqNo());
			aplvUpdateRequest.setSeqNo(aplvHisInfo.getSeqNo());
			aplvUpdateRequest.setAplvStatCd("APPROVAL");
			aplvUpdateRequest.setRejectCtnt("");
			aplvUpdateRequest.setRegUserName(request.getRegUser());
			aplvUpdateRequest.setProcId(request.getRegId());

			for (HfnUserVO manager : managerList)
			{
				aplvUpdateRequest.setProcId(manager.getUserKey());
				aplvUpdateRequest.setProcUser(manager.getUserNm());
				settingRepository.updateAplvHis(aplvUpdateRequest);
			}

			// 승인 정보 가져옴
			AplvVO aplvInfo = settingRepository.selectAplvDetail(request.getAplvSeqNo());

			if(thisServer.equals("development") || thisServer.equals("staging")) {
				// 개발, 품질인 경우 메시지허브 발송 테스트 user set
				userparamList.add(userParamGen( "07", "2144", "이혜성"));
				userparamList.add(userParamGen( "07", "2744", "반성욱"));
				userparamList.add(userParamGen( "07", "2676", "최진현"));
				userparamList.add(userParamGen( "07", "2600", "염유진"));
			}

			String title = "";
			switch(aplvInfo.getAplvDivCd()) {
				case "USEORG":
					title = "이용기관 등록";
					break;
				case "APP":
					title = "APP 등록";
					break;
				case "APPEXP":
					title = "앱 기간연장";
					break;
				case "APPDEL":
					title = "APP 삭제";
					break;
				case "APPEDIT":
					title = "APP 수정";
					break;
				case "HFNAPLV":
					title = "API 이용신청";
					break;
				case "HFNDEL":
					title = "API 중지신청";
					break;
				case "FEE_M":
					title = "과금다건신청";
					break;
				case "FEE_S":
					title = "과금단건신청";
					break;
				default:
					log.error("@@ Aplv Div Cd 에러");
			}

			String name = "";
			String userKey = "";
			if(StringUtils.equals(aplvInfo.getAplvDivCd(), "USEORG") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNAPLV") || StringUtils.equals(aplvInfo.getAplvDivCd(), "HFNDEL")) { // 이용기관 관련 승인일 경우
				UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
				useorgDetailRequest.setUserKey(aplvInfo.getAplvReqCd());
				name = "이용기관명: " + settingRepository.selectUseorg(useorgDetailRequest).getUseorgNm();
				userKey = aplvInfo.getAplvReqCd();
			} else if (StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_M") || StringUtils.equals(aplvInfo.getAplvDivCd(), "FEE_S")) {
				// 과금승인
				name = "청구월: " + aplvInfo.getAplvReqCd();
				userKey = aplvInfo.getRegUser();

			} else { // 앱 관련 승인 일 경우
				AppsRequest appsRequest = new AppsRequest();
				appsRequest.setAppKey(aplvInfo.getAplvReqCd());
				AppsVO app = appsRepository.selectAppModDetail(appsRequest);
				UseorgRequest.UseorgDetailRequest useorgDetailRequest = new UseorgRequest.UseorgDetailRequest();
				useorgDetailRequest.setUserKey(app.getUserKey());
				UseorgVO useorg = settingRepository.selectUseorg(useorgDetailRequest);

				name = "앱 명: " + app.getAppNm() + "\n" + "이용기관명: " + useorg.getUseorgNm();
				userKey = useorg.getUserKey();
			}

			String ctntMsg = "승인요청: " + title + "\n" +
							 name + "\n" +
							 "신청자: " + aplvInfo.getRegUser() + "\n" +
							 "신청일: " + aplvInfo.getRegDttm().substring(0,19);

			log.debug("@@ hub send Msg = " + ctntMsg);

			if(thisServer.equals("development") || thisServer.equals("staging") || thisServer.equals("production")) {
				// 개발, 품질, 운영일 경우 하나허브 메시지 발송
				hubCommunicator.HubMsgCommunicator(title, ctntMsg, userparamList, userKey, "H2");
			}
		}
	}

	/*** 이용기관 개발자 등록전 있는지 검사 ***/
	public void checkDevDupCheck(UserRequest.RegistDeveloperRequest request){
		int cnt = settingRepository.cntDevDupCheck(request);
		if(cnt >= 1){
			throw new BusinessException("D001",messageSource.getMessage("D001"));
		}
	}

	/*** 다른기관에 등록된 개발자인지 확인 ***/
	public void checkDevExistsOtherUseorg(UserRequest.RegistDeveloperRequest request) {
		int cnt = settingRepository.checkDevExistsOtherUseorg(request);
		if(cnt >= 1){
			throw new BusinessException("D002",messageSource.getMessage("D002"));
		}
	}

	/*** 이용기관 개발자 등록전 있는지 검사 ***/
	public void checkDevExists(UserRequest.RegistDeveloperRequest request){
		int cnt = settingRepository.cntDevExists(request);
		if(cnt == 1){
			log.debug("Pass");
		}
		else if(cnt == 0){
			throw new BusinessException("D003",messageSource.getMessage("D003"));
		}
		else {
			log.error("조건에 만족하는 개발자 다수존재 에러");
			throw new BusinessException("E026",messageSource.getMessage("E026"));
		}
	}

	/*** 이용기관 개발자 등록 ***/
	public void regDeveloper(UserRequest.RegistDeveloperRequest request) {

		UserRequest.UserRegistRequest userRegistRequest = new UserRequest.UserRegistRequest();
		userRegistRequest.setEntrCd(request.getEntrCd());
		userRegistRequest.setUserId(request.getUserId());
		UserVO userInfo = settingRepository.selectUserForUserId(userRegistRequest);
		request.setUserKey(userInfo.getUserKey());

		settingRepository.regDeveloperInfo(request);
		settingRepository.regDeveloperLogin(request);
		settingRepository.regDeveloperRole(request);
	}

	/*** 개발자 리스트 ***/
	public UserRsponse.DeveloperListResponse selectDeveloperList(UserRequest.RegistDeveloperRequest request) {

		List<UserVO> devList = settingRepository.selectDeveloperList(request);

		for (UserVO userVO : devList) {
			// 복호화
			try {
				// 전화번호
				if (userVO.getUserTel() != null && !"".equals(userVO.getUserTel())) {
					String decryptedUserTel = AES256Util.decrypt(userVO.getUserTel());
					userVO.setUserTel(decryptedUserTel);
					log.debug("복호화 - 이름: {}", decryptedUserTel);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}

			try {
				// 이메일
				if (userVO.getUserEmail() != null && !"".equals(userVO.getUserEmail())) {
					String decryptedUserEmail = AES256Util.decrypt(userVO.getUserEmail());
					userVO.setUserEmail(decryptedUserEmail);
					log.debug("복호화 - 이름: {}", decryptedUserEmail);
				}
			} catch ( Exception e ) {
				log.error(e.getMessage());
				throw new BusinessException("E026",messageSource.getMessage("E026"));
			}
		}

		UserRsponse.DeveloperListResponse response = new UserRsponse.DeveloperListResponse();
		response.setDevList(devList);

		return response;
	}

	/*** 개발자 삭제 ***/
	public void deleteDeveloper(UserRequest.RegistDeveloperRequest request) {

		UserRequest.UserRegistRequest userRegistRequest = new UserRequest.UserRegistRequest();
		userRegistRequest.setUserId(request.getUserId());
		UserVO userInfo = settingRepository.selectUserForUserId(userRegistRequest);
		request.setUserKey(userInfo.getUserKey());

		settingRepository.deleteDeveloperInfo(request);
		settingRepository.deleteDeveloperLogin(request);
		settingRepository.deleteDeveloperRole(request);
	}

	/** 로그인 테이블 관련 **/
	public void updateUserLoginPwd(UserLoginRequest.UpdatePwdRequest request){
		settingRepository.updateUserLoginPwd(request);
		settingRepository.insertPwHisUser(request);
	}

	/** 결재선 목록 조회 **/
	public HfnUserRsponsePaging getAplLineList (HfnUserRequest.HfnUserAplvLevelRequest request) {
		List<HfnUserVO> user = settingRepository.getAplvLevelList(request);

		HfnUserRsponsePaging pagingData = new HfnUserRsponsePaging();
		pagingData.setList(user);

		return pagingData;
	}

	/*********** 개인회원 아이디 찾기 *************/
	public void searchUserId(UserVO user) throws Exception {
		UserRequest.searchUserRequest searchData = new UserRequest.searchUserRequest();

		searchData.setUserNm(AES256Util.encrypt(user.getUserNm()));
		searchData.setUserEmail(AES256Util.encrypt(user.getUserEmail()));

		UserVO userData = settingRepository.searchUserId(searchData);


		if (userData != null) {
			sendEmailForFoundId(searchData.getUserEmail(), userData.getUserId());
		} else {
			log.info("아이디찾기 : 조회된 정보가 없습니다 " + user);
			throw new BusinessException("L005", messageSource.getMessage("L005"));
		}
	}

	/*********** 사업자회원 아이디 찾기 *************/
	public void searchUserOrgId(UseorgVO user) throws Exception {
		UserRequest.searchUserorgRequest searchData = new UserRequest.searchUserorgRequest();

		searchData.setUseorgNm(user.getUseorgNm());
		searchData.setBrn(user.getBrn());
		searchData.setUseorgUserEmail(AES256Util.encrypt(user.getUseorgUserEmail()));

		UseorgVO userData = settingRepository.searchUserOrgId(searchData);

		if (userData != null) {
			sendEmailForFoundId(searchData.getUseorgUserEmail(), userData.getUseorgId());
		} else {
			log.info("아이디찾기 : 조회된 정보가 없습니다 " + user);
			throw new BusinessException("L005", messageSource.getMessage("L005"));
        }
	}

	/*********** 이메일(아이디찾기) *************/
	public void sendEmailForFoundId(String email, String id) throws Exception {
		if (!thisServer.equals("local")) {
			sendMail.initMail();
			sendMail.setSubject("[하나금융그룹 API Market] 아이디를 알려 드립니다.");
			sendMail.setText(setSendContentForId(id));
			sendMail.addInline();
			sendMail.setFrom(SEND_EMAIL_URL, "관리자");
			if (thisServer.equals("development") || thisServer.equals("staging")) {
				sendMail.setTo("hsung1004@hanafn.com");
			} else {
				sendMail.setTo(email);
			}
			sendMail.send();
		}
	}

	private String setSendContentForId(String id) {

		StringBuffer content = new StringBuffer();

		content.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"padding:0; margin:0; font-size:0; line-height:0;\">");
		content.append("	<tr>");
		content.append("		<td><img src=\"cid:mail_logo.png\"></td>");
		content.append("	</tr>");
		content.append("	<tr>");
		content.append("		<td>");
		content.append("			<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:480px; padding:0; margin:auto; font-size:0; line-height:0;\">");
		content.append("				<tr>");
		content.append("					<td style=\"padding:52px 0 26px 0; border-bottom:1px solid #eaeaea; font-size:30px; line-height:1; color:#000000; text-align:center; letter-spacing:-1px; font-family:'나눔고딕',NanumGothic,'맑은고딕','Malgun Gothic','돋움',Dotum,Helvetica,'Apple SD Gothic Neo',Sans-serif;\">아이디를 알려 드립니다.</td>");
		content.append("				</tr>");
		content.append("				<tr>");
		content.append("					<td style=\"padding:23px 0 100px 0; font-size:14px; line-height:1.7; color:#8d8d8f; text-align:center; font-family:'나눔고딕',NanumGothic,'맑은고딕','Malgun Gothic','돋움',Dotum,Helvetica,'Apple SD Gothic Neo',Sans-serif;\">");
		content.append("						<p style=\"padding:0 0 20px 0; margin:0;\">");
		content.append("				손님께서 요청하신 아이디를 알려 드립니다.");
		content.append("						</p>");
		content.append("						<p style=\"padding:15px 20px; margin:0; color:#666666; font-size:20px; line-height:1.7; background:#f4f4f4\">");
		content.append("				아이디 : <span style=\"color:#303133; font-weight:bold;\">"+id+"</span> ");
		content.append("						</p>");
		content.append("						<p style=\"padding:20px 0 15px; margin:0;\">");
		content.append("				API Market을 이용해 주셔서 감사합니다.<br>");
		content.append("				더욱 편리한 서비스를 제공하기 위해 항상 최선을 다하겠습니다.");
		content.append("				</p>");
		content.append("						<p style=\"padding:0; margin:0;\">감사합니다.</p>");
		content.append("					</td>");
		content.append("				</tr>");
		content.append("			</table>");
		content.append("		</td>");
		content.append("	</tr>");
		content.append("	<tr>");
		content.append("		<td><img src=\"cid:mail_footer.png\"></td>");
		content.append("	</tr>");
		content.append("</table>");

		return content.toString();
	}

	/*********** 개인회원 비밀번호 찾기 *************/
	public void searchUserPassword(UserVO user) throws Exception {
		UserRequest.searchUserRequest searchData = new UserRequest.searchUserRequest();

		searchData.setUserId(user.getUserId());
		searchData.setUserNm(AES256Util.encrypt(user.getUserNm()));
		searchData.setUserEmail(AES256Util.encrypt(user.getUserEmail()));

		UserVO userData = settingRepository.searchUserPassword(searchData);

		if (userData != null) {
			sendUserAuthNum(userData.getUserKey(), user.getUserEmail());
		} else {
			log.error("비밀번호 찾기 : 조회된 정보가 없습니다 " );
			throw new BusinessException("L005", messageSource.getMessage("L005"));
		}
	}

	/*********** 사업자회원 비밀번호 찾기 *************/
	public void searchOrgPassword(UseorgVO user) throws Exception {
		UserRequest.searchUserorgRequest searchData = new UserRequest.searchUserorgRequest();

		searchData.setUseorgId(user.getUseorgId());
		searchData.setUseorgNm(user.getUseorgNm());
		searchData.setBrn(user.getBrn());
		searchData.setUseorgUserEmail(AES256Util.encrypt(user.getUseorgUserEmail()));

		String authNum = "0";

		UseorgVO userData = settingRepository.searchOrgPassword(searchData);

		if (userData != null) {
			sendUserAuthNum(userData.getUserKey(), user.getUseorgUserEmail());
		} else {
			log.error("비밀번호 찾기 : 조회된 정보가 없습니다 " );
			throw new BusinessException("L005", messageSource.getMessage("L005"));
		}
	}

	/*********** 비밀번호찾기 이메일 인증번호전송 *************/
	public void sendUserAuthNum(String userKey, String email) throws Exception {
		if (!thisServer.equals("local")) {
			sendMail.initMail();
			String sendNum = sendMail.makeAuthNum();	// 인증번호

			sendMail.setSubject("[하나금융그룹 API Market] 이메일 인증번호를 알려드립니다.");
			String context = setSendContentForUserAuthNum(sendNum);
			sendMail.setText(context);
			sendMail.addInline();
			sendMail.setFrom(SEND_EMAIL_URL, "관리자");
			String to = "";
			if (thisServer.equals("development") || thisServer.equals("staging")) {
				to = "hsung1004@hanafn.com";
			} else {
				to = email;
			}

			if (thisServer.equals("production")) {
				to = AES256Util.encrypt(to);
			}

			sendMail.setTo(to);
			sendMail.send();

			SndCertMgntRequest request = new SndCertMgntRequest();

			request.setUserKey(userKey);
			request.setCertValidTm("3");
			request.setSendCd("E2");
			request.setSendNo(sendNum);

			request.setRecvInfo(to);
			request.setSendCtnt(context);

			settingRepository.insertSndCertMgntByPw(request);
		}
	}

	private String setSendContentForUserAuthNum(String authNum) {

		String threeMinute = DateUtil.getMinute(3);

		threeMinute = threeMinute.substring(0,2) + ":" +threeMinute.substring(2,4);

		StringBuffer content = new StringBuffer();

		content.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"padding:0; margin:0; font-size:0; line-height:0;\">");
		content.append("	<tr>");
		content.append("		<td><img src=\"cid:mail_logo.png\"></td>");
		content.append("	</tr>");
		content.append("	<tr>");
		content.append("		<td>");
		content.append("			<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:480px; padding:0; margin:auto; font-size:0; line-height:0;\">");
		content.append("				<tr>");
		content.append("					<td style=\"padding:52px 0 26px 0; border-bottom:1px solid #eaeaea; font-size:30px; line-height:1; color:#000000; text-align:center; letter-spacing:-1px; font-family:'나눔고딕',NanumGothic,'맑은고딕','Malgun Gothic','돋움',Dotum,Helvetica,'Apple SD Gothic Neo',Sans-serif;\">인증번호를 알려 드립니다.</td>");
		content.append("				</tr>");
		content.append("				<tr>");
		content.append("					<td style=\"padding:23px 0 100px 0; font-size:14px; line-height:1.7; color:#8d8d8f; text-align:center; font-family:'나눔고딕',NanumGothic,'맑은고딕','Malgun Gothic','돋움',Dotum,Helvetica,'Apple SD Gothic Neo',Sans-serif;\">");
		content.append("						<p style=\"padding:0 0 20px 0; margin:0;\">");
		content.append("				아래 인증 번호를 입력 후 인증절차를 완료하세요.");
		content.append("						</p>");
		content.append("						<p style=\"padding:15px 20px; margin:0; color:#666666; font-size:20px; line-height:1.7; background:#f4f4f4\">");
		content.append("				인증번호 : <span style=\"color:#303133; font-weight:bold;\">"+authNum+"</span><br> ");
		content.append("				<span style=\"color:#303133; font-weight:bold; font-size: 15px;\">※ 인증번호는 "+threeMinute+" 까지 유효합니다.</span> ");
		content.append("						</p>");
		content.append("						<p style=\"padding:20px 0 15px; margin:0;\">");
		content.append("				API Market을 이용해 주셔서 감사합니다.<br>");
		content.append("				더욱 편리한 서비스를 제공하기 위해 항상 최선을 다하겠습니다.");
		content.append("				</p>");
		content.append("						<p style=\"padding:0; margin:0;\">감사합니다.</p>");
		content.append("					</td>");
		content.append("				</tr>");
		content.append("			</table>");
		content.append("		</td>");
		content.append("	</tr>");
		content.append("	<tr>");
		content.append("		<td><img src=\"cid:mail_footer.png\"></td>");
		content.append("	</tr>");
		content.append("</table>");

		return content.toString();
	}

	/*********** 개인회원 인증완료 *************/
	public boolean certUpdateUser(UserVO user) throws BusinessException {

		UserRequest.searchUserRequest userRequest = new UserRequest.searchUserRequest();

		userRequest.setUserId(user.getUserId());

		UserVO userData = settingRepository.selectCertMgntUser(userRequest);

		if (userData == null) {
			log.error(messageSource.getMessage("L005"));
			throw new BusinessException("L005", messageSource.getMessage("L005"));
		} else {
			if (userData.getSendNo().equals(user.getAuthInputNum())) {
				// 유효기간 체크
				if (DateUtil.getDayDiffMinute(DateUtil.getCurrentDateTime(), userData.getExpireDttm()) > 0) {
					SndCertMgntRequest request = new SndCertMgntRequest();
					request.setUserKey(userData.getUserKey());
					request.setSeq(userData.getSeq());

					settingRepository.updateCertMgnt(request);
					return true;
				} else {
					return false;
				}
			} else {
				log.error(messageSource.getMessage("E030"));
				throw new BusinessException("E030", messageSource.getMessage("E030"));
			}
		}
	}

	/*********** 사업자회원 인증완료 *************/
	public boolean certUpdateUseorg(UseorgVO user) throws BusinessException {

		UserRequest.searchUserRequest userRequest = new UserRequest.searchUserRequest();

		userRequest.setUserId(user.getUseorgId());

		UseorgVO userData = settingRepository.selectCertMgntUseorg(userRequest);

		if (userData == null) {
			log.error(messageSource.getMessage("L005"));
			throw new BusinessException("L005", messageSource.getMessage("L005"));
		} else {
			if (userData.getSendNo().equals(user.getOrgAuthInputNum())) {
				// 유효기간 체크
				if (DateUtil.getDayDiffMinute(DateUtil.getCurrentDateTime(), userData.getExpireDttm()) > 0) {
					SndCertMgntRequest request = new SndCertMgntRequest();

					request.setUserKey(userData.getUserKey());
					request.setSeq(userData.getSeq());

					settingRepository.updateCertMgnt(request);
					return true;
				} else {
					return false;
				}
			} else {
				log.error(messageSource.getMessage("E030"));
				throw new BusinessException("E030", messageSource.getMessage("E030"));
			}
		}
	}

    /*********** 개인회원 비밀번호변경 *************/
    public void setUserPwd(UserRequest.UserPwdUpdateRequest userPwdUpdateRequest){
        userPwdUpdateRequest.setUserPwd(passwordEncoder.encode(userPwdUpdateRequest.getUserPwd()));

        settingRepository.setUserPwd(userPwdUpdateRequest);
        settingRepository.setUserPwdLogin(userPwdUpdateRequest);
		settingRepository.setUserPwHis(userPwdUpdateRequest);

		// 비밀번호 변경시 로그인잠금 관련 세팅 초기화
		UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest = new UserLoginRequest.UserLoginLockCheckRequest();
		userLoginLockCheckRequest.setUserId(userPwdUpdateRequest.getUserId());
		settingRepository.userLoginLockRelease(userLoginLockCheckRequest);
    }

    /*********** 사업자회원 비밀번호변경 *************/
    public void setOrgPwd(UserRequest.OrgPwdUpdateRequest orgPwdUpdateRequest){
        orgPwdUpdateRequest.setUseorgPwd(passwordEncoder.encode(orgPwdUpdateRequest.getUseorgPwd()));

        settingRepository.setOrgPwd(orgPwdUpdateRequest);
        settingRepository.setOrgPwdLogin(orgPwdUpdateRequest);
		settingRepository.setUseorgPwHis(orgPwdUpdateRequest);

		// 비밀번호 변경시 로그인잠금 관련 세팅 초기화
		UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest = new UserLoginRequest.UserLoginLockCheckRequest();
		userLoginLockCheckRequest.setUserId(orgPwdUpdateRequest.getUseorgId());
		settingRepository.userLoginLockRelease(userLoginLockCheckRequest);
    }

	//------------------------------------------------------------------------------------------------------
	// 개발자공간-API제휴신청
	//------------------------------------------------------------------------------------------------------
	public void insertApiJehu(ApiJehuRequest apiJehuRequest){
    	// 암호화
		try {
			// 이름
			if (apiJehuRequest.getUserNm() != null && !"".equals(apiJehuRequest.getUserNm())) {
				String encryptedUserNm = AES256Util.encrypt(apiJehuRequest.getUserNm());
				apiJehuRequest.setUserNm(encryptedUserNm);
				log.debug("암호화 - 이름: {}", encryptedUserNm);
			}
		} catch ( Exception e ) {
			log.error(e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		try {
			// 계좌번호
			if (apiJehuRequest.getUserTel() != null && !"".equals(apiJehuRequest.getUserTel())) {
				String encryptedUserTel = AES256Util.encrypt(apiJehuRequest.getUserTel());
				apiJehuRequest.setUserTel(encryptedUserTel);
				log.debug("암호화 - 전화번호: {}", encryptedUserTel);
			}
		} catch ( Exception e ) {
			log.error(e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		settingRepository.insertApiJehu(apiJehuRequest);

		// 하나허브 연동(해당 관계사 담당자에게 제휴 관련 메시지허브 푸시알림 보냄)
		HfnInfoRequest hfnInfoRequest = new HfnInfoRequest();
		hfnInfoRequest.setHfnCd(apiJehuRequest.getHfnCd());
		List<HfnUserVO> hfnUserList = settingRepository.selectHfnManager(hfnInfoRequest);
		List<String> userparamList = new ArrayList<>();

		if(thisServer.equals("production")) {
			// 운영용 실제 수신자 리스트 생성
			for(HfnUserVO hfnUser : hfnUserList) {
				userparamList.add(userParamGen(hfnUser.getHfnCd(), hfnUser.getHfnId(), hfnUser.getUserNm()));
			}
		} else if(thisServer.equals("development") || thisServer.equals("staging")) {
			// 개발, 품질일 경우 테스트용 수신자 생성
			userparamList.add(userParamGen("07", "2144", "이혜성"));
			userparamList.add(userParamGen("07", "2744", "반성욱"));
			userparamList.add(userParamGen("07", "2676", "최진현"));
			userparamList.add(userParamGen("07", "2600", "염유진"));
		}

		String qnaType = "";
		String compNm = "";

		try {
			if(StringUtils.equals(apiJehuRequest.getQnaType(), "TECH")) {
				if(StringUtils.isNotBlank(apiJehuRequest.getCompNm()) && StringUtils.isNotEmpty(apiJehuRequest.getCompNm()) && apiJehuRequest.getCompNm() != null) {
					qnaType = "기술문의(" + apiJehuRequest.getCompNm() + "(기업))";
					compNm = "업체명: " + apiJehuRequest.getCompNm() + "\n";
				} else {
					qnaType = "기술문의(" + AES256Util.decrypt(apiJehuRequest.getUserNm()) + "(개인))";
				}
			} else if(StringUtils.equals(apiJehuRequest.getQnaType(), "BIZ")) {
				if(StringUtils.isNotBlank(apiJehuRequest.getCompNm()) && StringUtils.isNotEmpty(apiJehuRequest.getCompNm()) && apiJehuRequest.getCompNm() != null) {
					qnaType = "비즈니스 제안(" + apiJehuRequest.getCompNm() + "(기업))";
					compNm = "업체명: " + apiJehuRequest.getCompNm() + "\n";
				} else {
					qnaType = "비즈니스 제안(" + AES256Util.decrypt(apiJehuRequest.getUserNm()) + "(개인))";
				}
			}

			String returnMsg = "";
			String sendMsg = compNm +
					"신청자ID: " + AES256Util.decrypt(apiJehuRequest.getUserNm()) + "\n" +
					"API명: " + apiJehuRequest.getApiNm() + "\n";

			if(thisServer.equals("development") || thisServer.equals("staging") || thisServer.equals("production")) {
				// 개발, 품질, 운영일 경우 하나허브 메시지 발송
				hubCommunicator.HubMsgCommunicator(qnaType, sendMsg, userparamList, apiJehuRequest.getUserKey(), "H3");
			}
		} catch (Exception e) {
			log.debug("@@ 하나허브 메시지 생성 및 발송 에러");
		}
	}

	//------------------------------------------------------------------------------------------------------
	// 개발자공간-공지사항목록
	//------------------------------------------------------------------------------------------------------
	public NoticeResponse.NoticeListResponse selectNoticeList(NoticeRequest.NoticeListRequest noticeListRequest) {

    	if(noticeListRequest.getPageIdx() == 0) {
    		noticeListRequest.setPageIdx(1);
		}

    	if(noticeListRequest.getPageSize() == 0) {
    		noticeListRequest.setPageSize(20);
		}

    	noticeListRequest.setPageOffset((noticeListRequest.getPageIdx()-1) * noticeListRequest.getPageSize());

    	int totCnt = settingRepository.countNoticeList(noticeListRequest);
		List<NoticeVO> noticeList = settingRepository.selectNoticeList(noticeListRequest);
		NoticeResponse.NoticeListResponse response = new NoticeResponse.NoticeListResponse();
		response.setTotCnt(totCnt);
		response.setSelCnt(noticeList.size());
		response.setPageIdx(noticeListRequest.getPageIdx());
		response.setPageSize(noticeListRequest.getPageSize());
		response.setNoticeList(noticeList);

		return response;
	}

	public NoticeResponse detailNotice(String siteCd, NoticeRequest noticeRequest) {
    	NoticeVO notice = settingRepository.detailNotice(noticeRequest);

    	if(StringUtils.equals(siteCd, "userPortal") || StringUtils.equals(siteCd, "")) {
			notice.setViewCnt(notice.getViewCnt()+1);
			noticeRequest.setViewCnt(notice.getViewCnt());
			settingRepository.increaseNoticeViewCnt(noticeRequest);
		}

		NoticeResponse noticeResponse = new NoticeResponse();
		noticeResponse.setNotice(notice);

		return noticeResponse;
	}

	public FaqResponse selectFaqList(FaqRequest faqRequest) {

		if(faqRequest.getPageIdx() == 0) {
			faqRequest.setPageIdx(faqRequest.getPageIdx()+1);
		}

		if(faqRequest.getPageSize() == 0) {
			faqRequest.setPageSize(20);
		}

		faqRequest.setPageOffset((faqRequest.getPageIdx()-1) * faqRequest.getPageSize());

		int totCnt = settingRepository.countFaqList(faqRequest);

        List<FaqVO> faqList = settingRepository.selectFaqList(faqRequest);
        FaqResponse faqResponse = new FaqResponse();
		faqResponse.setTotCnt(totCnt);
		faqResponse.setSelCnt(faqList.size());
		faqResponse.setPageIdx(faqRequest.getPageIdx());
		faqResponse.setPageSize(faqRequest.getPageSize());
        faqResponse.setFaqList(faqList);
        return faqResponse;
    }

	public FaqResponse detailFaq(FaqRequest faqRequest) {
		FaqVO faq = settingRepository.detailFaq(faqRequest);
		FaqResponse faqResponse = new FaqResponse();
		faqResponse.setFaq(faq);
		return faqResponse;
	}

	/*** QNA 관련 service ***/
	public QnaResponse selectQnaList(QnaRequest qnaRequest) {

		if(qnaRequest.getPageIdx() == 0) {
			qnaRequest.setPageIdx(qnaRequest.getPageIdx()+1);
		}

		if(qnaRequest.getPageSize() == 0) {
			qnaRequest.setPageSize(20);
		}

		qnaRequest.setPageOffset((qnaRequest.getPageIdx()-1) * qnaRequest.getPageSize());

		int totCnt = settingRepository.countQna(qnaRequest);

		List<QnaVO> qnaList = settingRepository.selectQnaList(qnaRequest);

		// 복호화
		for (QnaVO qnaVO : qnaList) {
			try {
				// 이름
				if (qnaVO.getUserNm() != null && !"".equals(qnaVO.getUserNm())) {
					String decrypedUserNm = AES256Util.decrypt(qnaVO.getUserNm());
					qnaVO.setUserNm(decrypedUserNm);
					log.debug("복호화 - 이름: {}", decrypedUserNm);
				}
			} catch ( Exception e ) {
				log.error(e.toString());
				throw new BusinessException("E026", messageSource.getMessage("E026"));
			}

			try {
				// 전화번호
				if (qnaVO.getUserTel() != null && !"".equals(qnaVO.getUserTel())) {
					String decrypedUserTel = AES256Util.decrypt(qnaVO.getUserTel());
					qnaVO.setUserTel(decrypedUserTel);
					log.debug("복호화 - 전화번호: {}", decrypedUserTel);
				}
			} catch ( Exception e ) {
				log.error(e.toString());
				throw new BusinessException("E026", messageSource.getMessage("E026"));
			}
		}

		QnaResponse qnaResponse = new QnaResponse();
		qnaResponse.setTotCnt(totCnt);
		qnaResponse.setSelCnt(qnaList.size());
		qnaResponse.setPageIdx(qnaRequest.getPageIdx());
		qnaResponse.setPageSize(qnaRequest.getPageSize());
		qnaResponse.setQnaList(qnaList);
		return qnaResponse;
	}

	public QnaResponse detailQna(QnaRequest qnaRequest) {
		QnaVO qna = settingRepository.detailQna(qnaRequest);

		// 복호화
		try {
			// 이름
			if (qna.getUserNm() != null && !"".equals(qna.getUserNm())) {
				String decrypedUserNm = AES256Util.decrypt(qna.getUserNm());
				qna.setUserNm(decrypedUserNm);
				log.debug("복호화 - 이름: {}", decrypedUserNm);
			}
		} catch ( Exception e ) {
			log.error(e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		try {
			// 전화번호
			if (qna.getUserTel() != null && !"".equals(qna.getUserTel())) {
				String decrypedUserTel = AES256Util.decrypt(qna.getUserTel());
				qna.setUserTel(decrypedUserTel);
				log.debug("복호화 - 전화번호: {}", decrypedUserTel);
			}
		} catch ( Exception e ) {
			log.error(e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		QnaResponse qnaResponse = new QnaResponse();
		qnaResponse.setQna(qna);
		return qnaResponse;
	}

	// 개발자공간 - 공지사항 최근 공지사항 1개 출력
	public NoticeVO getNoticeFirstOne() {
		return settingRepository.getNoticeFirstOne();
	}

    public void saveChargeDiscountRateList(ChargeDiscountRateListRequest request) {
        List<RequestApiVO> list = request.getList();

        for (RequestApiVO apiVO : list) {
            if (!StringUtils.equals(apiVO.getChargeDiscountRateId(), "")) {
                // 업데이트
				if (StringUtils.equals(apiVO.getIsEdited(), "Y") ) {
					apiVO.setModUser(request.getModUser());
					settingRepository.updateApi(apiVO);
				}
            } else {
                // 신규 등록
                apiVO.setRegUser(request.getRegUser());
                apiVO.setUseFl("Y");
                settingRepository.insertApiChargeDiscountRate(apiVO);
            }
        }
    }

    /** 인증메일 발송 조회 관련 **/
    public SndCertMgntResponse selectCertMailList(SndCertMgntRequest request) {

    	if(request.getPageIdx() == 0) {
    		request.setPageIdx(1);
		}
    	if(request.getPageSize() == 0) {
    		request.setPageSize(20);
		}

    	request.setPageOffset((request.getPageIdx()-1) * request.getPageSize());

    	String userKey = "";
		if(request.getUserId() != null && !StringUtils.equals(request.getUserId(), "")) {
			userKey = settingRepository.selectUserkeyByUserid(request);
		}

    	int totCnt = settingRepository.countCertMailList(request);
		request.setUserKey(userKey);
		List<CertMailVO> certMailList = settingRepository.selectCertMailList(request);

		SndCertMgntResponse sndCertMgntResponse = new SndCertMgntResponse();
		sndCertMgntResponse.setCertMailList(certMailList);
		sndCertMgntResponse.setTotCnt(totCnt);
		sndCertMgntResponse.setSelCnt(certMailList.size());

		return sndCertMgntResponse;

	}

	/** 허브 메시지 재발송 **/
	public void reforwardHub(String seq) {

	}

	/** 인증 메일 등 재발송 **/
	public void reforwardMail(String seq) {
		SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
		sndCertMgntRequest.setSeq(seq);
	}

	// 배치 로그 리스트 페이징 조회
	public BatchLogListResponsePaging selectBatchLogListPaging(BatchLogListRequest request) {
		if(request.getPageIdx() == 0) {
			request.setPageIdx(request.getPageIdx() + 1);
		}

		if(request.getPageSize() == 0){
			request.setPageSize(20);
		}

		request.setPageOffset((request.getPageIdx() - 1) * request.getPageSize());

		int totCnt = settingRepository.countBatchLogList(request);
		List<BatchLogVO> list = settingRepository.selectBatchLogList(request);

		BatchLogListResponsePaging pagingData = new BatchLogListResponsePaging();
		pagingData.setPageIdx(request.getPageIdx());
		pagingData.setPageSize(request.getPageSize());
		pagingData.setList(list);
		pagingData.setTotCnt(totCnt);
		pagingData.setSelCnt(list.size());

		return pagingData;
	}

	/** 관리자 - 트랜잭션 관리 **/
	public TrxResponse selectTrx(TrxRequest request) {
		if(request.getPageIdx() == 0) {
			request.setPageIdx(request.getPageIdx() + 1);
		}

		if(request.getPageSize() == 0){
			request.setPageSize(20);
		}

		request.setPageOffset((request.getPageIdx() - 1) * request.getPageSize());

		int totCnt = settingRepository.cntTrxList(request);
		List<TrxVO> list = settingRepository.selectTrx(request);

		TrxResponse response = new TrxResponse();
		response.setTrxList(list);
		response.setTotCnt(totCnt);
		response.setSelCnt(list.size());

		return response;
	}
}
