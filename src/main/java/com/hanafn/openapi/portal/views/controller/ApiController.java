package com.hanafn.openapi.portal.views.controller;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.CurrentUser;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.security.dto.SignUpResponse;
import com.hanafn.openapi.portal.util.CommonUtil;
import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.repository.ApiRepository;
import com.hanafn.openapi.portal.views.service.ApiService;
import com.hanafn.openapi.portal.views.service.GuideService;
import com.hanafn.openapi.portal.views.service.SettingService;
import com.hanafn.openapi.portal.views.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ApiController {

    private final MessageSourceAccessor messageSource;
    private final ApiService apiService;
    private final SettingService settingService;
    private final GuideService guideService;
    private final ApiRepository apiRepository;
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    @Autowired
    CommonUtil commonUtil;

    @Value("${spring.profiles.active}")
    private String thisServer;

    /*
     * ******************************API CATEGORY******************************
    */
    @PostMapping("/apiCtgr")
    public ResponseEntity<?> apiCtgr(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiCtgrRequest.ApiCtgrDetilRequest request) {

        ApiCtgrVO data = apiService.selectApiCtgr(request);
    	
    	return ResponseEntity.ok(data);
    }
    
    @PostMapping("/apiCtgrs")
    public ResponseEntity<?> apiCtgrList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiCtgrRequest request) {

        ApiCtgrRsponsePaging data = apiService.selectApiCtgrListPaging(request);

    	return ResponseEntity.ok(data);
    }

    @PostMapping("/apiCtgrRegist")
    public ResponseEntity<?> apiCtgrRegist(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiCtgrRequest.ApiCtgrRegistRequest request) {
        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.insertApiCtgr(request);  // 공통 Exception Handling으로 처리 예정

        return ResponseEntity.ok(new SignUpResponse(true, "Api Ctgr Registered successfully"));
    }

    @PostMapping("/apiCtgrUpdate")
    public ResponseEntity<?> apiCtgrUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiCtgrRequest.ApiCtgrUpdateRequest request) {
        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.updateApiCtgr(request);  // 공통 Exception Handling으로 처리 예정

        return ResponseEntity.ok(new SignUpResponse(true, "Api Ctgr Update successfully"));
    }

    @PostMapping("/apiCtgrDelete")
    public ResponseEntity<?> apiCtgrDelete(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiCtgrRequest.ApiCtgrDeleteRequest request) {

        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.apiCtgrDelete(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Api Ctgr Delete successfully"));
    }

    @PostMapping("/apiCtgrsAll")
    public ResponseEntity<?> apiCtgrAllList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiCtgrRequest request) {

        ApiCtgrRsponse data = apiService.selectApiCtgrAllList(request);

        return ResponseEntity.ok(data);
    }

    /*
     * ******************************API SUB CATEGORY******************************
     */
    @PostMapping("/apiSubCtgr")
    public ResponseEntity<?> apiSubCtgr(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiSubCtgrRequest.ApiSubCtgrDetilRequest request) {

        ApiSubCtgrVO data = apiService.selectApiSubCtgr(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/apiSubCtgrsByCtgrCd")
    public ResponseEntity<?> apiSubCtgrsByCtgrCd(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiSubCtgrRequest request) {

        ApiSubCtgrRsponse data = apiService.selectApiSubCtgrs(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/apiSubCtgrs")
    public ResponseEntity<?> apiSubCtgrList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiSubCtgrRequest request) {

        ApiSubCtgrRsponsePaging data = apiService.selectApiSubCtgrListPaging(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/apiSubCtgrRegist")
    public ResponseEntity<?> apiSubCtgrRegist(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiSubCtgrRequest.ApiSubCtgrRegistRequest request) {
        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.insertApiSubCtgr(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Api Ctgr Registered successfully"));
    }

    @PostMapping("/apiSubCtgrUpdate")
    public ResponseEntity<?> apiSubCtgrUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiSubCtgrRequest.ApiSubCtgrUpdateRequest request) {
        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.updateApiSubCtgr(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Api Ctgr Update successfully"));
    }

    @PostMapping("/apiSubCtgrDelete")
    public ResponseEntity<?> apiSubCtgrDelete(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiSubCtgrRequest.ApiSubCtgrDeleteRequest request) {

        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.apiSubCtgrDelete(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Api Ctgr Delete successfully"));
    }

    @PostMapping("/apiSubCtgrsAll")
    public ResponseEntity<?> apiSubCtgrAllList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiSubCtgrRequest request) {

        ApiSubCtgrRsponse data = apiService.selectApiSubCtgrAllList(request);

        return ResponseEntity.ok(data);
    }

    /*
     * ******************************API******************************
     * */

    @PostMapping("/api")
    public ResponseEntity<?> api(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest request) {
        UserRequest.UserDetailRequest userDetailRequest = new UserRequest.UserDetailRequest();
        userDetailRequest.setUserKey(currentUser.getUserKey());

        ApiDetailRsponse data = apiService.selectApi(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/apis")
    public ResponseEntity<?> apiList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(),"adminPortal")){
            Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
            for(GrantedAuthority ga : authorities) {
                if(ga.getAuthority() != "ROLE_SYS_ADMIN") {
                    if(StringUtils.isNotBlank(request.getSearchHfnCd()) && !StringUtils.equals(currentUser.getHfnCd(), request.getSearchHfnCd())) {
                        log.error("/apis 거래 hfnCd 조작가능성 탐지");
                        throw new BusinessException("C001",messageSource.getMessage("잘못된 조작입니다."));
                    }
                }
            }
        }

        if(StringUtils.isBlank(request.getSearchUserKey()) && StringUtils.equals(currentUser.getSiteCd(),"userPortal")) {
            request.setSearchUserKey(currentUser.getUseorgKey());
        }

        ApiRsponsePaging data = apiService.selectApiListPaging(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/apiListNoPaging")
    public ResponseEntity<?> apisListNoPaging(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest request) {

        if(StringUtils.equals(currentUser.getSiteCd(),"adminPortal")){
            Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
            for(GrantedAuthority ga : authorities) {
                if(ga.getAuthority() != "ROLE_SYS_ADMIN") {
                    if(StringUtils.isNotBlank(request.getSearchHfnCd()) && !StringUtils.equals(currentUser.getHfnCd(), request.getSearchHfnCd())) {
                        log.error("/apis 거래 hfnCd 조작가능성 탐지");
                        throw new BusinessException("C001",messageSource.getMessage("잘못된 조작입니다."));
                    }
                }
            }
        }

        if(StringUtils.isBlank(request.getSearchUserKey()) && StringUtils.equals(currentUser.getSiteCd(),"userPortal")) {
            request.setSearchUserKey(currentUser.getUseorgKey());
        }

        ApiRsponsePaging data = apiService.selectApiListNoPaging(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/apisCtgrs")
    public ResponseEntity<?> apisCtgrs(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiDevGuideRequest request) {
        ApiCtgrRsponse data = null;
        if(!StringUtils.isBlank(request.getSearchHfnCd())){ // hfnCd가 주어졌을 경우
            ApiRequest apiRequest = new ApiRequest();
            apiRequest.setSearchHfnCd(request.getSearchHfnCd());
            apiRequest.setSearchCtgrCd(request.getSearchCtgrCd());
            apiRequest.setSearchSubCtgrCd(request.getSearchSubCtgrCd());
            apiRequest.setSearchUserKey(request.getSearchUserKey());
            data.setList(apiService.selectCtgrApiList(apiRequest));
        }else{
            data = guideService.selectDevGuidesApiCtgrAll(request);
        }
        return ResponseEntity.ok(data);
    }

    @PostMapping("/apiRegist")
    public ResponseEntity<?> apiRegist(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRegistRequest request) {
        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());

        String apiSvc = StringUtils.replaceAll(StringUtils.trim(request.getApiSvc()), " ", "");
        String apiVer = StringUtils.replaceAll(StringUtils.trim(request.getApiVer()), " ", "");
        String apiUri = StringUtils.replaceAll(StringUtils.trim(request.getApiUri()), " ", "");
        String apiTosUrl= StringUtils.replaceAll(StringUtils.trim(request.getApiTosUrl()), " ", "");

        request.setApiSvc(apiSvc);
        request.setApiVer(apiVer);
        request.setApiUri(apiUri);
        request.setApiTosUrl(apiTosUrl);

        String gwType = request.getGwType();
        String hfnCd = request.getHfnCd();
        hfnCd = HfnEnum.resolve(hfnCd).getCode();
        if ("onefgw".equals(gwType)) hfnCd = "file";
        if (thisServer.equals("development")) hfnCd +="-dev";

        String url = "/" + gwType + "/" + hfnCd +"/api/" + request.getApiSvc() + "/" + request.getApiVer() + "/" + request.getApiUri();
        url = StringUtils.replace(url, "-", "_");

        request.setApiUrl(url);

        //API 중복 체크
        apiService.ApiDupCheck(request);
        apiService.insertApi(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Api registered successfully"));
    }

    @PostMapping("/apiUpdate")
    public ResponseEntity<?> apiUpdate(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRegistRequest request) {
        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.updateApi(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Api Update successfully"));
    }

    @PostMapping("/apiStatCdChange")
    public ResponseEntity<?> apiStatCdChange(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiStatModRequest request) {

        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.apiStatCdChange(request);

        return ResponseEntity.ok(new SignUpResponse(true, "Api_Stat_Cd Change successfully"));
    }

    @PostMapping("/apisAll")
    public ResponseEntity<?> apisAll(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest.ApiAllListRequest request) {
        ApiResponse data = apiService.selectApiAllList(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/searchCtgrApiAll")
    public ResponseEntity<?> searchCtgrApiAll(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest.CtgrApiAllListRequest request) {
        ApiResponse data = apiService.searchCtgrApiAll(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/apiDelete")
    public ResponseEntity<?> apiDelete(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiStatModRequest request) {
        request.setRegUserId(currentUser.getUserKey());
        request.setRegUserName(currentUser.getUsername());
        apiService.apiDelete(request);

        return ResponseEntity.ok(new SignUpResponse(true, "API Deleted successfully"));
    }

    // 스웨거 관련
    @PostMapping("/swaggerList")
    public ResponseEntity<?> swaggerList(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest.SwaggerRequest request) {

        ApiResponse data = apiService.swaggerList(request);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/swaggerInfo")
    public ResponseEntity<?> swagger(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiRequest.SwaggerInfoRequest request) {
        ApiRegistRequest apiRegistRequest = new ApiRegistRequest();
        apiRegistRequest.setApiSvc(request.getApiSvc());
        apiRegistRequest.setApiVer(request.getApiVer());
        apiRegistRequest.setApiUri(request.getApiUri());

        //API 중복 체크
        apiService.ApiDupCheck(apiRegistRequest);

        ApiDetailRsponse data = apiService.swaggerDetailInfo(request);

        return ResponseEntity.ok(data);
    }

    // API과금집계목록조회
    @PostMapping("/getApiChargeList")
    public ResponseEntity<?> getApiChargeList(@Valid @RequestBody ApiRequest request) {
        return ResponseEntity.ok(apiService.getApiChargeList(request));
    }

    // API과금집계할인율 다건조회
    @PostMapping("/getApiDetailList")
    public ResponseEntity<?> getApiDetailList(@Valid @RequestBody ApiRequest request) {
        return ResponseEntity.ok(apiService.getApiDetailList(request));
    }

    // API과금집계엑셀다운로드
    @PostMapping("/excelDownload")
    @CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
    public ResponseEntity<?> excelDownload(HttpServletResponse response,
                                           @RequestParam String stDt,
                                           @RequestParam String enDt,
                                           @RequestParam String searchUserKey,
                                           @RequestParam String searchHfnCd
                                            ) throws Exception {
        ApiRequest request = new ApiRequest();

        request.setStDt(stDt);
        request.setEnDt(enDt);
        request.setSearchUserKey(searchUserKey);
        request.setSearchHfnCd(searchHfnCd);

        apiService.excelDownload(request, response);
        return ResponseEntity.ok(new SignUpResponse(true, "API Deleted successfully"));
    }


    @PostMapping("/apiPolicy")
    public ApiPolicyVO apiPolicy(@Valid @RequestBody ApiPolicyRequest request) {
        return apiService.selectApiPolicy(request);
    }

    @PostMapping("/apiPolicyRegist")
    public ResponseEntity<?> apiPolicyRegist(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody ApiPolicyRequest request) {
        if (StringUtils.isBlank(request.getGubun())) {
            request.setRegUser(currentUser.getUserKey());
            apiService.insertApiPolicy(request);
        } else {
            request.setModUser(currentUser.getUserKey());
            apiService.updateApiPolicy(request);
        }
        return ResponseEntity.ok(true);
    }

    /** 대응답 정보용 : 대응답정보 리스트 불러오기 **/
    @PostMapping("/selectApiEcho")
    public ResponseEntity<?> selectApiEcho(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest request) {
        EchoResponse.EchoListResponse response = new EchoResponse.EchoListResponse();

        if(!commonUtil.superAdminCheck(currentUser)) {
            request.setSearchHfnCd(currentUser.getHfnCd());
        }

        try{
            response.setEchoList(apiRepository.selectApiEcho(request));
        }catch(Exception e) {
            log.error("selectApiEcho error :" + e.toString());
            throw new BusinessException("E182", messageSource.getMessage("E182"));
        }
        return ResponseEntity.ok(response);
    }

    /** 대응답 정보용 : api 리스트 불러오기 **/
    @PostMapping("/selectApisForEcho")
    public ResponseEntity<?> selectApisForEcho(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.ApisRequest request) {
        EchoResponse.ApisResponse response = new EchoResponse.ApisResponse();

        if(!commonUtil.superAdminCheck(currentUser)) {
            request.setSearchHfnCd(currentUser.getHfnCd());
        }

        try{
            List<ApiVO> apis = apiRepository.selectApisForEcho(request);
            response.setApis(apis);
        }catch(Exception e) {
            log.error("selectApisForEcho :" + e.toString());
            throw new BusinessException("E183", messageSource.getMessage("E183"));
        }
        return ResponseEntity.ok(response);
    }

    /** 대응답 정보용 : 해당 api의 응답정보 가져오기 **/
    @PostMapping("/getApiResponseSetting")
    public ResponseEntity<?> getApiResponseSetting(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.ApiDetailRequest request) {
        EchoResponse.ApiDetailResponse response = new EchoResponse.ApiDetailResponse();

        if(!commonUtil.superAdminCheck(currentUser)) {
            request.setSearchHfnCd(currentUser.getHfnCd());
        }

        try{
            ApiColumnRequest apiColumnRequest = new ApiColumnRequest();
            apiColumnRequest.setApiId(request.getApiId());
            apiColumnRequest.setClmReqDiv("RESPONSE");
            List<ApiColumnVO> apiColumnResponseList = apiRepository.selectApiColumnList(apiColumnRequest);
            apiService.settingApiColumn(apiColumnResponseList); //컬럼 타입이 LIST일 경우 하위 리스트 셋팅

            response.setColumns(apiColumnResponseList);

        }catch(Exception e) {
            log.error("getApiResponseSetting :" + e.toString());
            throw new BusinessException("E183", messageSource.getMessage("E183"));
        }
        return ResponseEntity.ok(response);
    }

    /** 대응답 정보용 : 해당 api의 요청정보(Parameter) 가져오기 **/
    @PostMapping("/getApiRequestSetting")
    public ResponseEntity<?> getApiRequestSetting(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.ApiDetailRequest request) {
        EchoResponse.ApiDetailResponse response = new EchoResponse.ApiDetailResponse();

        if(!commonUtil.superAdminCheck(currentUser)) {
            request.setSearchHfnCd(currentUser.getHfnCd());
        }

        try{
            ApiColumnRequest apiColumnRequest = new ApiColumnRequest();
            apiColumnRequest.setApiId(request.getApiId());
            apiColumnRequest.setClmReqDiv("REQUEST");
            List<ApiColumnVO> apiColumnRequestList = apiRepository.selectApiColumnList(apiColumnRequest);

            response.setColumns(apiColumnRequestList);

        }catch(Exception e) {
            log.error("getApiRequestSetting :" + e.toString());
            throw new BusinessException("E183", messageSource.getMessage("E183"));
        }
        return ResponseEntity.ok(response);
    }

    /** 대응답 정보용 : 대응답 정보 등록 **/
    @PostMapping("/regApiEcho")
    public ResponseEntity<?> regApiEcho(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.RegEchoRequest request) {

        int cnt = apiRepository.checkApiEchoDup(request);

        if (cnt > 0) {
            log.error(messageSource.getMessage("E184"));
            throw new BusinessException("E184", messageSource.getMessage("E184"));
        }

        try {
            ApiVO api = apiRepository.getApiInfo(request);

            api.setHfnCd(HfnEnum.resolve(api.getHfnCd()).getCode());
            if ("onefgw".equals(api.getGwType())) {
                api.setHfnCd("file");
            }

            String url = "/" + api.getGwType() + "/" + api.getHfnCd() +"/api/" + api.getApiSvc() + "/" + api.getApiVer() + "/" + api.getApiUri();
            request.setApiUrl(url);

            request.setRegUser(currentUser.getUserKey());
            apiRepository.regApiEcho(request);
        } catch (Exception e) {
            log.error("regApiEcho :" + e.toString());
            throw new BusinessException("E185", messageSource.getMessage("E185"));
        }

        return ResponseEntity.ok(new SignUpResponse(true, "대응답정보를 성공적으로 등록하였습니다."));
    }

    /** 대응답 정보용 : 대응답 정보 업데이트 **/
    @PostMapping("/updateApiEcho")
    public ResponseEntity<?> updateApiEcho(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.RegEchoRequest request) {
        try {
            request.setModUser(currentUser.getUserKey());
            apiRepository.updateApiEcho(request);
        } catch (Exception e) {
            log.error("updateApiEcho :" + e.toString());
            throw new BusinessException("E186", messageSource.getMessage("E186"));
        }

        return ResponseEntity.ok(new SignUpResponse(true, "대응답정보를 성공적으로 수정하였습니다."));
    }

    /** 대응답 정보용 : 대응답 상세 정보 불러오기 **/
    @PostMapping("/detailApiEcho")
    public ResponseEntity<?> detailApiEcho(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.RegEchoRequest request) {

        EchoResponse response = new EchoResponse();

        try {
            EchoVO echo = apiRepository.detailApiEcho(request);
            response.setEcho(echo);
        } catch (Exception e) {
            log.error("detailApiEcho :" + e.toString());
            throw new BusinessException("E187", messageSource.getMessage("E187"));
        }

        return ResponseEntity.ok(response);
    }

    /** 대응답 정보용 : 해당 API의 대응답정보 리스트 불러오기 **/
    @PostMapping("/apiEchos")
    public ResponseEntity<?> apiEchos(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.RegEchoRequest request) {

        EchoResponse.EchoListResponse response = new EchoResponse.EchoListResponse();

        try {
            response.setEchoList(apiRepository.apiEchos(request));
        } catch (Exception e) {
            log.error("apiEchos :" + e.toString());
            throw new BusinessException("E187", messageSource.getMessage("E187"));
        }

        return ResponseEntity.ok(response);
    }

    /** 대응답 정보용 : 대응답 상세 정보 삭제 **/
    @PostMapping("/deleteApiEcho")
    public ResponseEntity<?> deleteApiEcho(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.RegEchoRequest request) {

        try {
            request.setModUser(currentUser.getUserKey());
            apiRepository.deleteApiEcho(request);
        } catch (Exception e) {
            log.error("deleteApiEcho :" + e.toString());
            throw new BusinessException("E188", messageSource.getMessage("E188"));
        }

        return ResponseEntity.ok(new SignUpResponse(true, "대응답정보를 성공적으로 삭제하였습니다."));
    }

    /** 대응답 정보용 : API 검색 키 등록 **/
    @PostMapping("/regApiSearchKey")
    public ResponseEntity<?> regApiSearchKey(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.RegSearchKeyRequest request) {

        try {
            request.setRegUser(currentUser.getUserKey());
            apiRepository.regApiSearchKey(request);

        } catch (Exception e) {
            log.error("regApiSearchKey :" + e.toString());
            throw new BusinessException("E189", messageSource.getMessage("E189"));
        }

        return ResponseEntity.ok(new SignUpResponse(true, "API 검색키를 성공적으로 등록하였습니다."));
    }

    /** 대응답 정보용 : API 검색 키 업데이트 **/
    @PostMapping("/updateApiSearchKey")
    public ResponseEntity<?> updateApiSearchKey(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.RegSearchKeyRequest request) {

        try {
            request.setModUser(currentUser.getUserKey());

            if(StringUtils.equals(request.getSearchKey(), "NULL") || request.getSearchKey() == "NULL") {
                apiRepository.deleteApiEchoAll(request);
            }
            apiRepository.updateApiSearchKey(request);

        } catch (Exception e) {
            log.error("updateApiSearchKey :" + e.toString());
            throw new BusinessException("E190", messageSource.getMessage("E190"));
        }

        return ResponseEntity.ok(new SignUpResponse(true, "API 검색키를 성공적으로 변경하였습니다."));
    }

    /** 대응답 정보용 : API 검색 키 가져오기 **/
    @PostMapping("/getApiSearchKey")
    public ResponseEntity<?> getApiSearchKey(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EchoRequest.RegSearchKeyRequest request) {

        EchoResponse echo = new EchoResponse();
        try {
            echo.setEcho(apiRepository.getApiSearchKey(request));

        } catch (Exception e) {
            log.error("getApiSearchKey :" + e.toString());
            throw new BusinessException("E191", messageSource.getMessage("E191"));
        }

        return ResponseEntity.ok(echo);
    }
}