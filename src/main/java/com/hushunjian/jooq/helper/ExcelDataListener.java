package com.hushunjian.jooq.helper;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class ExcelDataListener extends AnalysisEventListener<Map<Integer, String>> {

    /**
     * 解析模式 列名模式:COLUMN_NAME;列下标模式:COLUMN_INDEX;列下标|列名模式:COLUMN_INDEX_NAME
     */
    private ExcelAnalysisModel analysisModel;

    /**
     * 各sheet数据
     */
    private final Map<String, List<Map<String, String>>> sheetRowsMap = Maps.newHashMap();

    /**
     * 各sheet表头
     */
    private final Map<String, Map<Integer, String>> sheetHeadMap = Maps.newHashMap();

    @Override
    public void invoke(Map<Integer, String> columnValueMap, AnalysisContext analysisContext) {
        // 取出当前sheet
        String sheetName = analysisContext.readSheetHolder().getSheetName();
        // 每一行的数据
        Map<String, String> result = Maps.newLinkedHashMap();
        // 当前表头map
        Map<Integer, String> currentHeadMap = sheetHeadMap.get(sheetName);
        // 按照表头全部填充数据
        currentHeadMap.forEach((index, head) -> this.analysisModel.putKeyValue(result, index, head, columnValueMap.get(index)));
        sheetRowsMap.computeIfAbsent(sheetName, v -> Lists.newArrayList()).add(result);
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext analysisContext) {
        // 读取表头数据
        sheetHeadMap.put(analysisContext.readSheetHolder().getSheetName(), headMap);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public void setAnalysisModel(ExcelAnalysisModel analysisModel) {
        this.analysisModel = analysisModel;
    }

    public Map<String, List<Map<String, String>>> getSheetRowsMap() {
        return sheetRowsMap;
    }

    public ExcelData getExcelData() {
        return new ExcelData(sheetHeadMap, sheetRowsMap);
    }

}
