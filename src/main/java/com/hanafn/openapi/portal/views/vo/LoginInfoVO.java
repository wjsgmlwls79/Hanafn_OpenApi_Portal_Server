package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("loginInfo")
public class LoginInfoVO {
    private String userKey;
    private String userId;
    private String userType;
    private String loginLock;
    private String loginLockTime;
    private int loginFailCnt;
}
