package com.hanafn.openapi.portal.views.service;

import com.hanafn.openapi.portal.cmct.HubCommunicator;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.util.AES256Util;
import com.hanafn.openapi.portal.util.ExcelUtil;
import com.hanafn.openapi.portal.views.dto.AplvRequest;
import com.hanafn.openapi.portal.views.dto.HfnInfoRequest;
import com.hanafn.openapi.portal.views.dto.SettlementRequest;
import com.hanafn.openapi.portal.views.dto.UseorgRequest;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.repository.SettlementRepository;
import com.hanafn.openapi.portal.views.vo.AplvVO;
import com.hanafn.openapi.portal.views.vo.FeeCollectionInfoVO;
import com.hanafn.openapi.portal.views.vo.HfnUserVO;
import com.hanafn.openapi.portal.views.vo.UseorgVO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class SettlementService {
	private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);


	@Autowired
    ExcelUtil excelUtil;

	@Autowired
	SettlementRepository settlementRepository;

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    HubCommunicator hubCommunicator;

    // 승인분류코드 단건
    private final static String FEE_DIV_CD_SINGLE = "FEE_S";

    // 승인분류코드 다건
    private final static String FEE_DIV_CD_MULTIPLE = "FEE_M";

    // 요청 타이틀
    private final static String FEE_TITLE = "과금 수수료 결재신청";

    @Value("${spring.profiles.active}")
    private String thisServer;

	// 수수료 징수정보 목록 조회
	public List<FeeCollectionInfoVO> getFeeCollectionInfoList(SettlementRequest request) throws Exception {

		List<FeeCollectionInfoVO> feeList = settlementRepository.getFeeCollectionInfoList(request);

		for (FeeCollectionInfoVO fee : feeList) {
			fee.setAccNo(AES256Util.decrypt(fee.getAccNo()));
		}

		return feeList;
	}

	// 수수료징수정보 엑셀다운로드
	public void feeExcelDownload(SettlementRequest request, HttpServletResponse response) throws Exception {
		excelUtil.feeExcelDownload(request, response);
	}

	// 수수료징수정보 엑셀업로드
	public ResponseEntity<String> excelUpload(SettlementRequest settlementRequest, HttpServletRequest request, HttpServletResponse response, UserPrincipal currentUser) throws Exception {
		return excelUtil.settlementExcelUpload(settlementRequest,request , response, currentUser);
	}

    // 수수료 징수정보 다건 저장
    public void feeApprovalRequest(SettlementRequest request) {

	    // 수수료 승인생성 프로세스
        feeAplvProcess(request, FEE_DIV_CD_MULTIPLE);

        // 수수료징수정보 테이블 결제여부 UPDATE
        settlementRepository.feeApprovalRequest(request);

        // 수수료징수정보 history 저장
        setFeeHis(request);
    }

    // 수수료 승인생성 프로세스
    private void feeAplvProcess(SettlementRequest request, String divCd) {

        // 승인테이블 데이터 생성
        AplvRequest.AplvRegistRequest aplvRegistRequest = insertAplv(request, divCd);

        // 승인히스토리테이블 데이터 생성
        List<String> userParamList = insertAplvHis(aplvRegistRequest);

        // 개발, 품질, 운영일 경우 하나 허브 메시지 발송
        if(thisServer.equals("development") || thisServer.equals("staging") || thisServer.equals("production")) {
            sendHanaHub(request, userParamList, aplvRegistRequest.getAplvSeqNo());
        }
    }

    // 승인 테이블 데이터 생성
    private AplvRequest.AplvRegistRequest insertAplv(SettlementRequest request, String divCd) {
        // 승인생성
        AplvRequest.AplvRegistRequest aplvRegistRequest = new AplvRequest.AplvRegistRequest();

        // 승인코드 청구월+관계사코드
        String aplvReqCd = "";

        if (divCd.equals(FEE_DIV_CD_MULTIPLE)) {
            aplvReqCd = request.getBilMonth() + "_" + request.getHfnCd();
        } else {
            aplvReqCd = request.getBilMonth() + "_" + request.getHfnCd() + "_" + request.getAppKey();
        }

        aplvRegistRequest.setAplvReqCd(aplvReqCd);
        aplvRegistRequest.setAplvDivCd(divCd);
        aplvRegistRequest.setAplvReqCtnt(FEE_TITLE);
        aplvRegistRequest.setRegUserId(request.getRegUserId());
        aplvRegistRequest.setHfnCd(request.getHfnCd());

        settingRepository.insertAplv(aplvRegistRequest);

        return aplvRegistRequest;
    }

    // 승인 히스토리 테이블 데이터 생성
    private List<String> insertAplvHis(AplvRequest.AplvRegistRequest request) {

        // 관계사 별 담당자에게 승인(결재선 등록) 지정
        HfnInfoRequest hfnInfo = new HfnInfoRequest();

        hfnInfo.setHfnCd(request.getHfnCd());

        List<HfnUserVO> userList = settingRepository.selectHfnManager(hfnInfo);

        List<String> userParamList = new ArrayList<>();

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
                userParamList.add(userParamGen(user.getHfnCd(), user.getHfnId(), user.getUserNm()));
            }
        }

        // 하나티아이 관리자 추가
        if(thisServer.equals("development") || thisServer.equals("staging")) {
            userParamList.add(userParamGen("07", "2144", "이혜성"));
            userParamList.add(userParamGen("07", "2744", "반성욱"));
            userParamList.add(userParamGen("07", "2676", "최진현"));
            userParamList.add(userParamGen("07", "2600", "염유진"));
        }

        return userParamList;
    }

    // 메시지허브 user parameter 생성
    private String userParamGen(String hfnCd, String hfnId, String userNm) {

        String pad = "";
        int len = 7 - hfnId.length();

        for(int i=0; i<len; i++){
            pad += "0";
        }

        return hfnCd + pad + hfnId + "_" + userNm ;
    }

    // 개발, 품질, 운영일 경우 하나 허브 메시지 발송
    private void sendHanaHub(SettlementRequest request, List<String> userParamList, String aplvSeqNo) {

        // 승인 정보 조회
        AplvVO aplvInfo = settingRepository.selectAplvDetail(aplvSeqNo);

	    String hfnNm = settlementRepository.getHfnNm(request.getHfnCd());

        String name = "청구월: " + request.getBilMonth() + "\n" + "관계사 명: " + hfnNm;
        String userKey = request.getUserKey();

        String ctntMsg = "승인요청: " + FEE_TITLE + "\n" +
                name + "\n" +
                "신청자: " + aplvInfo.getRegUser() + "\n" +
                "신청일: " + aplvInfo.getRegDttm().substring(0,19);

        log.debug("@@ hub send Msg = " + ctntMsg);

        hubCommunicator.HubMsgCommunicator(FEE_TITLE, ctntMsg, userParamList, userKey, "H1");
    }

    // 수수료 징수정보 단건저장
    public void setFeeCollectionInfo(SettlementRequest request) throws Exception {
        // 수수료징수정보 테이블 결제여부 UPDATE
        settlementRepository.setFeeCollectionInfo(request);
    }

    // 수수료 징수정보 목록 조회
    public List<FeeCollectionInfoVO> getFeeCollectionHisList(SettlementRequest request) throws Exception {

        List<FeeCollectionInfoVO> feeList = settlementRepository.getFeeCollectionHisList(request);

        for (FeeCollectionInfoVO fee : feeList) {
            fee.setAccNo(AES256Util.decrypt(fee.getAccNo()));
        }

        return feeList;
    }

    // 수수료 징수정보 단건저장
    public void setFeeCollectionHisInfo(SettlementRequest request) throws Exception {

	    // 수수료 승인생성 프로세스
        feeAplvProcess(request, FEE_DIV_CD_SINGLE);

        settlementRepository.setFeeCollectionHisInfo(request);

        // 수수료징수정보 history 저장
        setFeeHis(request);
    }

    // 출금요청, 출금결과 앱목록 조회
    public List<FeeCollectionInfoVO> getAppList(SettlementRequest request) throws Exception {
        return settlementRepository.getAppList(request);
    }

    // 출금요청, 출금결과 앱목록 조회
    public List<FeeCollectionInfoVO> getUseOrgList(SettlementRequest request) throws Exception {
        return settlementRepository.getUseOrgList(request);
    }

    // 출금요청 목록조회
    public List<FeeCollectionInfoVO> getWdRequestList(SettlementRequest request) throws Exception {
        return settlementRepository.getWdRequestList(request);
    }

    // 출금결과 목록조회
    public List<FeeCollectionInfoVO> getWdResultList(SettlementRequest request) throws Exception {
        return settlementRepository.getWdResultList(request);
    }

    // 수수료징수정보 history 저장
    public void setFeeHis(SettlementRequest request) {
	    settlementRepository.setFeeHis(request);
    }

    // 수수료 징수정보 목록 조회(이용자포털)
    public List<FeeCollectionInfoVO> getFeeCollectionHisListOffice(SettlementRequest request) throws Exception {


        FeeCollectionInfoVO info = settlementRepository.selectUseorgListHfn(request);

        String[] hfnCd = info.getHfnCd().split(",");

        /*List<FeeCollectionInfoVO> feeList;

        for (int i = 0; i < hfnCd.length; i++) {
            request.setHfnCd(hfnCd[i]);
            settlementRepository.getFeeCollectionHisListOffice(request);
            feeList.
        }*/

        request.setHfnCd(info.getHfnCd());

        List<FeeCollectionInfoVO> feeList = settlementRepository.getFeeCollectionHisListOffice(request);

        for (FeeCollectionInfoVO fee : feeList) {
            fee.setAccNo(AES256Util.decrypt(fee.getAccNo()));
        }

        return feeList;
    }
}
