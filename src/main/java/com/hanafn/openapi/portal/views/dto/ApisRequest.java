package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApisRequest {

    @NotNull
    private String ctgrCd;
}
