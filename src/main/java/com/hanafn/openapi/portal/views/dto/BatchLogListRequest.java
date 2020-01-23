package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class BatchLogListRequest {

    private String batchId;
    private String batchResult;
    private String searchStDt;
    private String searchEnDt;

    private int pageIdx = 0;
    private int pageSize = 20;
    private int pageOffset = 0;
}
