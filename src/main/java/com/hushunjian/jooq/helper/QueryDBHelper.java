package com.hushunjian.jooq.helper;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.DData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.excel.EasyExcelFactory.write;
import static com.alibaba.excel.EasyExcelFactory.writerSheet;

@Slf4j
public class QueryDBHelper {


    @SneakyThrows
    public static D getRes(QueryDBReq req, RestTemplate restTemplate) {
        String url = "http://db.platform.taimeicloud.cn/query/";
        HttpHeaders headers = new HttpHeaders();

        LinkedMultiValueMap<String, Object> bodyParams = new LinkedMultiValueMap<>();

        bodyParams.put("instance_name", Lists.newArrayList(req.getInstanceName()));
        bodyParams.put("db_name", Lists.newArrayList(req.getDbName()));
        bodyParams.put("sql_content", Lists.newArrayList(req.getSqlContent()));
        bodyParams.put("limit_num", Lists.newArrayList(req.getLimitNum()));
        headers.add("Cookie", req.getCookie());
        headers.add("X-Csrftoken", req.getCsrfToken());
        headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
//        headers.add("Accept-Encoding", "gzip, deflate");
//        headers.add("Accept-Language", "zh-CN,zh;q=0.9");
//        headers.add("Connection", "keep-alive");
//        headers.add("Content-Length", "110");
//        headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        headers.add("Host", "db.platform.taimeicloud.cn");
//        headers.add("Origin", "http://db.platform.taimeicloud.cn");
//        headers.add("Referer", "http://db.platform.taimeicloud.cn/sqlquery/");
//        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
//        headers.add("X-Requested-With", "XMLHttpRequest");
        HttpEntity<Object> requestEntity = new HttpEntity<>(bodyParams, headers);
        Thread.sleep(200);
        long start = System.currentTimeMillis();

        ResponseEntity<D> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, D.class);
        D body = responseEntity.getBody();
        log.info("查询耗时:[{}],查询语句:[{}]", System.currentTimeMillis() - start, req.getSqlContent());
        if (body == null) {
            throw new RuntimeException("查询出错!");
        }
        // 查询结果
        log.info("查询结果:[{}],数据条数:[{}]", JSON.toJSONString(body), body.getData().getRows().size());
        return body;
    }

    public static void exportExcel(Map<String, Pair<List<List<String>>, List<List<String>>>> exportDataMap,
                                   String path,
                                   String db) {
        // 生成临时文件
        File tempFile = genTempFile(path, db + ".xlsx");
        // 创建构建器
        ExcelWriter writer = write(tempFile).build();
        // 循环配置
        AtomicInteger sheetNo = new AtomicInteger(0);
        exportDataMap.forEach((sheetName, sheetPair) -> {
            // 表头创建sheet
            WriteSheet sheet = createSheet(sheetPair.getKey().stream().map(Lists::newArrayList).collect(Collectors.toList()), sheetName, sheetNo.getAndIncrement());
            // 写入表格内容
            writer.write(sheetPair.getValue(), sheet);
        });
        // 完成
        writer.finish();
    }

    public static void exportExcel(List<List<String>> headers, List<List<String>> table, String fileName) {
        // 生成临时文件
        File tempFile = genTempFile(fileName);
        // 创建构建器
        ExcelWriter writer = write(tempFile).build();
        // 创建sheet
        WriteSheet sheet = createSheet(headers);
        // 写入表格
        writer.write(table, sheet);
        // 完成
        writer.finish();
        System.out.println(tempFile.getAbsolutePath());
        System.out.println(tempFile.getName());
    }


    public static void exportExcel(DData resData, String fileName) {
        // 表头
        List<List<String>> headers = resData.getColumn_list().stream().map(Lists::newArrayList).collect(Collectors.toList());
        // table
        List<List<String>> table = resData.getRows().stream().map(Lists::newArrayList).collect(Collectors.toList());
        // 导出
        exportExcel(headers, table, fileName);
    }

    private static File genTempFile(String fileName) {
        return genTempFile("C:\\Users\\shunjian.hu\\Desktop", genName(fileName));
    }

    private static File genTempFile(String path, String fileName) {
        return Paths.get(path, fileName).toFile();
    }

    private static String genName(String fileName) {
        return String.format("%s.xlsx", StringUtils.isBlank(fileName) ? UUID.randomUUID().toString() : fileName);
    }

    private static WriteSheet createSheet(List<List<String>> headers, String sheetName, Integer sheetNo) {
        // 创建sheet,并设置表头样式
        WriteSheet sheet = writerSheet().registerWriteHandler(getHandler()).build();
        // 设置表头
        sheet.setHead(headers);
        // 设置名称
        sheet.setSheetName(sheetName);
        // 设置位置
        sheet.setSheetNo(sheetNo);
        // 设置列宽
        Map<Integer, Integer> columnWidthMap = Maps.newHashMap();
        for (int i = 0; i < headers.size(); i++) {
            columnWidthMap.put(i, 10000);
        }
        sheet.setColumnWidthMap(columnWidthMap);
        return sheet;
    }

    private static WriteSheet createSheet(List<List<String>> headers) {
        return createSheet("Sheet1", headers);
    }

    private static WriteSheet createSheet(String sheetName, List<List<String>> headers) {
        // 创建sheet,并设置表头样式
        WriteSheet sheet = writerSheet(sheetName).registerWriteHandler(getHandler()).build();
        // 设置表头
        sheet.setHead(headers);
        // 设置限制
        return sheet;
    }

    private static WriteHandler getHandler() {
        // 字体
        WriteFont font = new WriteFont();
        // 设置字号
        font.setFontHeightInPoints((short) 11);
        // 表头样式
        WriteCellStyle cellStyle = new WriteCellStyle();
        // 背景色
        cellStyle.setFillBackgroundColor(IndexedColors.WHITE.index);
        // 关闭自动换行
        cellStyle.setWrapped(false);
        // 设置字体配置
        cellStyle.setWriteFont(font);
        return new HorizontalCellStyleStrategy(cellStyle, cellStyle);
    }

}
