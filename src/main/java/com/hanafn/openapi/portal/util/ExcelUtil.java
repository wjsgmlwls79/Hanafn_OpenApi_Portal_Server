package com.hanafn.openapi.portal.util;

import com.hanafn.openapi.portal.cmct.AccountStatusCommunicater;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.security.UserPrincipal;
import com.hanafn.openapi.portal.views.dto.ApiRequest;
import com.hanafn.openapi.portal.views.dto.SettlementRequest;
import com.hanafn.openapi.portal.views.repository.ApiRepository;
import com.hanafn.openapi.portal.views.repository.SettlementRepository;
import com.hanafn.openapi.portal.views.service.AppsService;
import com.hanafn.openapi.portal.views.vo.FeeCollectionInfoVO;
import com.hanafn.openapi.portal.views.vo.RequestApiVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component("statXls")
@Slf4j
public class ExcelUtil {

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    SettlementRepository settlementRepository;

    @Autowired
    AppsService appsService;

    @Autowired
    MessageSourceAccessor messageSource;

    @Autowired
    AccountStatusCommunicater accountStatusCommunicater;

    public void excelDown(ApiRequest request, HttpServletResponse response) {

        // 워크북 생성
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("엑셀다운");
        Row row = null;
        Cell cell = null;
        int rowNo = 0;

        // 테이블 헤더용 스타일
        CellStyle headStyle = wb.createCellStyle();
        setHeaderStyle(headStyle);

        // 데이터용 스타일
        CellStyle bodyStyle = wb.createCellStyle();
        setBodyStyle(bodyStyle);

        // 헤더 생성
        row = sheet.createRow(rowNo++);
        setHeader(row, cell, headStyle);

        List<RequestApiVO> apiList = apiRepository.getApiChargeList(request);

        // 데이터 부분 생성
        for(RequestApiVO data : apiList) {
//            dataSet(data);
            row = sheet.createRow(rowNo++);
            setBody(row, cell, bodyStyle, data);
        }


        // 컨텐츠 타입과 파일명 지정
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=APILIST.xls");

        try {
            // 엑셀 출력
            wb.write(response.getOutputStream());
            wb.close();
        }catch(Exception e) {
            e.printStackTrace();
            log.error("ExcelUtils write Error: " + e.toString());
            throw new RuntimeException("ExcelUtils write Error", e.getCause());
        }
    }

    private void dataSet(RequestApiVO data) {
        ApiRequest api = new ApiRequest();

        api.setAppKey(data.getAppKey());
        api.setApiId(data.getApiId());
        api.setMsDate(data.getMsDate());

        List<RequestApiVO> apiDetailList = apiRepository.getApiDetailList(api);

        int finalCost = 0;		// 최종요금
        int rateCnt = 0;		// 할인율건수
        int rate = 0;			// 할인율
        int useCnt = 0;			// 사용건수
        int totalCost = 0;		// 총금액
        int minimumUseNumber = Integer.parseInt(data.getMinimumUseNumber());

        int finalCnt = 0;
        for (RequestApiVO detailData : apiDetailList) {
            rateCnt++;
            rate = Integer.parseInt(detailData.getDiscountRate());
            useCnt += Integer.parseInt(detailData.getUseCnt());
            totalCost += Integer.parseInt(detailData.getTotalCost());

            minimumUseNumber = minimumUseNumber - Integer.parseInt(detailData.getUseCnt());

            if (minimumUseNumber > 0) {
                finalCost += 0;
            } else {
                finalCnt++;
                double rateCal = 1.0 - (double)rate / 100;
                if (finalCnt == 1) {
                    int minimumCharges = (int)(Double.parseDouble(data.getMiniMumCharges()) * rateCal); // 최소부과요금
                    finalCost += minimumCharges;
                }
                finalCost += (int)(((double)(-minimumUseNumber) * Double.parseDouble(data.getFeeAmount())) * rateCal);
                minimumUseNumber = 0;
            }
        }

        // 최종요금 계산
        if (finalCost == 0) {
            double finalCalCost = 0;
            double minimumCarge = Double.parseDouble(data.getMiniMumCharges());
            double rateCal = 1.0 - (double)rate / 100;
            finalCalCost = minimumCarge * rateCal;
            data.setFinalCost(String.valueOf((int)finalCalCost));
        } else {
            data.setFinalCost(String.valueOf(finalCost));
        }

        // 할인율
        if (rateCnt > 1) {
            data.setDiscountRate("다건");
        } else {
            data.setDiscountRate(String.valueOf(rate));
        }

        // 사용건수
        data.setUseCnt(String.valueOf(useCnt));

        if (Integer.parseInt(data.getMinimumUseNumber()) >= useCnt) {
            data.setComment("최소청구금액 적용");
        }

        // 총금액
        data.setTotalCost(String.valueOf(totalCost));
    }

