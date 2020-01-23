package com.hanafn.openapi.portal.cmct.dto;

import lombok.Data;

import java.util.Map;

@Data
public class NoticeServerResponse {
    private Map<String, Object> dataHeader;
    private Map<String, Object> dataBody;
}
