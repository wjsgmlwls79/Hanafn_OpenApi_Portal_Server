package com.hanafn.openapi.portal.views.controller;

import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.SignUpResponse;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.dto.SettlementRequest;
import com.hanafn.openapi.portal.views.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/settlement")
@Slf4j
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    @Autowired
    CommonUtil commonUtil;

    // 수수료 징수정보 목록 조회
    @PostMapping("/getFeeCollectionInfoList")
    public ResponseEntity<?> getFeeCollectionInfoList(@Valid @RequestBody SettlementRequest request) throws Exception {
        return ResponseEntity.ok(settlementService.getFeeCollectionInfoList(request));
    }


    // 수수료 징수정보 엑셀다운로드
    @PostMapping("/feeExcelDownload")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
    public ResponseEntity<?> feeExcelDownload(HttpServletResponse response,
                                           @RequestParam String bilMonth,
                                           @RequestParam String hfnCd
    ) throws Exception {

        SettlementRequest request = new SettlementRequest();

        request.setBilMonth(bilMonth);
        request.setHfnCd(hfnCd);

        settlementService.feeExcelDownload(request, response);
        return ResponseEntity.ok(new SignUpResponse(true, "API Deleted successfully"));
    }

    // 수수료 징수정보 엑셀업로드
    @PostMapping("/feeExcelUpload")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
    public ResponseEntity<?> excelUpload(@CurrentUser UserPrincipal currentUser, HttpServletRequest request, HttpServletResponse response,
                                         @RequestParam(value = "fileData", required = false) MultipartFile fileData,
                                         @RequestParam(value = "hfnCd", required = false) String hfnCd
    ) throws Exception {

        SettlementRequest settlementRequest = new SettlementRequest();

        settlementRequest.setFileData(fileData);
        settlementRequest.setHfnCd(hfnCd);

        return ResponseEntity.ok(settlementService.excelUpload(settlementRequest, request ,response, currentUser));
    }

    // 수수료 징수정보 결재요청
    @PostMapping("/feeApprovalRequest")
    public ResponseEntity<?> feeApprovalRequest(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody SettlementRequest request) throws Exception {

        request.setRegUser(currentUser.getUsername());
        request.setRegUserId(currentUser.getUserKey());
        request.setUserKey(currentUser.getUserKey());

        settlementService.feeApprovalRequest(request);

        return ResponseEntity.ok(new SignUpResponse(true, "successfully"));
    }

    // 수수료 징수정보 -> 출금정보저장
    @PostMapping("/setFeeCollectionInfo")
    public ResponseEntity<?> setFeeCollectionInfo(@Valid @RequestBody SettlementRequest request) throws Exception {
        settlementService.setFeeCollectionInfo(request);
        return ResponseEntity.ok(new SignUpResponse(true, "successfully"));
    }

    // 수수료 징수내역 목록 조회
    @PostMapping("/getFeeCollectionHisList")
    public ResponseEntity<?> getFeeCollectionHisList(@Valid @RequestBody SettlementRequest request) throws Exception {
        return ResponseEntity.ok(settlementService.getFeeCollectionHisList(request));
    }

    // 수수료 징수내역 저장
    @PostMapping("/setFeeCollectionHisInfo")
    public ResponseEntity<?> setFeeCollectionHisInfo(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody SettlementRequest request) throws Exception {

        request.setRegUser(currentUser.getUsername());
        request.setRegUserId(currentUser.getUserKey());
        request.setUserKey(currentUser.getUserKey());

        settlementService.setFeeCollectionHisInfo(request);

        return ResponseEntity.ok(new SignUpResponse(true, "successfully"));
    }

    // 출금요청, 출금결과 앱목록 조회
    @PostMapping("/getAppList")
    public ResponseEntity<?> getAppList(@Valid @RequestBody SettlementRequest request) throws Exception {
        return ResponseEntity.ok(settlementService.getAppList(request));
    }

    // 출금요청, 출금결과 이용기관 목록 조회
    @PostMapping("/getUseOrgList")
    public ResponseEntity<?> getUseOrgList(@Valid @RequestBody SettlementRequest request) throws Exception {
        return ResponseEntity.ok(settlementService.getUseOrgList(request));
    }

    // 출금요청 목록조회
    @PostMapping("/getWdRequestList")
    public ResponseEntity<?> getWdRequestList(@Valid @RequestBody SettlementRequest request) throws Exception {
        return ResponseEntity.ok(settlementService.getWdRequestList(request));
    }

    // 출금요청 목록조회
    @PostMapping("/getWdResultList")
    public ResponseEntity<?> getWdResultList(@Valid @RequestBody SettlementRequest request) throws Exception {
        return ResponseEntity.ok(settlementService.getWdResultList(request));
    }

    // 수수료 징수내역 목록 조회(이용자포털)
    @PostMapping("/getFeeCollectionHisListOffice")
    public ResponseEntity<?> getFeeCollectionHisListOffice(@Valid @RequestBody SettlementRequest request) throws Exception {
        return ResponseEntity.ok(settlementService.getFeeCollectionHisListOffice(request));
    }
}
