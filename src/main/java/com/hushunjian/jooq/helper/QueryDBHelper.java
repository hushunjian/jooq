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
import com.hushunjian.jooq.req.QueryLogEventReq;
import com.hushunjian.jooq.res.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.excel.EasyExcelFactory.write;
import static com.alibaba.excel.EasyExcelFactory.writerSheet;

@Slf4j
public class QueryDBHelper {

    public static final String DB_COOKIE = "csrftoken=gZUi9yILcM13TGsDMuKmKaIwDVEVBq7U; sessionid=8pmvgta3029h3fpftw2y4t65b6v4nqrj";

    public static final String DB_CSRF_TOKEN = "gZUi9yILcM13TGsDMuKmKaIwDVEVBq7U";

    public static final String ESAFETY_5_COOKIE = "gr_user_id=8065e17c-9fe0-4464-be05-cc8651fce673; b322f7b164f88f10_gr_last_sent_cs1=8a81c08a7965a66b01798247b8c04c33; b322f7b164f88f10_gr_cs1=8a81c08a7965a66b01798247b8c04c33; Hm_lvt_88897af8840c3689db7cb8c0886b1026=1742551743; eSafety5_unblind_warn=true; acw_tc=1a0c39d217447683880546315e003bc664ea1b11d840c6b25f3da7fa371698; token=10e66448adde49e584c51a6ee05ad7a5";

    @SneakyThrows
    public static String getConfigMapStr(String serviceName, RestTemplate restTemplate, String cookie) {
        String searchAppsUrl = "http://itaimei.in.taimei.com/api/cicd-service/configMap/searchConfigMapApps";
        HttpHeaders headers = new HttpHeaders();
        // 头权限
        headers.add("Cookie", cookie);
        headers.add("Content-Type", "application/json;charset=UTF-8");
        // 入参
        Map<String, Object> bodyParams = Maps.newHashMap();
        bodyParams.put("namespaces", Lists.newArrayList("prod"));
        bodyParams.put("searchParam", serviceName);
        bodyParams.put("pageNum", 1);
        bodyParams.put("pageSize", 10);

        HttpEntity<Object> requestEntity = new HttpEntity<>(bodyParams, headers);
        Thread.sleep(200);
        ResponseEntity<ConfigMapRes> responseEntity = restTemplate.exchange(searchAppsUrl, HttpMethod.POST, requestEntity, ConfigMapRes.class);
        ConfigMapRes body = responseEntity.getBody();
        if (body == null) {
            throw new RuntimeException("查询出错!");
        }
        log.info("查询服务:[{}]数据", serviceName);
        String appId = body.getData().getList().get(0).getAppId();
        // 获取app yml
        String getAppYmlUrl = "http://itaimei.in.taimei.com/api/cicd-service/configMap/configMapYaml?namespace=prod&appId=" + appId;
        ResponseEntity<AppConfigMapRes> ymlRes = restTemplate.exchange(getAppYmlUrl, HttpMethod.GET, new HttpEntity<>(headers), AppConfigMapRes.class);
        AppConfigMapRes appConfigMapRes = ymlRes.getBody();
        if (appConfigMapRes == null) {
            throw new RuntimeException("查询出错!");
        }
        return appConfigMapRes.getData();
    }


    @SneakyThrows
    public static LogEventRes getRes(QueryLogEventReq req, RestTemplate restTemplate) {
        HttpHeaders headers = new HttpHeaders();
        // 头权限
        headers.add("Cookie", req.getCookie());
        headers.add("Authorization", req.getAuthorization());
        headers.add("Content-Type", "application/json;charset=UTF-8");
        ResponseEntity<LogEventRes> res = restTemplate.exchange(req.getUrl(), HttpMethod.GET, new HttpEntity<>(headers), LogEventRes.class);
        return res.getBody();
    }
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

    public static void exportExcel2(List<String> headers, List<List<String>> table, String fileName) {
        exportExcel(headers.stream().map(header -> Lists.newArrayList(header)).collect(Collectors.toList()), table, fileName);
    }


    public static void exportExcel(List<List<String>> headers, List<List<String>> table, String fileName) {
        if (CollectionUtils.isEmpty(headers) || CollectionUtils.isEmpty(table)) {
            return;
        }
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

    public static void download(Map<String, String> fileMap, RestTemplate restTemplate, String outFilePath, String cookie) {
        AtomicInteger num = new AtomicInteger(1);
        fileMap.forEach((fileId, fileName) -> {
            download(fileId, fileName, restTemplate, outFilePath, cookie);
            log.info("下载进度[{}-{}]", num.getAndIncrement(), fileMap.size());
        });
    }

    @SneakyThrows
    public static void download(String fileId, String fileName, RestTemplate restTemplate, String outFilePath, String cookie) {
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        // 文件下载URL
        String url = String.format("https://file.trialos.com.cn/file/getFile?fileId=%s", fileId);
        long start = System.currentTimeMillis();
        ResponseEntity<Resource> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Resource.class);
        log.info("文件:[id:{},fileName:{}]查询耗时:[{}]", fileId, fileName, System.currentTimeMillis() - start);
        String out = String.format("%s\\%s", outFilePath, fileName);
        try (InputStream inputStream = res.getBody().getInputStream();
             OutputStream outputStream = new FileOutputStream(out)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getReportVersion(RestTemplate restTemplate, String cookie) {
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        String reportId = "8a8d801c952c407b0195403233111de9";
        String url = String.format("https://www.trialos.com.cn/api/pvs-report/report-model/findAllVersionsReports?reportId=%s", reportId);
        ResponseEntity<ActionResult> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), ActionResult.class);
        System.out.println();
    }

