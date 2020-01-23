package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("userWithdraw")
public class UserWithdrawVO {
    private String userKey;
    private String reasonGb;
    private String reasonDetail;
    private String regDttm;
    private String approvalDttm;
}
