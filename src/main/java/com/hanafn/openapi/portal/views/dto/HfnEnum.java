package com.hanafn.openapi.portal.views.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.util.annotation.Nullable;

import javax.annotation.PostConstruct;
import java.util.EnumSet;

public enum HfnEnum {

    HFN("00", "하나금융지주", "http://localhost:8080/test/noticeToHfnTest", "default", "hfn"),
    HBK("01", "KEB하나은행", "", "", "hbk" ),
    HNW("02", "하나금융투자", "http://localhost:8080/test/noticeToHfnTest", "http://10.167.36.219:28688/v1/useorg-AplvInfo", "hnw" ),
    HLF("04", "하나생명", "http://localhost:8080/test/noticeToHfnTest", "default", "hlf" ),
    HCP("05", "하나캐피탈", "", "", "hcp" ),
    HTI("07", "하나금융티아이", "http://localhost:8080/test/noticeToHfnTest", "default", "hti" ),
    HCD("12", "하나카드", "http://localhost:8080/test/noticeToHfnTest", "default","hcd" ),
    HSV("14", "하나저축은행", "http://localhost:8080/test/noticeToHfnTest", "default","hsv" ),
    HMB("99", "하나멤버스", "http://localhost:8080/test/noticeToHfnTest", "default","hmb" );


    private String value;
    private String name;
    private String appApiaAplvUrl;
    private String useorgAplvUrl;
    private String code;

    HfnEnum(String value, String name, String appApiaAplvUrl, String useorgAplvUrl, String code) {
        this.value = value;
        this.name = name;
        this.appApiaAplvUrl = appApiaAplvUrl;
        this.useorgAplvUrl = useorgAplvUrl;
        this.code = code;
    }

    @Component
    private static class HfnEnumUrlInjector {
        @Value("${hbk.appApiaAplvUrl}")
        private String hbkAppApiServiceUrl;

        @Value("${hbk.useorgAplvUrl}")
        private String hbkUseorgAplvUrl;

        @Value("${hcp.appApiaAplvUrl}")
        private String hcpAppApiServiceUrl;

        @Value("${hcp.useorgAplvUrl}")
        private String hcpUseorgAplvUrl;

        @PostConstruct
        private void postConstruct() {
            HfnEnum.HBK.setAppApiaAplvUrl(hbkAppApiServiceUrl);
            HfnEnum.HBK.setUseorgAplvUrl(hbkUseorgAplvUrl);

            HfnEnum.HCP.setAppApiaAplvUrl(hcpAppApiServiceUrl);
            HfnEnum.HCP.setUseorgAplvUrl(hcpUseorgAplvUrl);
        }
    }
    
    /**
     * Return the String value of HfnCompanyInfo
     */
    public String value() {
        return this.value;
    }

    /**
     * Return the Name of this HfnCompanyInfo
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the Key of this HfnCompanyInfo
     */
    public String getCode() {
    return this.code;
}

    /**
     * Get the urls
     */
    public String getAppApiaAplvUrl() {
        return this.appApiaAplvUrl;
    }
    public String getUseorgAplvUrl() {
    return this.useorgAplvUrl;
}

    /**
     *  Set the urls based by properties
     * @param url
     */
    private void setAppApiaAplvUrl(String url) {
            this.appApiaAplvUrl = url;
        }
        private void setUseorgAplvUrl(String url) {
            this.useorgAplvUrl = url;
        }
    
    @Nullable
    public static HfnEnum resolve(String hfnCd) {
        for (HfnEnum hfnCompanyInfo : HfnEnum.values()) {
            if (StringUtils.equals(hfnCd, hfnCompanyInfo.value())) {
                return hfnCompanyInfo;
            }
        }
        return null;
    }

    @Nullable
    public static HfnEnum resolveByHfnName(String hfnName) {
        for (HfnEnum hfnCompanyInfo : HfnEnum.values()) {
            if (StringUtils.equals(hfnName, hfnCompanyInfo.getCode())) {
                return hfnCompanyInfo;
            }
        }
        return null;
    }
}