package com.hanafn.openapi.portal.views.dto;

import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Data
public class CommonApiResponse {
    private String requestApiUrl;
    private String statCd;
    private HttpStatus apiResultStatCd;
    private HttpEntity<String> requestEntity;
    private ResponseEntity<String> responseEntity;
    private Map<String,Object> responseData;
}
