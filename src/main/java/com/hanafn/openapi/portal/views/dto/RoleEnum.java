package com.hanafn.openapi.portal.views.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.util.annotation.Nullable;

import javax.annotation.PostConstruct;

public enum RoleEnum {
    ROLE_SYS_ADMIN("1","ROLE_SYS_ADMIN"),
    ROLE_HFN_ADMIN("2","ROLE_HFN_ADMIN"),
    ROLE_HFN_USER("3","ROLE_HFN_USER"),
    ROLE_ORG_ADMIN("4","ROLE_ORG_ADMIN"),
    ROLE_ORG_USER("5", "ROLE_ORG_USER"),
    ROLE_PERSONAL("6", "ROLE_PERSONAL");


    private String value;
    private String name;

    RoleEnum(String value, String name ) {
        this.name = name;
        this.value = value;
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


    @Nullable
    public static RoleEnum resolve(String name) {
        for (RoleEnum roleInfo : RoleEnum.values()) {
            if (StringUtils.equals(name, roleInfo.getName())) {
                return roleInfo;
            }
        }
        return null;
    }
}
