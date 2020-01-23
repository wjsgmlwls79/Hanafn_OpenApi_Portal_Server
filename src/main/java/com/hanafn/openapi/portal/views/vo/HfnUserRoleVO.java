package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

@Data
@Alias("hfnuserrole")
@EqualsAndHashCode(callSuper = false)
public class HfnUserRoleVO {
    private String id;
    private String hfnCd;
    private String hfnNm;
    private String acessCd;
}
