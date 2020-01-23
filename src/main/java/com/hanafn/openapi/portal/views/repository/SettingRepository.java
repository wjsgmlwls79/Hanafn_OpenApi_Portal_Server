package com.hanafn.openapi.portal.views.repository;

import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.vo.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SettingRepository {

	String getUserKeyByEntrCd(String values);
	/*
	 *******************************이용기관******************************
	 */
	UseorgVO selectUseorg(UseorgRequest.UseorgDetailRequest useorgDetailRequest);

	int countUseorgList(UseorgRequest useorgRequest);
	List<UseorgVO> selectUseorgList(UseorgRequest useorgRequest);
	List<UseorgVO> selectUseorgListHfn(UseorgRequest useorgRequest);
	int useorgDupCheckForUseorgCd(UseorgRequest useorgRequest);

	int useorgDupCheckForBrn(UseorgRequest.UseorgDupCheckRequest useorgDupRequest);
	void insertUseorg(UseorgRequest.UseorgRegistRequest useorgRegistRequest);
	void updateUseorg(UseorgRequest.UseorgUpdateRequest useorgRequest);
	void insertPwHisUseorg(UseorgRequest.UseorgUpdateRequest useorgRequest);
	void updateLoginUser(UseorgRequest.UseorgUpdateRequest useorgRequest);
	void updateUseorgStatCdChange(UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest);
	void updateUseorgStatCdAfterAplv(UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest);
	void updateUseorgStatCdAfterDelAplv(UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest);
	void updateUseorgStatCdAfterExpireAplv(UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest);
	List<UseorgVO> selectUseorgAllList(UseorgRequest useorgRequest);
	List<UseorgVO> selectUseorgAllList();
	List<UseorgVO> selectUseorgAllListByHfn(UseorgRequest useorgRequest);
	void useorgHfnAplv(UseorgRequest.HfnAplvRequest request);
	void useorgHfnAplvReject(UseorgRequest.HfnAplvRejectRequest request);
	void useorgHfnDelReject(UseorgRequest.HfnAplvRejectRequest request);

	// 초기 선택한 관계사의 API 정보 불러오기
	List<ApiVO> selectApiListByUseorgYn(UseorgRequest.UseorgDetailRequest request);
	List<HfnInfoVO> selectHfnListByUseorgYn(UseorgRequest.UseorgDetailRequest request);

	void secedeUseorg(UseorgRequest.UseorgSecedeRequest request);
	void secedeUseorgHis(UseorgRequest.UseorgSecedeRequest request);
	UseorgVO selectSecedeUseorg(UseorgRequest.UseorgSecedeRequest request);
	void secedeUseorgInfo(UseorgRequest.UseorgSecedeRequest request);
	void secedeUseorgLogin(UseorgRequest.UseorgSecedeRequest request);
	UserWithdrawVO selectUserWithDraw(UserRequest request);
	void secedeUseorgWithDraw(UseorgRequest.UseorgSecedeRequest request);

	/** AES키 발급 관련**/
	void insertDateRowIfNotExists();
	void updateDateSeqForNoDup();
	int getDateSeq();

	public void updateUseorgUpload(UseorgRequest.UseorgUploadRequest request);


	UserVO getUserAuth(UserLoginRequest.UserLoginLockCheckRequest request);
	UserVO getAdminAuth(UserLoginRequest.HfnLoginLockCheckRequest request);

	void setUserPwHis(UserRequest.UserPwdUpdateRequest request);
	void setUseorgPwHis(UserRequest.OrgPwdUpdateRequest request);

	/*
	 *******************************사용자******************************
	 */
	UserVO selectUser(UserRequest.UserDetailRequest userDetailRequest);
	UseorgVO selectUseorgData(UserRequest.UserDetailRequest useorgDetailRequest);
	int countUserList(UserRequest userRequest);
	List<UserVO> selectUserList(UserRequest userRequest);
	void insertUser(UserRequest.UserRegistRequest userRegistRequest);
	UserVO selectUserForId(UserRequest.UserRegistRequest userRegistRequest);
	UserVO selectUserForUserId(UserRequest.UserRegistRequest userRegistRequest);
	void insertUserRole(UserRequest.UserRegistRequest userRegistRequest);
	UserVO selectUserPwd(UserRequest.UserDetailRequest userDetailRequest);
	UserVO selectHfnUserPwd(UserRequest.UserDetailRequest userDetailRequest);
	void updateUser(UserRequest.UserUpdateRequest userUpdateRequest);
	int userIdDupCheck(UserRequest.UserDupCheckRequest userDupCheckRequest);
	int userIdDupCheckInUserTable(UserRequest.UserDupCheckRequest userDupCheckRequest);
	int userEmailDupCheck(UserRequest.UserDupCheckRequest userDupCheckRequest);
	int useorgEmailDupCheck(UserRequest.UserDupCheckRequest userDupCheckRequest);
	int userEmailDupCheckUpdate(UserRequest.UserDupCheckRequest userDupCheckRequest);
	int useorgIdDupCheck(UseorgRequest.UseorgIdDupCheckRequest useorgIdDupCheckRequest);
	int tmpHbkIdDupCheck(UserRequest.UserDupCheckRequest userDupCheckRequest);
	List<UserVO> selectSignUserList();
	void updateUserStatCdChange(UserRequest.UserStatCdChangeRequest userRequest);
	void userPwdUpdate(UserRequest.UserPwdUpdateRequest userPwdUpdateRequest);
	void userPwdAndTosUpdate(UserRequest.UserPwdAndTosUpdateRequest userPwdAndTosUpdateRequest);
	void userTepPwdUpdate(UserRequest.UserTmpPwdUpdateRequest userTmpPwdUpdateRequest);
	void updateUserLoginType(UserLoginRequest.UpdateTypeRequest request);

	void secedeUser(UserRequest.UserSecedeRequest request);
	void secedeUserLogin(UserRequest.UserSecedeRequest request);
	void secedeUserWithDraw(UserRequest.UserSecedeRequest request);
	UserRequest.UserSecedeRequest selectUserWithDrawReason(String userKey);
	void insertUserWithdrawa(UserRequest.UserSecedeRequest request);
	/*
	 *******************************승인******************************
	 */
	String selectAppKeyByAplvSeqNo(String aplvSeqNo);
	AplvVO selectAplv(AplvRequest.AplvDetailRequest aplvDetailRequest);
//	AplvVO selectAplvTestselectAplvTest(AplvRequest.AplvDetailRequest aplvDetailRequest);
	String selectAplvBtnYn(AplvRequest.AplvDetailRequest aplvDetailRequest);
	AplvVO selectAplvDetail(String aplvSeqNo);

	List<AplvHisVO> selectAplvHis(AplvRequest.AplvDetailRequest aplvDetailRequest);
	AppsVO selectAppDetailInfo(AppsRequest appsRequest);
	AppsVO selectAppHisDetail(AppsRequest appsRequest);
	AppsVO selectAppDetailFromModTable(AppsRequest appsRequest);
	AppsVO aplvReqAppDetailInfo(AppsRequest appsRequest);
	List<AppsVO> selectAppSrcHisList(AppsRequest appsRequest);
	int selectAppApiCnt(AppsRequest appsRequest);
	List<ApiVO> selectAppApiList(ApiRequest.ApiDetailRequest apiDetailRequest);
	List<ApiVO> selectAppApiDetailList(ApiRequest.ApiDetailRequest apiDetailRequest);
	List<ApiVO> selectAppApiRejectList(ApiRequest.ApiDetailRequest apiDetailRequest);
	List<ApiVO> getIpList(ApiRequest.ApiDetailRequest apiDetailRequest);
	List<RequestApiVO> getRequestList(ApiRequest.ApiDetailRequest apiDetailRequest);
	List<AppCnlInfoVO> selectAppChannlList(ApiRequest.ApiDetailRequest apiDetailRequest);

	SndCertMgntRequest getUserCertMgnt(UserRequest.UserPwdUpdateRequest request);

	int countAplvList(AplvRequest aplvRequest);
	List<AplvVO> selectAplvList(AplvRequest aplvRequest);
	void insertAplv(AplvRequest.AplvRegistRequest aplvRegistRequest);
	void insertAplv2(AplvRequest.AplvRegistRequest aplvRegistRequest);

	AplvVO selectAplvForAplvReqCd(AplvRequest.AplvRegistRequest aplvRegistRequest);
	AplvVO selectAplvForAplvReqCdForHfnAplv(AplvRequest.AplvRegistRequest aplvRegistRequest);
	void insertAplvHis(AplvRequest.AplvHisRegistRequest aplvRequest);
	void insertAplvHis2(AplvRequest.AplvHisRegistRequest aplvRequest);

	AplvHisVO selectAplvHisDetailInfo(AplvRequest.AplvHisDetailRequest aplvHisDetailRequest);

	String selectHfnCdByUserKey(String userKey);

	void updateAplvHis(AplvRequest.AplvApprovalRequest aplvApprovalRequest);
	void updateAplvInfo(AplvRequest.AplvApprovalRequest aplvApprovalRequest);
	void updateAplvStatCdChange(AplvRequest.AplvApprovalRequest aplvApprovalRequest);
	int countAplvRest(AplvRequest.AplvApprovalRequest aplvApprovalRequest);

	void updateUserAplv(UserRequest.UserIdUpdateRequest userIdUpdateRequest);
	void updateAppsStatCdChange(AppsModRequest appsModRequest);
//	void insertAppsScrHis(AppsModRequest appsModRequest);
	void insertAppsScrHis(AppsRegRequest appsRegRequest);
	void updateAppsSvcEnDt(AppsRequest appsRequest);
	void updateAppsScrHis(AppsModRequest appsModRequest);

	void updateUserReject(UserRequest.UserIdUpdateRequest userIdUpdateRequest);
	void updateUseorgStatCdReject(UseorgRequest.UseorgStatCdChangeRequest useorgStatCdChangeRequest);
	void updateAppsStatCdReject(AppsModRequest appsModRequest);
	void updateAppsStatCdRejectAndTermEtdYn(AppsModRequest appsModRequest);

	/*
	 *************************관계사 사용자******************************
	 */
	int hfnIdDupCheck(HfnUserRequest.HfnUserDupCheckRequest hfnUserDupCheckRequest);
	void insertHfnUser(HfnUserRequest.HfnUserRegistRequest hfnUserRegistRequest);
	HfnUserVO selectHfnUserForId(HfnUserRequest.HfnUserRegistRequest hfnUserRegistRequest);
	void insertHfnUserRole(HfnUserRequest.HfnUserRegistRequest hfnUserRegistRequest);
	void updateHfnUserRole(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest);
	void insertUserPwHis(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest);
	void insertHfnLine(HfnUserRequest.HfnUserRegistRequest hfnUserRegistRequest);
	HfnLineVO selectHfnLineById(HfnUserRequest.HfnLineRequest hfnLineRequest);
	int countHfnUserList(HfnUserRequest hfnUserRequest);
	List<HfnUserVO> selectHfnUserList(HfnUserRequest hfnUserRequest);
	void updateHfnUserStatCdChange(HfnUserRequest.HfnUserStatCdChangeRequest hfnUserStatCdChangeRequest);
	void hfnUserPwdUpdate(HfnUserRequest.HfnUserPwdUpdateRequest hfnUserPwdUpdateRequest);
	void hfnUserPwdHisUpdate(HfnUserRequest.HfnUserPwdUpdateRequest hfnUserPwdUpdateRequest);
	void hfnUserPwdAndTosUpdate(HfnUserRequest.HfnUserPwdAndTosUpdateRequest hfnUserPwdAndTosUpdateRequest);

	/*
	 *******************************관계사******************************
	 */
	List<HfnInfoVO> selectHfnCdList();
	List<HfnUserVO> selectMyHfnMember(HfnInfoRequest hfnInfoRequest);

	HfnUserVO selectHfnUser(HfnUserRequest.HfnUserDetailRequest hfnUserDetailRequest);
	HfnUserVO selectHfnLine(HfnUserRequest.HfnUserDetailRequest hfnUserDetailRequest);
	HfnLineVO selectHfnAltUser(HfnUserRequest.HfnAltUserRequest hfnAltUserRequest);
	List<HfnLineVO> selectHfnAltUsers(HfnUserRequest.HfnAltUserRequest request);

	void updateHfnUser(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest);
	void updateHfnLine(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest);
	void insertHfnAltInfo(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest);
	void updateHfnAltInfo(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest);
	void updateHfnAltInfoByHfnEdit(HfnUserRequest.HfnUserUpdateRequest hfnUserUpdateRequest);

	List<HfnInfoVO> selectHfnByHfnCd(String hfnCd);

	// 선택된 APP과 관계된 관계사 리스트
	List<HfnInfoVO> selectedAppRelatedHfn(AplvRequest.AplvRegistRequest request);

	// 선택된 Useorg와 관계된 관계사 리스트
	List<HfnInfoVO> selectedUseorgRelatedHfn(AplvRequest.AplvRegistRequest request);

	// 선택된 Useorg와 관계된 관계사 리스트(탈퇴용)
	List<HfnInfoVO> selectedUseorgExpRelatedHfn(AplvRequest.AplvRegistRequest request);

	// 관계사 담당자 조회
	List<HfnUserVO> selectHfnManager(HfnInfoRequest request);

	// 접속한 관계사 직원의 승인권한 조회
	HfnUserVO userAplvLevelInfo(HfnUserRequest.HfnUserAplvLevelRequest request);

	// 관계사 결재자 리스트 조회
	List<HfnUserVO> selectHfnSignUserList();

	HfnUserVO selectHfnUserFromHfnLine(HfnUserVO hfnuser);

	List<HfnUserVO> getAplvLevelList(HfnUserRequest.HfnUserAplvLevelRequest request);

	// 선택된 APP과 관계된 이용기관의 ENC_KEY
	String selectUseorgEncKey(AppsRequest request);

	// 수정 -> 등록시 앱발급 용
	String selectEncKeyFromModByAppKey(AppsRequest request);

	// 이용기관 테이블의 INDEX 리턴
	int countUseorgTable();

	// 이용기관의 기관코드 리턴
	String selectUseorgEntrCd(AppsRequest request);
	AppsVO selectUseorgEntrCdFromMod(AppsRequest request);
	AppsVO selectUseorgEntrCdFromInfo(AppsRequest request);
	UseorgVO selectUseorgByEntrCd(String request);

	// 이용기관 포탈 - 개발자 등록, 삭제(기관코드, user_type, role 변경)
    void regDeveloperInfo(UserRequest.RegistDeveloperRequest request);
    void regDeveloperLogin(UserRequest.RegistDeveloperRequest request);
    void regDeveloperRole(UserRequest.RegistDeveloperRequest request);
    int cntDevExists(UserRequest.RegistDeveloperRequest request);
	int checkDevExistsOtherUseorg(UserRequest.RegistDeveloperRequest request);
	int cntDevDupCheck(UserRequest.RegistDeveloperRequest request);
    List<UserVO> selectDeveloperList(UserRequest.RegistDeveloperRequest request); // 개발자 리스트 로딩
    void deleteDeveloperInfo(UserRequest.RegistDeveloperRequest request);
    void deleteDeveloperLogin(UserRequest.RegistDeveloperRequest request);
    void deleteDeveloperRole(UserRequest.RegistDeveloperRequest request);
	List<UserPwHisVO> getUseorgPw(String userKey);
	List<UserPwHisVO> getUserIdPw(String userId);

	void updateHfnUserPwd(HfnUserRequest.HfnUserPwdUpdateRequest request);
	void setHfnUserPwdHis(HfnUserRequest.HfnUserPwdUpdateRequest request);
	List<UserPwHisVO> getHfnIdPw(String hfnId);

    /** 로그인 테이블관련 **/
	void updateUserLoginPwd(UserLoginRequest.UpdatePwdRequest request);
	void insertPwHisUser(UserLoginRequest.UpdatePwdRequest request);

	int hfnLoginFailCntCheck(UserLoginRequest.HfnLoginLockCheckRequest hfnLoginLockCheckRequest);		// 관리자 포탈 로그인 실패횟수 카운트 리턴
	void hfnLoginFailCntSet(UserLoginRequest.HfnLoginLockCheckRequest hfnLoginLockCheckRequest);		// 관리자 포탈 로그인 실패 시 카운트 세팅
	void hfnLoginLockChange(UserLoginRequest.HfnLoginLockCheckRequest hfnLoginLockCheckRequest);		// 관리자 포탈 로그인 잠금 여부 체인지
	String hfnLoginLockYn(UserLoginRequest.HfnLoginLockCheckRequest hfnLoginLockCheckRequest);			// 관리자 포탈 로그인 잠금 여부 리턴

	int userLoginFailCntCheck(UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest);	// 이용자 포탈 로그인 실패횟수 카운트 리턴
	void userLoginFailCntSet(UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest);		// 이용자 포탈 로그인 실패 시 카운트 세팅
	void userLoginLockChange(UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest);		// 이용자 포탈 로그인 잠금 여부 체인지
	String userLoginLockYn(UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest);		// 이용자 포탈 로그인 잠금 여부 리턴

	/** 개인회원 아이디 찾기 **/
	UserVO searchUserId(UserRequest.searchUserRequest request);

	/** 사업자회원 아이디 찾기 **/
	UseorgVO searchUserOrgId(UserRequest.searchUserorgRequest request);

	/** 개인회원 비밀번호 찾기 **/
	UserVO searchUserPassword(UserRequest.searchUserRequest request);

	/** 사업자회원 비밀번호 찾기 **/
	UseorgVO searchOrgPassword(UserRequest.searchUserorgRequest request);

	/** 개인회원 비밀번호 변경 **/
	void setUserPwd(UserRequest.UserPwdUpdateRequest userPwdUpdateRequest);

	/** 개인회원 비밀번호 변경 **/
	void setUserPwdLogin(UserRequest.UserPwdUpdateRequest userPwdUpdateRequest);

	/** 사업자회원 비밀번호 변경 **/
	void setOrgPwd(UserRequest.OrgPwdUpdateRequest userPwdUpdateRequest);

	/** 사업자회원 비밀번호 변경 **/
	void setOrgPwdLogin(UserRequest.OrgPwdUpdateRequest userPwdUpdateRequest);

	/** 비밀번호찾기 인증정보 입력 **/
	void insertSndCertMgntByPw(SndCertMgntRequest request);

	/** 회원가입 인증정보 입력 **/
	void insertEmailData(SndCertMgntRequest request);

	/** 이메일 발송 정보 입력 **/
	void insertMailSendData(SndCertMgntRequest request);

	/** 메지시허브 발송 정보 입력 **/
	void insertHubMsgData(SndCertMgntRequest request);

	/** 가입완료 **/
	void authCompleteCertMgnt(SndCertMgntRequest request);

	/** 가입완료 - 인증정보조회 **/
	UserVO selectCertMgnt(SndCertMgntRequest request);

	/** 가입완료_사업자 **/
	void authCompleteUseorgInfo(SndCertMgntRequest request);

	/** 가입완료_개인외국인 **/
	void authCompleteUserInfo(SndCertMgntRequest request);

	/** 인증완료 **/
	void updateCertMgnt(SndCertMgntRequest request);

    /** 인증정보조회**/
    UserVO selectCertMgntUser(UserRequest.searchUserRequest request);

    /** 인증정보조회**/
    UseorgVO selectCertMgntUseorg(UserRequest.searchUserRequest request);

	/** 로그인 - 인증정보조회 **/
	UserVO selectLoginCertMgnt(String userKey);

	//------------------------------------------------------------------------------------------------------
	// 개발자공간-API제휴신청
	//------------------------------------------------------------------------------------------------------
	void insertApiJehu(ApiJehuRequest apiJehuRequest);

	//------------------------------------------------------------------------------------------------------
	// 개발자공간-공지사항목록
	//------------------------------------------------------------------------------------------------------
	List<NoticeVO> selectNoticeList(NoticeRequest.NoticeListRequest noticeListRequest);
	NoticeVO getNoticeFirstOne();
	int countNoticeList(NoticeRequest.NoticeListRequest noticeListRequest);
	NoticeVO detailNotice(NoticeRequest noticeRequest);
	void increaseNoticeViewCnt(NoticeRequest noticeRequest);
	void insertNotice(NoticeRequest noticeRequest);
	void deleteNotice(NoticeRequest noticeRequest);
	void updateNotice(NoticeRequest noticeRequest);

	List<FaqVO> selectFaqList(FaqRequest faqRequest);
	int countFaqList(FaqRequest faqRequest);
	FaqVO detailFaq(FaqRequest faqRequest);
	void insertFaq(FaqRequest faqRequest);
	void updateFaq(FaqRequest faqRequest);
	void deleteFaq(FaqRequest faqRequest);

	List<QnaVO> selectQnaList(QnaRequest qnaRequest);
	int countQna(QnaRequest qnaRequest);
	QnaVO detailQna(QnaRequest qnaRequest);
	void deleteQna(QnaRequest qnaRequest);
	void updateAnswer(QnaRequest qnaRequest);

	String selectQnaAttachment01(QnaRequest qnaRequest);
	String selectQnaAttachment02(QnaRequest qnaRequest);

	/** api 할인율등록 **/
	void insertApiChargeDiscountRate(RequestApiVO request);
	/** api 할인율수정 **/
	void updateApi(RequestApiVO request);
	/** api 할인율 승인 **/
	void updateDiscount(RequestApiVO request);
	void updateDiscountByNForAppKeyAndApiId(RequestApiVO request);
	/** api 할인율 삭제 **/
	void updateDelDiscount(RequestApiVO request);
	/** api 할인율 삭제: chargeDiscountRateId로 **/
	void deleteChargeDiscountRate(RequestApiVO request);

	/** 로그인 잠금 해제 **/
	void hfnLoginLockRelease(UserLoginRequest.HfnLoginLockCheckRequest hfnLoginLockCheckRequest);
	void userLoginLockRelease(UserLoginRequest.UserLoginLockCheckRequest userLoginLockCheckRequest);


	void deleteDiscountForN(RequestApiVO requestApiVO);
	void deleteDiscountForWAIT(RequestApiVO requestApiVO);
	void updateDiscountForWAIT(RequestApiVO requestApiVO);
	/** 승인/반려 유저정보조회 **/
	UseorgVO getAppUseorg(String aplvReqCd);
	UseorgVO getUseorg(String aplvReqCd);

	void updateDiscountByYForN(RequestApiVO requestApiVO);

	String getHfnCdTxt(String hfnCd);

	// 개인 정보 해제 로그 등록
    void insertPrivacyReleaseLog(PrivacyReleaseLogRequest request);

    /** 메일 재발송 관련 **/
    String selectUserkeyByUserid(SndCertMgntRequest sndCertMgntRequest);
    List<CertMailVO> selectCertMailList(SndCertMgntRequest sndCertMgntRequest);
    int countCertMailList(SndCertMgntRequest sndCertMgntRequest);
    CertMailVO detailCertMail(SndCertMgntRequest sndCertMgntRequest);

    // 배치 ID 목록
    List<BatchLogVO> selectBatchIdList();

	int countBatchLogList(BatchLogListRequest request);

	List<BatchLogVO> selectBatchLogList(BatchLogListRequest request);

	/** 관리자 - 트랜잭션 관리 **/
	List<TrxVO> selectTrx(TrxRequest request);
	int cntTrxList(TrxRequest request);
	void regTrx(TrxRequest request);
	TrxVO detailTrx(TrxRequest request);
	void updateTrx(TrxRequest request);

	String selectEntrCdFromUserKey(String userKey);

	String getPwHisDate(String userId);
}
