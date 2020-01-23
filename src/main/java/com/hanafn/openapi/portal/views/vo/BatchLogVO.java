package com.hanafn.openapi.portal.views.vo;

import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("batchLog")
public class BatchLogVO {
    // 배치 히스토리 ID
    private long batchHistoryId;
    // 배치 ID
    private String batchId;
    // 처리건수
    private int numberOfProcessing;
    // 배치 시작시간
    private String batchStartDate;
    // 배치 종료시간
    private String batchEndDate;
    // 배치 결과
    private String batchResult;
}
