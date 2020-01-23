package com.hanafn.openapi.portal.views.dto;

import com.hanafn.openapi.portal.views.vo.StatsVO;
import lombok.Data;

import java.util.List;

@Data
public class StatsRsponse {

    private String apiTrxCnt;			//API 총 건수
    private String avgProcTerm;			//응답시간 평균
    private String maxProcTerm;			//최고 응답시간
    private String gwError;				//G/W 에러율
    private String apiError;			//API 에러율

    List<StatsVO> dayList;
    List<StatsVO> topApiUseList;
    List<StatsVO> topProcTermList;
    List<StatsVO> topApiError;

    @Data
    public static class UseorgStatsRsponse {

        private String apiTrxCnt;			//API 총 건수
        private String avgProcTerm;			//응답시간 평균
        private String maxProcTerm;			//최고 응답시간
        private String gwError;				//G/W 에러율
        private String apiError;			//API 에러율
        private String lastUpdateDate;

        List<StatsVO> dayList;
        List<StatsVO> appTrxList;
    }

    @Data
    public static class AppApiDetailStatsRsponse {

        private String appKey;
        private String appNm;
        private int appApiCnt;
        List<StatsVO> apiStatsList;
    }
}
