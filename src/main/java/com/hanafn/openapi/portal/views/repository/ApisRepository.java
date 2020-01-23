package com.hanafn.openapi.portal.views.repository;

import com.hanafn.openapi.portal.views.dto.*;
import com.hanafn.openapi.portal.views.vo.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ApisRepository {

	List<ApiCtgrVO> selectApisAll();
	List<ApiVO> selectApisList(ApisRequest request);
	ApiCtgrVO selectApis(ApisRequest request);
}