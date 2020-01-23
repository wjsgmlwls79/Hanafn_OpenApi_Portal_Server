package com.hanafn.openapi.portal.aspectj.aspectComponent;

import com.hanafn.openapi.portal.views.repository.LogRepository;
import com.hanafn.openapi.portal.views.vo.TrxInfoVO;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Component
@Getter
@Slf4j
public class TrxInfoBean {

    @Autowired
    LogRepository logRepository;

    private List<TrxInfoVO> trxInfoList;

    @PostConstruct
    public void postConstruct() {
        this.trxInfoList = logRepository.selectTrxInfoAll();
    }

    @PreDestroy
    public void preDestroy() {
        log.error("preDestroy Called [" + this );
    }
}
