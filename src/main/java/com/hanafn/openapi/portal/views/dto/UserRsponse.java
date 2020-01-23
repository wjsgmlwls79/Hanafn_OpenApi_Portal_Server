package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.UserVO;
import lombok.Data;

import java.util.List;

@Data
public class UserRsponse {

    @Data
    public static class UserDupCheckResponse{
        private String userIdDupYn;
        private String hfnIdDupYn;
        private String userEmailDupYn;
    }

    @Data
    public static class UserTmpPwdIssueResponse{
        private String tepPwd;
    }

    @Data
    public static class DeveloperListResponse{
        private List<UserVO> devList;
    }

}