    // 헤더 스타일
    private void setHeaderStyle(CellStyle headStyle) {
        // 가는 경계선
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);

        // 배경색은 노란색
        headStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 데이터는 가운데 정렬
        headStyle.setAlignment(HorizontalAlignment.CENTER);
    }

    // 헤더 생성
    private void setHeader(Row row, Cell cell, CellStyle headStyle) {
        cell = row.createCell(0);
        cell.setCellStyle(headStyle);
        cell.setCellValue("해당월");
        cell = row.createCell(1);
        cell.setCellStyle(headStyle);
        cell.setCellValue("앱명");
        cell = row.createCell(2);
        cell.setCellStyle(headStyle);
        cell.setCellValue("API명");
        cell = row.createCell(3);
        cell.setCellStyle(headStyle);
        cell.setCellValue("기본금액");
        cell = row.createCell(4);
        cell.setCellStyle(headStyle);
        cell.setCellValue("사용건수");
        cell = row.createCell(5);
        cell.setCellStyle(headStyle);
        cell.setCellValue("총금액");
        cell = row.createCell(6);
        cell.setCellStyle(headStyle);
        cell.setCellValue("할인율");
        cell = row.createCell(7);
        cell.setCellStyle(headStyle);
        cell.setCellValue("최종요금");
        cell = row.createCell(8);
        cell.setCellStyle(headStyle);
        cell.setCellValue("비고");
    }

    // 데이터영역 스타일
    private void setBodyStyle(CellStyle bodyStyle) {
        bodyStyle.setBorderTop(BorderStyle.THIN);
        bodyStyle.setBorderBottom(BorderStyle.THIN);
        bodyStyle.setBorderLeft(BorderStyle.THIN);
        bodyStyle.setBorderRight(BorderStyle.THIN);
    }

    // 데이터영역 생성
    private void setBody(Row row, Cell cell, CellStyle bodyStyle, RequestApiVO data) {
        cell = row.createCell(0);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getMsDate());
        cell = row.createCell(1);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getAppNm());
        cell = row.createCell(2);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getApiNm());
        cell = row.createCell(3);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getFeeAmount());
        cell = row.createCell(4);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getUseCnt());
        cell = row.createCell(5);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getTotalCost());
        cell = row.createCell(6);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getDiscountRate());
        cell = row.createCell(7);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getFinalCost());
        cell = row.createCell(8);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getComment());
    }


    // *********************  과금정산 ************************** //

    public void feeExcelDownload(SettlementRequest request, HttpServletResponse response) {

        // 워크북 생성
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("엑셀다운");
        Row row = null;
        Cell cell = null;
        int rowNo = 0;

        // 테이블 헤더용 스타일
        CellStyle headStyle = wb.createCellStyle();
        setHeaderStyle(headStyle);

        // 데이터용 스타일
        CellStyle bodyStyle = wb.createCellStyle();
        setBodyStyle(bodyStyle);

        // 헤더 생성
        row = sheet.createRow(rowNo++);
        setFeeHeader(row, cell, headStyle);

        List<FeeCollectionInfoVO> feeList = settlementRepository.getFeeCollectionInfoList(request);

        // 데이터 부분 생성
        for(FeeCollectionInfoVO data : feeList) {
            row = sheet.createRow(rowNo++);
            setFeeBody(row, cell, bodyStyle, data);
        }


        // 컨텐츠 타입과 파일명 지정
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=FEE_LIST.xls");

        try {
            // 엑셀 출력
            wb.write(response.getOutputStream());
            wb.close();
        }catch(Exception e) {
            e.printStackTrace();
            log.error("ExcelUtils write Error: " + e.toString());
            throw new RuntimeException("ExcelUtils write Error", e.getCause());
        }
    }

    // 헤더 생성
    private void setFeeHeader(Row row, Cell cell, CellStyle headStyle) {
        cell = row.createCell(0);
        cell.setCellStyle(headStyle);
        cell.setCellValue("청구월");
        cell = row.createCell(1);
        cell.setCellStyle(headStyle);
        cell.setCellValue("관계사코드");
        cell = row.createCell(2);
        cell.setCellStyle(headStyle);
        cell.setCellValue("기관명");
        cell = row.createCell(3);
        cell.setCellStyle(headStyle);
        cell.setCellValue("앱키");
        cell = row.createCell(4);
        cell.setCellStyle(headStyle);
        cell.setCellValue("앱명");
        cell = row.createCell(5);
        cell.setCellStyle(headStyle);
        cell.setCellValue("총금액");
        cell = row.createCell(6);
        cell.setCellStyle(headStyle);
        cell.setCellValue("출금요청금액");
        cell = row.createCell(7);
        cell.setCellStyle(headStyle);
        cell.setCellValue("출금요청계좌");
        cell = row.createCell(8);
        cell.setCellStyle(headStyle);
        cell.setCellValue("출금요청적요");
    }

    // 데이터영역 생성
    private void setFeeBody(Row row, Cell cell, CellStyle bodyStyle, FeeCollectionInfoVO data) {
        cell = row.createCell(0);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getBilMonth());
        cell = row.createCell(1);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getHfnCd());
        cell = row.createCell(2);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getUseorgNm());
        cell = row.createCell(3);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getAppKey());
        cell = row.createCell(4);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getAppNm());
        cell = row.createCell(5);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getTotAmt());
        cell = row.createCell(6);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getWdAmt());
        cell = row.createCell(7);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getWdAcno());
        cell = row.createCell(8);
        cell.setCellStyle(bodyStyle);
        cell.setCellValue(data.getWdMemo());
    }


    public ResponseEntity<String> settlementExcelUpload(SettlementRequest settlementRequest, HttpServletRequest request, HttpServletResponse response, UserPrincipal currentUser) throws Exception{
        return parseTableExcel(settlementRequest, request ,currentUser);
    }

    /**
     * 엑셀 데이터 추출
     *
     * @param request
     * @return
     * @throws Exception
     */
    private ResponseEntity<String> parseTableExcel(SettlementRequest settlementRequest, HttpServletRequest request, UserPrincipal currentUser) throws Exception {

        ResponseEntity<String> data = null;

        try {

            Workbook workbook;

            MultipartFile excelFile = settlementRequest.getFileData();

            if (excelFile.getOriginalFilename().endsWith(".xls")) {
                workbook = new HSSFWorkbook(excelFile.getInputStream());
            } else {
                workbook = new XSSFWorkbook(excelFile.getInputStream());
            }

            Sheet sheet = (Sheet) workbook.getSheetAt(0);

            for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (i > 0) {
                    if (StringUtils.isNotEmpty(row.getCell(0).getStringCellValue())) {

                        String acno = "";

                        if (row.getCell(7).getCellTypeEnum() == CellType.STRING) {
                            acno = row.getCell(7).getStringCellValue();
                        } else {
                            acno = String.valueOf(row.getCell(7).getNumericCellValue());
                        }

                        if (!acno.trim().equals("")) {
                            // 계좌검증
                            data = accountStatusCommunicater.communicateServer(currentUser, request, acno);
                        } else {
                            throw new BusinessException("E103", messageSource.getMessage("E103"));
                        }

                        FeeCollectionInfoVO excelVO = new FeeCollectionInfoVO();

                        excelVO.setBilMonth(row.getCell(0).getStringCellValue());
                        excelVO.setHfnCd(row.getCell(1).getStringCellValue());
                        excelVO.setUseorgNm(row.getCell(2).getStringCellValue());
                        excelVO.setAppKey(row.getCell(3).getStringCellValue());
                        excelVO.setAppNm(row.getCell(4).getStringCellValue());


                        if (row.getCell(5).getCellTypeEnum() == CellType.STRING) {
                            excelVO.setTotAmt(row.getCell(5).getStringCellValue());
                        } else {
                            excelVO.setTotAmt(String.valueOf(row.getCell(5).getNumericCellValue()));
                        }

                        if (row.getCell(6).getCellTypeEnum() == CellType.STRING) {
                            excelVO.setWdAmt(row.getCell(6).getStringCellValue());
                        } else {
                            excelVO.setWdAmt(String.valueOf(row.getCell(6).getNumericCellValue()));
                        }

                        if (row.getCell(7).getCellTypeEnum() == CellType.STRING) {
                            excelVO.setWdAcno(row.getCell(7).getStringCellValue());
                        } else {
                            excelVO.setWdAcno(String.valueOf(row.getCell(7).getNumericCellValue()));
                        }

                        excelVO.setWdMemo(row.getCell(8).getStringCellValue());

                        int result = settlementRepository.setFeeExcelUpload(excelVO);

                        if (result == 0) {
                            throw new BusinessException("E096", messageSource.getMessage("E096"));
                        }
                    }
                }
            }
        } catch (BusinessException e) {
            e.printStackTrace();
            throw new BusinessException(e.getErrorCode(), messageSource.getMessage(e.getErrorCode()));
        }

        return data;
    }
}
