package com.hanafn.openapi.portal.views.repository;

import com.hanafn.openapi.portal.views.dto.SettlementRequest;
import com.hanafn.openapi.portal.views.vo.FeeCollectionInfoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SettlementRepository {
    List<FeeCollectionInfoVO> getFeeCollectionInfoList(SettlementRequest request);
    void setFeeCollectionInfo(SettlementRequest request);

    void feeApprovalRequest(SettlementRequest request);
    void feeApprovalRequestConfirm(SettlementRequest request);

    void setFeeCollectionReject(SettlementRequest request);

    List<FeeCollectionInfoVO> getFeeCollectionHisList(SettlementRequest request);
    List<FeeCollectionInfoVO> getFeeCollectionHisListOffice(SettlementRequest request);
    void setFeeCollectionHisInfo(SettlementRequest request);

    FeeCollectionInfoVO selectUseorgListHfn(SettlementRequest request);

    String getHfnNm(String hfnCd);

    List<FeeCollectionInfoVO> getAppList(SettlementRequest request);
    List<FeeCollectionInfoVO> getUseOrgList(SettlementRequest request);
    List<FeeCollectionInfoVO> getWdRequestList(SettlementRequest request);
    List<FeeCollectionInfoVO> getWdResultList(SettlementRequest request);

    int setFeeExcelUpload(FeeCollectionInfoVO request);

    void setFeeHis(SettlementRequest request);
}
