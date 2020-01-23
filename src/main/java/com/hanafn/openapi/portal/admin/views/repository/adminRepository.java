package com.hanafn.openapi.portal.admin.views.repository;

import com.hanafn.openapi.portal.admin.views.dto.MenuRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface adminRepository {
    void insertWebPageAccessLog(MenuRequest menuRequest);
}