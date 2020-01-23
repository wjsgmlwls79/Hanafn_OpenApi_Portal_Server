package com.hanafn.openapi.portal.views.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.views.dto.ApiCtgrRsponse;
import com.hanafn.openapi.portal.views.dto.ApisRequest;
import com.hanafn.openapi.portal.views.repository.ApisRepository;
import com.hanafn.openapi.portal.views.vo.ApiCtgrVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ApisService {

    private final ObjectMapper objectMapper;
    private final ApisRepository apisRepository;
    private final MessageSourceAccessor messageSource;

    public List<ApiCtgrVO> getApisAll() {
        ApiCtgrRsponse apiCtgrRsponse = new ApiCtgrRsponse();
        try {
            List<ApiCtgrVO> data = apisRepository.selectApisAll();
            return data;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }
    }

    public ApiCtgrRsponse.ApisResponse getApis(ApisRequest request) {
        try {
            ApiCtgrRsponse.ApisResponse apisResponse = new ApiCtgrRsponse.ApisResponse();
            ApiCtgrVO apiCtgrVO = apisRepository.selectApis(request);
            apisResponse = objectMapper.convertValue(apiCtgrVO, ApiCtgrRsponse.ApisResponse.class);
            apisResponse.setList(apisRepository.selectApisList(request));
            return apisResponse;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException("E026",messageSource.getMessage("E026"));
        }
    }
}