    public static CompanyInfo getCompanyInfo(RestTemplate restTemplate, String cookie, String tenantId) {
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        String url = String.format("https://www.trialos.com.cn/api/pvs-report/company/findInfomationByTenantId?tenantId=%s", tenantId);
        ResponseEntity<ActionResult<CompanyInfo>> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<ActionResult<CompanyInfo>>() {});
        System.out.println();
        return res.getBody().getData();
    }

    public static void batchExport(RestTemplate restTemplate, String cookie, List<BatchExportInfo> exportInfos) {
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        headers.add("TM-Header-AppId", "pv-esafety");
        String url = "https://www.trialos.com.cn/api/pvs-report/export/batchExportFiles";
        // 入参
        HttpEntity<List<BatchExportInfo>> requestEntity = new HttpEntity<>(exportInfos, headers);
        // 调用
        ResponseEntity<ActionResult<List<ExportRes>>> res = restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ActionResult<List<ExportRes>>>() {});
        System.out.println();
        List<ExportRes> resInfos = res.getBody().getData();
        if (resInfos.size() != exportInfos.size()) {
            log.info("有错误");
            throw new RuntimeException();
        }
        System.out.println();
    }

    public static void fixProdData(RestTemplate restTemplate, String cookie, List<FixData> fixDataInfos) {
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        String url = "https://www.trialos.com.cn/api/pvs-report/schema/fixData";
        // String url = "http://trialos.test.com/api/pvs-report/schema/fixData";
        // 入参
        HttpEntity<List<FixData>> requestEntity = new HttpEntity<>(fixDataInfos, headers);
        // 调用
        ResponseEntity<ActionResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ActionResult.class);
        System.out.println();
    }

    public static void fixHomePageTaskData(QueryDBReq req, RestTemplate restTemplate) {
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 查询首页任务与报告任务不一致的
        Map<String, String> nameSqlMap = Maps.newLinkedHashMap();
        nameSqlMap.put(
                "首页任务与报告任务不一致的",
                "select CONCAT('{\"columnValueMap\": {\"task_status\": \"esafety_task_status_completed\",\"gvp_task_status\": \"finished\"},\"id\": \"', t2.id, '\",\"table\": \"gvp_workbench.task_info\"}') as fixDataInfo from pvs_report_all.task_report_info t1 left join gvp_workbench.task_info t2 on t1.id = t2.unique_key where t1.finish_status = 1 and t2.gvp_task_status != 'finished'"
        );
        nameSqlMap.put(
                "首页任务与递交任务不一致的",
                "select CONCAT('{\"columnValueMap\": {\"task_status\": \"', IF(t1.submit_status = 'b51af9ee-083b-456b-ac7c-1595d3ab2a9e', 'esafety_task_status_completed', 'esafety_task_status_submit_fail'), '\",\"gvp_task_status\":\"', IF(t1.submit_status = 'b51af9ee-083b-456b-ac7c-1595d3ab2a9e', 'finished', 'unfinished'), '\"},\"id\":\"', t2.id, '\",\"table\":\"gvp_workbench.task_info\"}') as fixDataInfo from pvs_report_all.report_submit_task t1 left JOIN gvp_workbench.task_info t2 ON t1.id = t2.unique_key where t1.submit_status in ('b51af9ee-083b-456b-ac7c-1595d3ab2a9e', '3dbf7e2c-62a5-485b-ae74-383e4b479aff') and t2.task_status in ('esafety_task_status_ongoing', 'esafety_task_status_pending')"
        );
        nameSqlMap.forEach((name, sql) -> {
            req.setSqlContent(sql);
            // 查询数据
            D res = getRes(req, restTemplate);
            // 提取数据
            List<FixData> fixDataInfos = extractColumnValues(res, Lists.newArrayList("fixDataInfo")).stream().map(row -> JSON.parseObject(row.get("fixDataInfo"), com.hushunjian.jooq.res.FixData.class)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(fixDataInfos)) {
                fixProdData(restTemplate, req.getEsafety5Cookie(), fixDataInfos);
            }
            log.info("线上数据修复,修复任务:[{}],修复数量:[{}]", name, fixDataInfos.size());
        });
        // 调一次查询接口,续一下token
        getReportVersion(restTemplate, req.getEsafety5Cookie());
    }


    public static List<Map<String, String>> extractColumnValues(D res, List<String> extractColumns) {
        // 列
        List<String> columns = res.getData().getColumn_list();
        // 数据
        List<List<String>> rows = res.getData().getRows();
        // 提取出来的数据
        List<Map<String, String>> rowValues = Lists.newArrayList();
        // 循环所有的行
        rows.forEach(row -> {
            // 列数据
            Map<String, String> rowValueMap = Maps.newLinkedHashMap();
            // 循环获取数据
            extractColumns.forEach(extractColumn ->
                    rowValueMap.put(extractColumn, row.get(columns.indexOf(extractColumn))));
            rowValues.add(rowValueMap);
        });
        return rowValues;
    }
}
