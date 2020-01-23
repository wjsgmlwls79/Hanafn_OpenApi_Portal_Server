package com.hanafn.openapi.portal.cmct;

import com.hanafn.openapi.portal.views.dto.CommonApiResponse;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

public interface CommunicatorInterface {
    public CommonApiResponse communicateServer(String url, HttpMethod method, Object data, boolean procError);
}
