package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Alias("useorglist")
public class UseorgListVO {

    private String userKey;
    private String useorgNm;
}
