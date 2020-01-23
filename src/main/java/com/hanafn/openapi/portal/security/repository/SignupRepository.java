package com.hanafn.openapi.portal.security.repository;

import com.hanafn.openapi.portal.security.dto.HfnLoginRequest;
import com.hanafn.openapi.portal.security.dto.SignUpRequest;
import com.hanafn.openapi.portal.views.dto.SndCertMgntRequest;
import com.hanafn.openapi.portal.views.dto.UserLoginRequest;
import com.hanafn.openapi.portal.views.dto.UserRequest;
import com.hanafn.openapi.portal.views.vo.UseorgVO;
import com.hanafn.openapi.portal.views.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SignupRepository {
    void insertSignupUseorg(SignUpRequest signUpRequest);
    void updateSignupUseorgRegId(SignUpRequest signUpRequest);

    void insertUserSignup(SignUpRequest signUpRequest);
    UserVO selectUserForUserIdAndWait(SignUpRequest signUpRequest);
    void insertUserRoleForUseorg(SignUpRequest signUpRequest);

    /*********** 개인 사용자 등록 관련(동사/목적테이블(모델))***********/
    void insertUser(SignUpRequest.UserSingnUpRequest request);
    void insertLogin(SignUpRequest.UserSingnUpRequest request);
    void insertPwHisUser(SignUpRequest.UserSingnUpRequest request);
    void insertUserRoleForUser(SignUpRequest.UserSingnUpRequest request);
    String selectUserUserId(SignUpRequest.UserSingnUpRequest request);
    int checkDropUserOrNot(SignUpRequest.UserSingnUpRequest request);
    void insertSndCertMgntForSelfAuth(SndCertMgntRequest request);

    /*********** 법인사용자 등록 ***********/
    void insertUseorg(SignUpRequest.UseorgSignUpRequest request);
    void insertLoginUseorg(SignUpRequest.UseorgSignUpRequest request);
    void insertPwHisUseorg(SignUpRequest.UseorgSignUpRequest request);
    void insertRoleUseorg(SignUpRequest.UseorgSignUpRequest request);
    int useorgEmailDupCheckWhenSignUp(UserRequest.UserDupCheckRequest userDupCheckRequest);

    UseorgVO selectUseorg(SignUpRequest.UseorgSignUpRequest request);
    String selectUser(SignUpRequest.UserSingnUpRequest request);
}