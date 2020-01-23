package com.hanafn.openapi.portal.views.repository;

import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.vo.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppsRepository {

	List<AppsVO> selectAppAll(AppsRequest appsRequest);
	List<AppsVO> selectAppModAll(AppsRequest appsRequest);

	int countAppsList(AppsRequest appsRequest);
	List<AppsVO> selectAppsList(AppsRequest appsRequest);
	List<AppsVO> selectAppListAll(AppsRequest appsRequest);
	List<AppsVO> appListSelectedNoLPaging(AppsRequest appsRequest);

	void updateCancelApp(AppsRequest appsRequest);
	int updateAppStatCd(AppsRequest appsRequest);

	void deleteApp(AppsRegRequest appsRegRequest);
	void insertApp(AppsRegRequest appsRegRequest);
	void insertAppInfo(AppsRegRequest appsRegRequest);
	void insertAppMod(AppsRegRequest appsRegRequest);
	void insertAppModForUpdate(AppsRegRequest appsRegRequest);

	void updateAppApiInfoByNForWait(AppsRegRequest appsRegRequest);
	void updateAppChannelInfoByNForWait(AppsRegRequest appsRegRequest);

	void deleteAppMod(AppsRegRequest appsRegRequest);
	void insertAppCnlKey(AppsCnlKeyRequest appsCnlKeyRequest);
	void insertAppApiInfo(AppsApiInfoRequest appsApiInfoRequest);
	void appStatChange(AppsRegRequest appsRegRequest);
	void updateAppApiInfo(AppsApiInfoRequest appsRequest);
	void updateAppCnlInfo(AppsCnlKeyRequest appsRequest);

	/*** 앱 삭제 ***/
	void appDel(AppsRegRequest appsRegRequest);

	/*** 앱수정 상세조회 ***/
	AppsVO selectAppModifyDetail(AppsRequest appsRequest);
	AppsVO selectAppDetail(AppsRequest appsRequest);
	AppsVO selectAppManageDetail(AppsRequest appsRequest);
	AppsVO selectAppModDetail(AppsRequest appsRequest);
	List<AppApiInfoVO> selectAppApiInfo(AppsRequest appsRequest);
	List<AppCnlInfoVO> selectAppCnlInfo(AppsRequest appsRequest);
	List<AppApiInfoVO> selectAppApiInfoDetail(AppsRequest appsRequest);
	List<AppCnlInfoVO> selectAppCnlInfoDetail(AppsRequest appsRequest);
	List<AppApiInfoVO> selectAppApiInfoWait(AppsRequest appsRequest);
	List<AppCnlInfoVO> selectAppCnlInfoWait(AppsRequest appsRequest);
	ApiVO selectApi(ApiRequest apiRequest);

	/*** 앱수정 ***/
	void updateAppInfo(AppsRegRequest appsRegRequest);
	void updateAppInfoMod(AppsRegRequest appsRegRequest);
	void backupAppInfo(AppsRegRequest appsRegRequest);
    void backupAppInfoReject(AppsRegRequest appsRegRequest);
	void delAppCnlInfo(AppsCnlKeyRequest appsCnlKeyRequest);
	void delAppApiInfo(AppsApiInfoRequest appsApiInfoRequest);
	void updateAppSvcTermAndOfficialDocNo(AppsRegRequest appsRegRequest);
	void updateAppModSvcTermAndOfficialDocNo(AppsRegRequest appsRegRequest);

	/*** 승인대기 앱 정보 조회 관련 ***/
//	AppsVO selectAppModDetail(AppsRequest appsRequest); 위에 이미 있음.
	List<AppApiInfoVO> selectAppApiInfoMod(AppsRequest appsRequest);
	List<AppCnlInfoVO> selectAppCnlInfoMod(AppsRequest appsRequest);
	int countAppsListMod(AppsRequest appsRequest);
	List<AppsVO> selectAppsListMod(AppsRequest appsRequest);

	/*** 서비스기간연장 ***/
	void updateAppTerm(AppsRegRequest appsRegRequest);

	/*** 클라이언트ID, 시크릿키 발급,재발급 ***/
	void updateAppScr(AppsRegRequest appsRegRequest);
	void updateAppScrMod(AppsRegRequest appsRegRequest);
	void updateAppScrHis(AppsScrRequest appsScrRequest);
	void insertAppNewScrHis(AppsScrRequest appsScrRequest);
	void updateAppDldttm(String value);

	/*** 배치 ***/
	void batchAppStat();
	void batchAppInfo();
	void batchAppScrHis();

	AppsVO keyReturn(AppsScrRequest appsScrRequest);
	HfnUserRoleVO hfnUserRole(UserRequest request);
	List<UseorgVO> useorgList(HfnUseorgListRequest request);

	List<AppsVO> appListSelected(AppsRequest appsRequest);
	int countAppListSelected(AppsRequest appsRequest);

	List<HfnUserRoleVO> hfnCompanyAll(UseorgRequest request);

	List<AppsVO> selectAppListInChargeDiscountRate(AppsRequest request);

	List<ApiVO> selectApiListInChargeDiscountRate(AppsRequest request);

	/** 대응답 처리 **/
	String appUseDefresYn(String appKey);   // 앱 대응답사용여부 체크
	void appDefresChange(AppsRequest.AppDefresRequest request); // 앱 대응답사용여부 변경


	/** 항목암호화 툴 관리 **/
	String getCryptoKeyPath(String appKey);	// 앱 키를 이용하여 암호화키 경로를 가져옴
	List<AppCryptoKeyVO> selectAppCryptoKey(AppsRequest.AppCryptoKeyRequest request);	// 앱 암호화키 리스트 불러오기
	int cntSelectAppCryptoKey(AppsRequest.AppCryptoKeyRequest request);	// 앱 암호화키 리스트 갯수
    int checkCryptoKey(String appKey);
    List<AppsVO> selectCryptoKeyAppList(AppsRequest.AppCryptoKeyRequest request);   // 암호화키 등록 시 앱 검색 리스트
	void saveCryptoKey(AppsRequest.CryptoKeyFileRequest request);	// 암호화키 저장
	AppCryptoKeyVO detailAppCryptoKey(String appKey);	// 앱 암호화키 상세 정보
	void updateCryptoKey(AppsRequest.CryptoKeyFileRequest request);	// 암호화키 업데이트
	void deleteCryptoKey(AppsRequest.CryptoKeyFileRequest request);	// 암호화키 삭제
}