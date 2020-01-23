package com.hanafn.openapi.portal.security.service;

import com.hanafn.openapi.portal.cmct.AccountStatusCommunicater;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.dto.SignUpRequest;
import com.hanafn.openapi.portal.security.model.User;
import com.hanafn.openapi.portal.security.repository.RoleRepository;
import com.hanafn.openapi.portal.security.repository.SignupRepository;
import com.hanafn.openapi.portal.security.repository.UserRepository;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.UseorgVO;
import com.hanafn.openapi.portal.views.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class SignupService {

	private static final String FILE_DIR = "/openapi/upload";	// 파일저장경로

	@Autowired
	SignupRepository signupRepository;
	@Autowired
	SettingRepository settingRepository;
	@Autowired
	SettingService settingService;
	@Autowired
	CommonUtil commonUtil;
	@Autowired
	MessageSourceAccessor messageSource;
	@Autowired
	UserRepository userRepository;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	AccountStatusCommunicater accountStatusCommunicater;

	public void insertSignup(SignUpRequest signUpRequest){

		try {
			signupRepository.insertSignupUseorg(signUpRequest);

			// Creating user's account
			User user = new User(signUpRequest.getUserId(), signUpRequest.getUserNm(), signUpRequest.getPassword(), signUpRequest.getUserTel());

			signUpRequest.setPassword(passwordEncoder.encode(user.getPassword()));

			signupRepository.insertUserSignup(signUpRequest);

			UserVO userInfo = signupRepository.selectUserForUserIdAndWait(signUpRequest);

			signUpRequest.setUserKey(userInfo.getUserKey());
			signupRepository.insertUserRoleForUseorg(signUpRequest);

			//승인 내역 쌓기
			insertAplvUseorg(signUpRequest);
			signupRepository.updateSignupUseorgRegId(signUpRequest);
		} catch( Exception e ) {
			log.error("회원가입 에러 : " + e.toString());
			throw new BusinessException("E025", messageSource.getMessage("E025"));
		}
	}

	public void insertAplvUseorg(SignUpRequest signUpRequest){

		AplvRequest.AplvRegistRequest aplvRequest = new AplvRequest.AplvRegistRequest();
		aplvRequest.setAplvReqCd(signUpRequest.getUserKey());
		aplvRequest.setAplvDivCd("USEORG");
		aplvRequest.setAplvReqCtnt(signUpRequest.getUseorgNm());
		aplvRequest.setRegUserName(signUpRequest.getUserNm());
		aplvRequest.setRegUserId(signUpRequest.getUserKey());

		settingService.insertAplv(aplvRequest);
	}

	// 이용자포털 회원ID 중복 체크
	public int userIdDupCheck(UserRequest.UserDupCheckRequest request) {

		// PORTAL_USER_LOGIN : 개인ID+기관ID
		UserRequest.UserDupCheckRequest dupCheckRequest = new UserRequest.UserDupCheckRequest();
		dupCheckRequest.setUserId(request.getUserId().toLowerCase());

		int idDupCheck = settingRepository.userIdDupCheck(dupCheckRequest);

		// TMP_HBK_IDCHECK : 은행API포털 회원ID (타기관 사용 방지)
		dupCheckRequest.setUseorgId(request.getUserId().toLowerCase());
		idDupCheck += settingRepository.tmpHbkIdDupCheck(dupCheckRequest);

		return idDupCheck;
	}

	/*********************** 개인 사용자 SignUpService ************************/
	public void insertUser(SignUpRequest.UserSingnUpRequest signUpRequest){

		// Creating user's account
		User user = new User(signUpRequest.getUserKey(), signUpRequest.getUserId(), signUpRequest.getUserNm(), signUpRequest.getUserPwd());

		// 1. 패스워드 암호화
		try {
			signUpRequest.setUserPwd(passwordEncoder.encode(user.getPassword()));
		} catch ( Exception e ) {
			log.error("개인 회원가입 비밀번호 암호화 에러 : " + e.toString());
			throw new BusinessException("E025", messageSource.getMessage("E025"));
		}

		// 2. 탈퇴유저 유효기간내 재가입방지 Logic
		int checkDropUserOrNot = signupRepository.checkDropUserOrNot(signUpRequest);
		if(checkDropUserOrNot > 0) {
			log.error(messageSource.getMessage("L009"));
			throw new BusinessException("L009", messageSource.getMessage("L009"));
		}

		// 외국인 가입자면 WAIT로 하고 이메일인증 처리
		if("F".equals(signUpRequest.getUserGb())) {
			signUpRequest.setUserStatCd("WAIT");
		} else {
			signUpRequest.setUserStatCd("OK");
		}

		// 암호화
		try {
			// 이름
			if (signUpRequest.getUserNm() != null && !"".equals(signUpRequest.getUserNm())) {
				String encryptedUseorgUserNm = AES256Util.encrypt(signUpRequest.getUserNm());
				signUpRequest.setUserNm(encryptedUseorgUserNm);
				log.debug("암호화 - 이름: {}", encryptedUseorgUserNm);
			}
		} catch ( Exception e ) {
			log.error(messageSource.getMessage("E026") + e.getMessage());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		try {
			// 휴대전화번호
			if (signUpRequest.getUserTel() != null && !"".equals(signUpRequest.getUserTel())) {
				String encryptedUseorgUserTel = AES256Util.encrypt(signUpRequest.getUserTel());
				signUpRequest.setUserTel(encryptedUseorgUserTel);
			}
		} catch ( Exception e ) {
			log.error(messageSource.getMessage("E026") + e.getMessage());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		try {
			// 이메일
			if (signUpRequest.getUserEmail() != null && !"".equals(signUpRequest.getUserEmail())) {
				String encryptedUseorgUserEmail = AES256Util.encrypt(signUpRequest.getUserEmail());
				signUpRequest.setUserEmail(encryptedUseorgUserEmail);
				log.debug("암호화 - 이메일: {}", encryptedUseorgUserEmail);
			}
		} catch ( Exception e ) {
			log.error(messageSource.getMessage("E026") + e);
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		try {
			signupRepository.insertUser(signUpRequest);
			signupRepository.insertUserRoleForUser(signUpRequest);
			signupRepository.insertLogin(signUpRequest);
			signupRepository.insertPwHisUser(signUpRequest);
		} catch( Exception e ) {
			log.error("개인 회원가입 에러 : " + e.toString());
			throw new BusinessException("E025", messageSource.getMessage("E025"));
		}

		if ("F".equals(signUpRequest.getUserGb())) {
			try {
				signUpRequest.setUserKey(signupRepository.selectUser(signUpRequest));
				settingService.sendEmailForFUser(signUpRequest);
			} catch(Exception e) {
				log.error(e.toString());
				throw new BusinessException("E026", messageSource.getMessage("E026"));
			}
		}

		// 운영서버 반영
		if(StringUtils.equals(signUpRequest.getUserGb(), "K") && StringUtils.isNotBlank(signUpRequest.getUserDi())){
			try {
				SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
				sndCertMgntRequest.setSendCd("S1");		// 본인인증 코드
				sndCertMgntRequest.setSendNo(signUpRequest.getUserDi());
				sndCertMgntRequest.setResultCd(signUpRequest.getUserResSeq());
				sndCertMgntRequest.setUserKey(signUpRequest.getUserKey());
				signupRepository.insertSndCertMgntForSelfAuth(sndCertMgntRequest);
			} catch (Exception e){
				log.error(e.toString());
				throw new BusinessException("E025", messageSource.getMessage("E026"));
			}
		}
	}

	public void signupUserDupCheck(SignUpRequest.UserSingnUpRequest request){
		UserRequest.UserDupCheckRequest userDupCheckRequest = new UserRequest.UserDupCheckRequest();
		userDupCheckRequest.setUserId(request.getUserId());
		userDupCheckRequest.setUserEmail(request.getUserEmail());
		UserRsponse.UserDupCheckResponse userEmailDupCheckResponse = settingService.userEmailDupCheck(userDupCheckRequest);

		int idDupCheck = userIdDupCheck(userDupCheckRequest);
		if(idDupCheck > 0){
			log.error(messageSource.getMessage("E003"));
			throw new BusinessException("E003",messageSource.getMessage("E003"));
		}
		if(StringUtils.equals(userEmailDupCheckResponse.getUserEmailDupYn(), "Y")){
			log.error(messageSource.getMessage("E027"));
			throw new BusinessException("E027",messageSource.getMessage("E027"));
		}
	}

	public void signupUseorgEmailDupCheck(SignUpRequest.UserSingnUpRequest request){
		UserRequest.UserDupCheckRequest userDupCheckRequest = new UserRequest.UserDupCheckRequest();
		userDupCheckRequest.setUserId(request.getUserId());
		userDupCheckRequest.setUserEmail(request.getUserEmail());

		UserRsponse.UserDupCheckResponse userEmailDupCheckResponse = settingService.useorgEmailDupCheck(userDupCheckRequest);

		if(StringUtils.equals(userEmailDupCheckResponse.getUserEmailDupYn(), "Y")){
			log.error(messageSource.getMessage("E027"));
			throw new BusinessException("E027",messageSource.getMessage("E027"));
		}
	}

	public void signupUseorgEmailDupCheckWhenSignUp(SignUpRequest.UserSingnUpRequest request){
		UserRequest.UserDupCheckRequest userDupCheckRequest = new UserRequest.UserDupCheckRequest();
		userDupCheckRequest.setUserId(request.getUserId());
		userDupCheckRequest.setUserEmail(request.getUserEmail());

		// 암호화
		try {
			if (request.getUserEmail() != null && !"".equals(request.getUserEmail())) {
				String encryptedUseorgUserEmail = AES256Util.encrypt(request.getUserEmail());
				userDupCheckRequest.setUserEmail(encryptedUseorgUserEmail);
			}
		} catch ( Exception e ) {
			log.error("사업자 회원가입 이메일 관련 에러 : " + e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		int dupCnt = signupRepository.useorgEmailDupCheckWhenSignUp(userDupCheckRequest);

		if( dupCnt > 0 ){
			log.error(messageSource.getMessage("E027"));
			throw new BusinessException("E027",messageSource.getMessage("E027"));
		}
	}

	public void signupUserDupCheckUpdate(SignUpRequest.UserSingnUpRequest request){
		UserRequest.UserDupCheckRequest userDupCheckRequest = new UserRequest.UserDupCheckRequest();
		userDupCheckRequest.setUserId(request.getUserId());
		userDupCheckRequest.setUserEmail(request.getUserEmail());
		UserRsponse.UserDupCheckResponse userEmailDupCheckResponse = settingService.userEmailDupCheckUpdate(userDupCheckRequest);

		if(StringUtils.equals(userEmailDupCheckResponse.getUserEmailDupYn(), "Y")){
			log.error(messageSource.getMessage("E027"));
			throw new BusinessException("E027",messageSource.getMessage("E027"));
		}
	}

	/*********************** 법인사용자 SignUpService ************************/

	// 실제 사용중 - 이용기관 생성 등록
	public void insertUseorg(SignUpRequest.UseorgSignUpRequest request, MultipartFile multipartfile) throws Exception {

		// 파일저장
		String fileName = fileSave(multipartfile);

		request.setUseorgUpload(fileName);

		// 패스워드 암호화
		request.setUseorgPwd(passwordEncoder.encode(request.getUseorgPwd()));

		// 탈퇴 후 재가입 방지 로직

		// encKey 세팅
		String encKey = commonUtil.generateEncKey(request);
		request.setEncKey(encKey);

		// 이용기관 ENTR_CD 생성
		SecureRandom random = new SecureRandom();
		int randomValue = random.nextInt(999);
		String id = null;
		try {
			id = StringUtils.upperCase(request.getUseorgId()).substring(0, 3);
		} catch (Exception e){
			log.error(e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}
		int seq = (settingRepository.countUseorgTable() + 1) % 10000;
		request.setEntrCd(id + String.format("%03d", randomValue) + String.format("%04d", seq));

		// 암호화
		try {
			// 이름
			if (request.getUseorgUserNm() != null && !"".equals(request.getUseorgUserNm())) {
				String encryptedUseorgUserNm = AES256Util.encrypt(request.getUseorgUserNm());
				request.setUseorgUserNm(encryptedUseorgUserNm);
				log.debug("암호화 - 이름: {}", encryptedUseorgUserNm);
			}
		} catch ( Exception e ) {
			log.error(e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		try {
			// 휴대전화번호
			if (request.getUseorgUserTel() != null && !"".equals(request.getUseorgUserTel())) {
				String encryptedUseorgUserTel = AES256Util.encrypt(request.getUseorgUserTel());
				request.setUseorgUserTel(encryptedUseorgUserTel);
			}
		} catch ( Exception e ) {
			log.error(e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		try {
			// 이메일
			if (request.getUseorgUserEmail() != null && !"".equals(request.getUseorgUserEmail())) {
				String encryptedUseorgUserEmail = AES256Util.encrypt(request.getUseorgUserEmail());
				request.setUseorgUserEmail(encryptedUseorgUserEmail);
				log.debug("암호화 - 이메일: {}", encryptedUseorgUserEmail);
			}
		} catch ( Exception e ) {
			log.error(e.toString());
			throw new BusinessException("E026", messageSource.getMessage("E026"));
		}

		// 이용기관 등록 (USEORG_STAT_CD = APLV)
		signupRepository.insertUseorg(request);
		signupRepository.insertLoginUseorg(request);
		signupRepository.insertRoleUseorg(request);
		signupRepository.insertPwHisUseorg(request);

		// 승인 등록
		insertUseorgAplv(request);

		if(StringUtils.equals(request.getUseorgGb(), "SOLE") && StringUtils.isNotBlank(request.getUserDi())){
			try {
				SndCertMgntRequest sndCertMgntRequest = new SndCertMgntRequest();
				sndCertMgntRequest.setSendCd("S1");	// 본인인증 코드
				sndCertMgntRequest.setSendNo(request.getUserDi());
				sndCertMgntRequest.setResultCd(request.getUserResSeq());
				sndCertMgntRequest.setUserKey(request.getUserKey());
				signupRepository.insertSndCertMgntForSelfAuth(sndCertMgntRequest);
			} catch (Exception e){
				log.error(e.toString());
				throw new BusinessException("E026", messageSource.getMessage("E026"));
			}
		}

		UseorgVO useorgInfo = signupRepository.selectUseorg(request);

		settingService.sendEmailForUseorg(useorgInfo);
	}

	// 파일저장
	private String fileSave(MultipartFile multipartfile) throws IOException {

		String path = "";

		if (multipartfile != null && !multipartfile.getOriginalFilename().isEmpty()) {

			String fileName = multipartfile.getOriginalFilename();

			String uuid = UUID.randomUUID().toString();
			String subPath = String.format("%s", uuid);
			File tmpDir = new File(getDir(), subPath);
			if(!tmpDir.isDirectory()) {
				tmpDir.mkdirs();
			}

			File target = new File(tmpDir, fileName);

			if (target.exists()) {

				fileName = System.currentTimeMillis() + "_" + fileName;
				target = new File (FILE_DIR + '\\' + fileName);
			}

			byte[] bytes = multipartfile.getBytes();
			FileOutputStream fos = new FileOutputStream(target);
			fos.write(bytes);
			fos.close();

			path = target.getAbsolutePath();
		}
		return path;
	}

	// 파일 디렉토리 생성
	public File getDir() {

		File f = new File(FILE_DIR);
		if(!f.isDirectory()) {
			f.mkdirs();
		}

		return f;
	}

	// 실제 사용중 - 이용기관 등록시 승인 생성
	public void insertUseorgAplv(SignUpRequest.UseorgSignUpRequest request){

		AplvRequest.AplvRegistRequest aplvRequest = new AplvRequest.AplvRegistRequest();
		aplvRequest.setAplvReqCd(request.getUserKey());
		aplvRequest.setAplvDivCd("USEORG");
		aplvRequest.setAplvReqCtnt(request.getUseorgNm());
		aplvRequest.setRegUserName(request.getRegUser());
		aplvRequest.setRegUserId(request.getRegId());

		settingService.insertAplv(aplvRequest);
	}
}
