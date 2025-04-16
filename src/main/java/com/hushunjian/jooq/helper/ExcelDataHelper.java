package com.hushunjian.jooq.helper;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.alibaba.excel.EasyExcelFactory.read;

@Slf4j
public class ExcelDataHelper {


    private ExcelDataHelper() {

    }

    public static ExcelData readExcelData(byte[] bytes, ExcelAnalysisModel analysisModel) {
        return readExcel(new ByteArrayInputStream(bytes), analysisModel).getExcelData();
    }

    public static ExcelData readExcelData(byte[] bytes) {
        return readExcelData(bytes, ExcelAnalysisModel.COLUMN_NAME);
    }

    public static ExcelData readExcelData(InputStream inputStream) {
        return readExcel(inputStream, ExcelAnalysisModel.COLUMN_NAME).getExcelData();
    }

    public static ExcelData readExcelData(InputStream inputStream, ExcelAnalysisModel analysisModel) {
        return readExcel(inputStream, analysisModel).getExcelData();
    }

    private static ExcelDataListener readExcel(InputStream inputStream, ExcelAnalysisModel analysisModel) {
        ExcelDataListener listener = new ExcelDataListener();
        // 设置解析模式
        listener.setAnalysisModel(analysisModel);
        // 读取excel数据
        read(inputStream, listener).ignoreEmptyRow(true).doReadAll();
        return listener;
    }

}
