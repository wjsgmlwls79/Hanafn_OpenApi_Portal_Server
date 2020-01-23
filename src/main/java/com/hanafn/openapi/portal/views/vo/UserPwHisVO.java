package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("userPwHis")
public class UserPwHisVO {
    private String seq;
    private String userKey;
    private String userId;
    private String userPwd;
    private String pwChangeDt;
}
