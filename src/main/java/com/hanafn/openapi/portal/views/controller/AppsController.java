package com.hanafn.openapi.portal.views.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanafn.openapi.portal.cmct.RedisCommunicater;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.file.FileService;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.SignUpResponse;
import com.hanafn.openapi.portal.security.model.User;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.util.UUIDUtils;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.AppsRepository;
import com.hanafn.openapi.portal.views.repository.SettingRepository;
import com.hanafn.openapi.portal.views.service.AppsService;
import com.hanafn.openapi.portal.views.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class AppsController {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CommonUtil commonUtil;
    @Autowired
    AppsService appsService;
    @Autowired
    SettingRepository settingRepository;
    @Autowired
    AppsRepository appsRepository;
    @Autowired
    MessageSourceAccessor messageSource;

    @Autowired
    FileService fileService;

    /******************** 앱 조회 ********************/
    @PostMapping("/appList")
    public ResponseEntity<?> appList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        AppsRsponsePaging data = appsService.selectAppsListPaging(request);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/appUpdateCancel")
    public ResponseEntity<?> appUpdateCancel(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {

        request.setModUserId(currentUser.getUserKey());
        request.setModUser(currentUser.getUsername());
        appsService.updateCancelApp(request);

        return ResponseEntity.ok(new SignUpResponse(true, "APP_STAT_CD Cancel Update successfully"));
    }

    /*** 사용중 - 앱 상태 변경 ***/
    @PostMapping("/appUpdateStatCd")
    public ResponseEntity<?> appUpdateStatCd (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {

        request.setModUserId(currentUser.getUserKey());
        request.setModUser(currentUser.getUsername());
        AppsRsponse data = appsService.updateAppStatCd(request);

        return ResponseEntity.ok(data);
    }

    /******************** 앱 등록 ********************/
    @PostMapping("/appReg")
    public ResponseEntity<?> appReg (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRegRequest request) {

        request.setAppKey(UUIDUtils.generateUUID());
        request.setRegUser(currentUser.getUsername());
        request.setRegUserId(currentUser.getUserKey());

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        appsService.insertApp(request);

        return ResponseEntity.ok(new SignUpResponse(true, "App registered successfully"));
    }

    @PostMapping("/apiListByUseorgYn")
    public ResponseEntity<?> apiListByUseorgYn(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest.UseorgDetailRequest request) {

        ApiResponse data= new ApiResponse();
        List<ApiVO> apiListByUseorgYn = settingRepository.selectApiListByUseorgYn(request);
        data.setList(apiListByUseorgYn);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/hfnListByUseorgYn")
    public ResponseEntity<List<HfnInfoVO>> hfnListByUseorgYn(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest.UseorgDetailRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        List<HfnInfoVO> data = null;
        data = settingRepository.selectHfnListByUseorgYn(request);
        return ResponseEntity.ok(data);
    }

    /******************** 앱 상세조회 ********************/
    @PostMapping("/appDetail")
    public ResponseEntity<?> appDetail (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setSearchUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        AppsRsponse data = appsService.selectAppDetail(request);

        return ResponseEntity.ok(data);
    }

    /*** 앱 업데이트 ***/
    @PostMapping("/appUpdate")
    public ResponseEntity<?> appUpdate (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRegRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        AppsRequest appsRequest = new AppsRequest();
        appsRequest.setAppKey(request.getAppKey());

        AppsVO apps = appsRepository.selectAppDetail(appsRequest);

        if(apps == null) {
            log.error("업데이트 하려는 앱 정보 null");
            throw new BusinessException("E073",messageSource.getMessage("E073"));
        }

        request.setRegDttm(apps.getRegDttm());
        request.setModUser(currentUser.getUsername());
        request.setRegUser(apps.getRegUser());
        request.setRegUserId(apps.getRegUserKey());
        request.setModUserId(currentUser.getUserKey());
        appsService.updateAppInfo(request);
        return ResponseEntity.ok(new SignUpResponse(true, "OPENAPI_PORTAL_APP_INFO Update successfully"));
    }

    @PostMapping("/appExtend")
    public ResponseEntity<?> appExtend (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRegRequest request) {

        request.setModUser(currentUser.getUsername());
        request.setModUserId(currentUser.getUserKey());
        appsService.appExtend(request);

        return ResponseEntity.ok(new SignUpResponse(true, "APP SERVICE EXTEND successfully"));
    }

    @PostMapping("/appSecretReisu")
    public ResponseEntity<?> appSecretReisu (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRegRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(),"userPortal")){
            request.setUserKey(currentUser.getUseorgKey()); // 개발자관련
        }

        request.setModUser(currentUser.getUsername());
        request.setModUserId(currentUser.getUserKey());
        request.setRegUserId(currentUser.getUserKey());
        AppsRsponse data = appsService.appSecretReisu(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/userRole")
    public ResponseEntity<?> userRole (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UserRequest request) {

        request.setUserKey(currentUser.getUserKey());

        HfnUserRoleVO role = new HfnUserRoleVO();
        role = appsService.hfnUserRole(request);

        return ResponseEntity.ok(role);
    }

    @PostMapping("/useOrgList")
    public ResponseEntity<?> useOrgList (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody HfnUseorgListRequest request) {
        HfnUseorgListResponse resp = appsService.useorgList(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/userPortalAppListAll")
    public ResponseEntity<?> userPortalAppListAll (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        if(StringUtils.equals(currentUser.getUserType(),"ORGD")) {
            request.setSearchUserKey(currentUser.getUseorgKey());
        }   // 개발자관련

        List<AppsVO> list = appsService.selectUserPortalAppList(request);
        return ResponseEntity.ok(list);
    }


    @PostMapping("/appListAll")
    public ResponseEntity<?> appListAll (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        List<AppsVO> list = appsService.selectAppAll(request);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/appListSelected")
    public ResponseEntity<?> appListSelected (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {

        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        for(GrantedAuthority ga : authorities) {
            if(ga.getAuthority() != "ROLE_SYS_ADMIN") {
                request.setSearchHfnCd(currentUser.getHfnCd());
                break;
            }
        }

        AppsRsponsePaging resp = new AppsRsponsePaging();
        resp = appsService.appListSelected(request);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/hfnCompanyAll")
    public ResponseEntity<?> hfnCompanyAll (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UseorgRequest request) {
        HfnCompanyAllResponse resp = new HfnCompanyAllResponse();
        resp = appsService.hfnCompanyAll(request);

        return ResponseEntity.ok(resp);
    }

    /* 키 다운로드 */
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
    @PostMapping("/appCidSecretDownload")
    public ResponseEntity<?> appCidSecretIssue(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {

        AppsRsponse data = appsService.selectAppDetail(request);
        AppsRequest.AppsIssueRequest appsIssueRequest = objectMapper.convertValue(data, AppsRequest.AppsIssueRequest.class);
        appsIssueRequest.setPubKey(request.getPubKey());

        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String clientIp = commonUtil.getIp(httpServletRequest);

        AppCidScrDlResponse appCidScrDlResponse = new AppCidScrDlResponse();
        if(StringUtils.equals(data.getAppScrReisuYn(),"Y")) {
            appCidScrDlResponse = appsService.reissueKeydownload(appsIssueRequest);
        }
        else
            appCidScrDlResponse = appsService.keyDownload(appsIssueRequest);

        appCidScrDlResponse.setReqServerIp(clientIp);
        appCidScrDlResponse.setUserId(currentUser.getUserId());
        appCidScrDlResponse.setUserKey(currentUser.getUserKey());

        return ResponseEntity.ok(appCidScrDlResponse);
    }

    @PostMapping("/appDel")
    public ResponseEntity<?> appDel (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRegRequest request) {

        request.setModUser(currentUser.getUsername());
        request.setRegUserId(currentUser.getUserId());
        request.setRegUser(currentUser.getUsername());
        appsService.deleteApp(request);

        return ResponseEntity.ok(new SignUpResponse(true, "APP Delete successfully"));
    }

    /* 승인 대기중 앱 관련 (AppInfoMod) */
    @PostMapping("/appDetailMod")
    public ResponseEntity<?> appDetailMod (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        AppsRsponse data = appsService.selectAppDetailMod(request);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/appListModAll")
    public ResponseEntity<?> appModAll (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        List<AppsVO> list = appsService.selectAppModAll(request);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/appListModPaging")
    public ResponseEntity<?> appListMod (@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        AppsRsponsePaging data = appsService.selectAppsListPagingMod(request);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/accountStatus")
    public ResponseEntity<?> accountStatus(@CurrentUser UserPrincipal currentUser, HttpServletRequest request, @Valid @RequestBody AccountStatusRequest accountStatusRequest) {
        String searchAccountBankCd = accountStatusRequest.getSearchAccountBankCd();

        if (StringUtils.isEmpty(searchAccountBankCd)) {
            log.error("출금은행 계좌번호 empty");
            throw new BusinessException("E074",messageSource.getMessage("E074"));
        }

        return appsService.accountStatus(currentUser, request, searchAccountBankCd);
    }

    @PostMapping("/accountStatusForHnw")
    public ResponseEntity<?> accountStatusHnw(@CurrentUser UserPrincipal currentUser, HttpServletRequest request, @Valid @RequestBody AccountStatusRequest accountStatusRequest) {
        String searchAccountBankCd = accountStatusRequest.getSearchAccountBankCd();

        if (StringUtils.isEmpty(searchAccountBankCd)) {
            log.error("출금은행 계좌번호 empty");
            throw new BusinessException("E074",messageSource.getMessage("E074"));
        }

        return appsService.accountStatusForHnw(currentUser, request, searchAccountBankCd);
    }

    @PostMapping("/fetchAppListInChargeDiscountRate")
    public  ResponseEntity<?> fetchAppListInChargeDiscountRate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        List<AppsVO> appsVOS = appsService.fetchAppListInChargeDiscountRate(request);

        return ResponseEntity.ok(appsVOS);
    }

    @PostMapping("/fetchApiListInChargeDiscountRate")
    public  ResponseEntity<?> fetchApiListInChargeDiscountRate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        List<ApiVO> apiVOS = appsService.fetchApiListInChargeDiscountRate(request);

        return ResponseEntity.ok(apiVOS);
    }

    @PostMapping("/appGuideDownload")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
    public ResponseEntity<?> appGuideDownload(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request, HttpServletResponse response){
        String appGuideFileName = "공개키생성_모듈+가이드문서";

        String fileName = "/openapi/support/aa.zip";
        Resource resource = null;
        String realFileName = "";

        try {
            resource = fileService.loadFileAsResource(fileName);
            realFileName = URLEncoder.encode(resource.getFilename(), "utf-8");
        } catch (Exception e) {
            log.error(messageSource.getMessage("E095"));
            throw new BusinessException("E095",messageSource.getMessage("E095"));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + realFileName + "\"")
                .body(resource);

//        try {
//            response.getOutputStream().write(FileUtil.readAsByteArray(new File("C:\\aa.zip")));
//        } catch ( IOException e) {
//            System.out.println("★가이드문서 다운로드중 에러");
//        }
    }

    /** 앱 대응답처리 설정 : 대응답 사용여부 확인 **/
    @PostMapping("/appDefresUseYn")
    public ResponseEntity<?> appDefresUseYn(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest.AppDefresRequest request) {
        String appKey = request.getAppKey();
        return ResponseEntity.ok(appsRepository.appUseDefresYn(appKey));
    }

    /** 앱 대응답처리 설정 : 대응답 사용여부 변경 (사용/해제) **/
    @PostMapping("/appDefresChange")
    public ResponseEntity<?> appDefresChange(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest.AppDefresRequest request) {
        appsRepository.appDefresChange(request);
        if(StringUtils.equals(request.getUseDefresYn(), "Y")) { // redis에 true(사용) 설정
            RedisCommunicater.setAppEchoResponse(request.getAppKey(), "true");
        } else { // 대응답 사용 중지
//            RedisCommunicater.setAppEchoResponse(request.getAppKey(), "false");   // redis에 false 설정
            RedisCommunicater.delAppEchoResponse(request.getAppKey());  // redis에서 삭제
        }
        return ResponseEntity.ok(new SignUpResponse(true, "Defined Response Setting Changes successfully"));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** 항목 암호화 키 존재유무 확인 **/
    @PostMapping("/checkCryptoKey")
    public ResponseEntity<?> checkCryptoKey(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        return ResponseEntity.ok(appsRepository.checkCryptoKey(request.getAppKey()));
    }

    /** 항목암호화 모듈 다운로드 (이용자포탈) **/
    @PostMapping("/cryptoUtilDownload")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
//    public ResponseEntity<?> cryptoUtilDownload(@CurrentUser UserPrincipal currentUser, HttpServletRequest request, HttpServletResponse response, @RequestParam String appKey) {
    public ResponseEntity<?> cryptoUtilDownload(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request, HttpServletResponse response) {
        String fileName = appsRepository.getCryptoKeyPath(request.getAppKey());

        if( fileName.isEmpty() || fileName == null || fileName == "" || fileName.equals("") ) {
            throw new BusinessException("E095", messageSource.getMessage("E095"));
        }

        Resource resource = null;
        try {
            resource = fileService.loadFileAsResource(fileName);
        } catch (Exception e) {
            log.error("암호화 키 로드 에러 : " + e.toString());
            throw new BusinessException("E095", messageSource.getMessage("E095"));
        }

        if( resource == null || !resource.exists() ) {
            throw new BusinessException("E095", messageSource.getMessage("E095"));
        }

        String contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .header("X-Suggested-Filename", resource.getFilename() + "\"")
                .body(resource);
    }

    /** 앱 별 항목암호화 : 키 리스트 가져오기 **/
    @PostMapping("/selectCryptoKeyList")
    public ResponseEntity<?> selectCryptoKeyList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest.AppCryptoKeyRequest request) {

        if(commonUtil.superAdminCheck(currentUser)) {
            try {
                AppsRsponse.AppCryptoKeyResponse response = appsService.selectCryptoKeyList(request);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                log.info("키 리스트 가져오기 에러 : " + e.toString());
                return ResponseEntity.ok(new SignUpResponse(false, "앱 별 암호화 키 리스트 조회 에러"));
            }
        } else {
            return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
        }
    }

    /** 앱 별 항목암호화 키 관리 : 관련 앱 리스트 가져오기 **/
    @PostMapping("/selectCryptoKeyAppList")
    public ResponseEntity<?> selectCryptoKeyAppList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest.AppCryptoKeyRequest request) {
        if(commonUtil.superAdminCheck(currentUser)) {
            try {
                AppsRsponse.AppCryptoKeyResponse response = appsService.selectCryptoKeyAppList(request);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                log.info("관련 앱 리스트 불러오기 에러 : " + e.toString());
                return ResponseEntity.ok(new SignUpResponse(false, "관련 앱 리스트 조회 에러"));
            }
        } else {
            return ResponseEntity.ok(new SignUpResponse(false, "권한이 없습니다."));
        }
    }

    /** 앱 별 항목암호화 키 관리 : 키 파일 저장하기 **/
    @PostMapping("/saveCryptoKeyFile")
    public ResponseEntity<?> saveCryptoKeyFile
    (
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "appNm", required = false) String appNm,
            @RequestParam(value = "appKey", required = false) String appKey,
            @RequestParam(value = "fileNm", required = false) String fileNm,
            @RequestParam(value = "fileData", required = false) MultipartFile file
    ) {
        if(commonUtil.superAdminCheck(currentUser)) {
            if( !file.isEmpty() && file != null ) {
                if( appsRepository.checkCryptoKey(appKey) > 0 ) {
                    throw new BusinessException("CR03", messageSource.getMessage("CR03"));
                }

                String data = "";
                try {
                    data = fileService.cryptoFileSave(file);
                } catch (Exception e) {
                    log.info("키 파일 저장하기 에러 : " + e.toString());
                    throw new BusinessException("CR01", messageSource.getMessage("CR01"));
                }

                if(data != null && data != "" && !data.equals("")) {

                    AppsRequest.CryptoKeyFileRequest request = new AppsRequest.CryptoKeyFileRequest();
                    request.setAppKey(appKey);
                    request.setKeyPath(data);
                    request.setRegUser(currentUser.getUserKey());
                    appsRepository.saveCryptoKey(request);

                    return ResponseEntity.ok(new SignUpResponse(true, "키 파일 저장 완료"));

                } else {
                    throw new BusinessException("CR01", messageSource.getMessage("CR01"));
                }
            } else {
                throw new BusinessException("CR02", messageSource.getMessage("CR02"));
            }
        } else {
            throw new BusinessException("C003", messageSource.getMessage("C003"));
        }
    }

    /** 앱 별 암호화키 관리 : 암호화키 상세정보 가져오기 **/
    @PostMapping("/detailAppCryptoKey")
    public ResponseEntity<?> detailAppCryptoKey(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest request) {
        if(commonUtil.superAdminCheck(currentUser)) {
            try {
                AppCryptoKeyVO info = appsRepository.detailAppCryptoKey(request.getAppKey());
                return ResponseEntity.ok(info);
            } catch (Exception e) {
                log.info("암호화키 상세정보 가져오기 에러 : " + e.toString());
                throw new BusinessException("CR04", messageSource.getMessage("CR04"));
            }
        } else {
            throw new BusinessException("C003", messageSource.getMessage("C003"));
        }
    }

    /** 앱 별 항목암호화 키 관리 : 키 파일 저장하기 **/
    @PostMapping("/updateCryptoKeyFile")
    public ResponseEntity<?> updateCryptoKeyFile
    (
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "appNm", required = false) String appNm,
            @RequestParam(value = "appKey", required = false) String appKey,
            @RequestParam(value = "fileNm", required = false) String fileNm,
            @RequestParam(value = "fileData", required = false) MultipartFile file
    ) {
        if(commonUtil.superAdminCheck(currentUser)) {
            if( !file.isEmpty() && file != null ) {
                if( appsRepository.checkCryptoKey(appKey) < 1 ) {
                    throw new BusinessException("CR05", messageSource.getMessage("CR05"));
                }

                // 기존 저장된 파일 삭제하기
                String oldPath = appsRepository.detailAppCryptoKey(appKey).getKeyPath();
                try {
                    int retCode = fileService.fileDelete(oldPath);
                    switch (retCode) {
                        case 1:
                            // 정상 삭제
                            break;
                        case 0:
                            // 키파일 삭제 도중 에러 발생
                            throw new BusinessException("CR06", messageSource.getMessage("CR06"));
                        case -1:
                            // 키 파일이 존재하지 않음
                            throw new BusinessException("CR07", messageSource.getMessage("CR07"));
                    }

                    retCode = fileService.dirDelete(oldPath);
                    switch (retCode) {
                        case 1:
                            // 정상 삭제
                            break;
                        case 0:
                            // 키파일 디렉토리 삭제 도중 에러 발생
                            throw new BusinessException("CR06", messageSource.getMessage("CR09"));
                        case -1:
                            // 키 파일 디렉토리가 존재하지 않음
                            throw new BusinessException("CR07", messageSource.getMessage("CR10"));
                    }
                } catch (Exception e) {
                    log.info("키 파일 삭제하기 에러 : " + e.toString());
                    throw new BusinessException("CR06", messageSource.getMessage("CR09"));
                }

                // 새로운 키 파일 저장하기
                String data = "";
                try {
                    data = fileService.cryptoFileSave(file);
                } catch (Exception e) {
                    log.info("키 파일 저장하기 에러 : " + e.toString());
                    throw new BusinessException("CR01", messageSource.getMessage("CR01"));
                }

                if(data != null && data != "" && !data.equals("")) {

                    AppsRequest.CryptoKeyFileRequest request = new AppsRequest.CryptoKeyFileRequest();
                    request.setAppKey(appKey);
                    request.setKeyPath(data);
                    request.setModUser(currentUser.getUserKey());
                    appsRepository.updateCryptoKey(request);

                    return ResponseEntity.ok(new SignUpResponse(true, "키 파일 업데이트 완료"));

                } else {
                    throw new BusinessException("CR01", messageSource.getMessage("CR08"));
                }
            } else {
                throw new BusinessException("CR02", messageSource.getMessage("CR02"));
            }
        } else {
            throw new BusinessException("C003", messageSource.getMessage("C003"));
        }
    }

    /** 앱 별 항목암호화 키 관리 : 키 파일 및 데이터 삭제 **/
    @PostMapping("/deleteCryptoKey")
    public ResponseEntity<?> deleteCryptoKey(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody AppsRequest.CryptoKeyFileRequest request) {

        if(commonUtil.superAdminCheck(currentUser)) {

            if( appsRepository.checkCryptoKey(request.getAppKey()) < 1 ) {
                throw new BusinessException("CR05", messageSource.getMessage("CR05"));
            }

            // 기존 저장된 파일 삭제하기
            String oldPath = appsRepository.detailAppCryptoKey(request.getAppKey()).getKeyPath();
            try {
                int retCode = fileService.fileDelete(oldPath);
                switch (retCode) {
                    case 1:
                        // 정상 삭제
                        break;
                    case 0:
                        // 키파일 삭제 도중 에러 발생
                        throw new BusinessException("CR06", messageSource.getMessage("CR06"));
                    case -1:
                        // 키 파일이 존재하지 않음
                        throw new BusinessException("CR07", messageSource.getMessage("CR07"));
                }

                retCode = fileService.dirDelete(oldPath);
                switch (retCode) {
                    case 1:
                        // 정상 삭제
                        break;
                    case 0:
                        // 키파일 디렉토리 삭제 도중 에러 발생
                        throw new BusinessException("CR06", messageSource.getMessage("CR09"));
                    case -1:
                        // 키 파일 디렉토리가 존재하지 않음
                        throw new BusinessException("CR07", messageSource.getMessage("CR10"));
                }

                // DB에서 정보 삭제하기
                appsRepository.deleteCryptoKey(request);

            } catch (Exception e) {
                log.info("키 파일 삭제하기 에러 : " + e.toString());
                throw new BusinessException("CR06", messageSource.getMessage("CR09"));
            }
        } else {
            throw new BusinessException("C003", messageSource.getMessage("C003"));
        }

        return ResponseEntity.ok(new SignUpResponse(true, "키 파일 삭제 완료"));
    }
}