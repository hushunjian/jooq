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
import com.hushunjian.jooq.req.RefreshReportEsReq;
import com.hushunjian.jooq.res.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.excel.EasyExcelFactory.write;
import static com.alibaba.excel.EasyExcelFactory.writerSheet;

@Slf4j
public class QueryDBHelper {

    public static final String DB_COOKIE = "csrftoken=GEt6qytlkvw2GnI8qZFOnF4fB2ot8Ey8; sessionid=w3qp8y3a7yg5t0bd0h71h232vsn73cvc";

    public static final String DB_CSRF_TOKEN = "GEt6qytlkvw2GnI8qZFOnF4fB2ot8Ey8";

    public static final String DOWNLOAD_FILE_COOKIE = "gr_user_id=8065e17c-9fe0-4464-be05-cc8651fce673; b322f7b164f88f10_gr_last_sent_cs1=8a81c08a7965a66b01798247b8c04c33; b322f7b164f88f10_gr_cs1=8a81c08a7965a66b01798247b8c04c33; token=9454171be51b4a31bff9f222b5707c65";

    //public static final String ESAFETY_5_COOKIE = "gr_user_id=8065e17c-9fe0-4464-be05-cc8651fce673; b322f7b164f88f10_gr_last_sent_cs1=8a81c08a7965a66b01798247b8c04c33; b322f7b164f88f10_gr_cs1=8a81c08a7965a66b01798247b8c04c33; Hm_lvt_88897af8840c3689db7cb8c0886b1026=1742551743; acw_tc=0aef82e417472741608807780e0072a6d10e6c312e7a2fa1fb59fafcc48159; token=9b3a912254c04039b92368a5eac0145c";

    public static final String ESAFETY_5_COOKIE = "Hm_lvt_88897af8840c3689db7cb8c0886b1026=1747187528; HMACCOUNT=792765C11516D6DF; gr_user_id=b5b6c2a4-599f-459b-8f19-902a2a857930; b322f7b164f88f10_gr_last_sent_cs1=8a81c08a78b143e60178b4b8263c4c52; b322f7b164f88f10_gr_cs1=8a81c08a78b143e60178b4b8263c4c52; eSafety5_unblind_warn=true; Hm_lpvt_88897af8840c3689db7cb8c0886b1026=1748254162; token=b1df06a968554af69c80739f75228049; acw_tc=1a0c384f17482578596257407e0089ee444f55585f290cce3dfe1f48e27d17";



    public static String MY_STUDY_COOKIE = "gr_user_id=b5b6c2a4-599f-459b-8f19-902a2a857930; b322f7b164f88f10_gr_last_sent_cs1=8a81c08a78b143e60178b4b8263c4c52; Hm_lvt_a6a71cf5f4147c04a5189490b5c31160=1748498485; eSafety5_unblind_warn=true; Hm_lvt_88897af8840c3689db7cb8c0886b1026=1750647416; HMACCOUNT=792765C11516D6DF; Hm_lpvt_88897af8840c3689db7cb8c0886b1026=1750839831; b322f7b164f88f10_gr_cs1=8a81c08a78b143e60178b4b8263c4c52; token=683222815e8548889cecd8b377fa2086; acw_tc=0a47329d17512740126171888e00691446da8cec0c32fa60f936181dcbdaf5";

    public static String MY_STUDY_USER_ID = "8a81c08a78b143e60178b4b826574c55";

    public static String MY_STUDY_COMPANY_ID = "6edc1973f7a24c65a305a6db001f52d0";

    public static String MY_STUDY_PASSWORD = "fbc8d01c49c94499c12a135db2d3feb7";


    private static final List<UserStudyInfo> study_info = Lists.newArrayList();


    static {
        study_info.add(
                UserStudyInfo.builder()
                        .userName("hx")
                        .userId("8a81c08a78b143e60178b4b826574c55")
                        .companyId("6edc1973f7a24c65a305a6db001f52d0")
                        .password("fbc8d01c49c94499c12a135db2d3feb7")
                        .cookie("")
                        .study(false)
                        .build()
        );

        study_info.add(
                UserStudyInfo.builder()
                        .userName("hsj")
                        .userId("8a81c08a7965a66b01798247b8da4c36")
                        .companyId("6edc1973f7a24c65a305a6db001f52d0")
                        .password("e23c31f7c06c8b3d4c3cce2b2e0df709")
                        .cookie("gr_user_id=8065e17c-9fe0-4464-be05-cc8651fce673; b322f7b164f88f10_gr_last_sent_cs1=8a81c08a7965a66b01798247b8c04c33; b322f7b164f88f10_gr_cs1=8a81c08a7965a66b01798247b8c04c33; eSafety5_unblind_warn=true; Hm_lvt_88897af8840c3689db7cb8c0886b1026=1748600945,1750993244; acw_tc=1a0c39dd17512751821453472e0083bd85eb2e84cf06d7bda128b0369ff7f3; token=33c119e4fed74bc2b7fb8c680d5015b8")
                        .study(false)
                        .build()
        );

        study_info.add(
                UserStudyInfo.builder()
                        .userName("mxl")
                        .userId("8a81c08b761d6b3c01766e88c4c16eca")
                        .companyId("6edc1973f7a24c65a305a6db001f52d0")
                        .password("8402965bb392bdab9d96b6428e342103")
                        .cookie("gr_user_id=b490a001-5850-4a2e-9ba4-c0b6fbfc2285; Hm_lvt_88897af8840c3689db7cb8c0886b1026=1745741540,1747290496; 99fe3e6894ec3188_gr_last_sent_cs1=8a8181f07d712e67017d754391ab2002; eSafety5_unblind_warn=true; 99fe3e6894ec3188_gr_cs1=8a8181f07d712e67017d754391ab2002; acw_tc=0a472f8d17512754652872121e0056a247e4acd2ba0c641633b48ac80b4c17; token=4d66b041b47643e4b33bb25c038f0b9e")
                        .study(true)
                        .build()
        );

    }

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

    private static WriteSheet createSheet2(List<String> headers, String sheetName) {
        // 创建sheet,并设置表头样式
        WriteSheet sheet = writerSheet(sheetName).registerWriteHandler(getHandler()).build();
        // 设置表头
        sheet.setHead(headers.stream().map(Lists::newArrayList).collect(Collectors.toList()));
        // 设置限制
        return sheet;
    }

    public static File genExcel(ExcelData excelData, String fileName) {
        // 生成临时文件
        File tempFile = genTempFile(fileName);
        // 创建构建器
        ExcelWriter writer = write(tempFile).build();
        // 循环生成sheet
        excelData.getSheetHeadValues().forEach((sheetName, headers) -> {
            // 创建sheet
            WriteSheet sheet = createSheet2(headers, sheetName);
            // 表格数据
            List<List<String>> table = Lists.newArrayList();
            if (excelData.getSheetRowsMap().containsKey(sheetName)) {
                // 表格数据
                table = excelData.getSheetRowsMap().get(sheetName).stream().map(row -> Lists.newArrayList(row.values())).collect(Collectors.toList());
            }
            // 写入数据
            writer.write(table, sheet);
        });
        // 完成
        writer.finish();
        // 返回临时文件
        return tempFile;
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

    public static void refreshReportEs(RestTemplate restTemplate) {
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", ESAFETY_5_COOKIE);
        String url = "https://www.trialos.com.cn/api/pvs-report-pipeline/indics/refreshReportEs";
        RefreshReportEsReq req = new RefreshReportEsReq();
        // 租户
        // 55791fe0-6347-11e9-b21d-ef616d53ac34 浙江医药股份有限公司
        // ed16ea26-16fd-465f-a94d-adf449fec06e 豪森
        req.setTenantIds(Lists.newArrayList("55791fe0-6347-11e9-b21d-ef616d53ac34", "ed16ea26-16fd-465f-a94d-adf449fec06e"));
        // 只刷新当天的
        req.setRefreshCurrentDate(true);
        // 入参
        HttpEntity<RefreshReportEsReq> requestEntity = new HttpEntity<>(req, headers);
        // 调用
        ResponseEntity<ActionResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ActionResult.class);
        System.out.println();
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

    public static Map<String, String> getDbReportCheckError(RestTemplate restTemplate, String cookie, String reportId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        String url = "https://www.trialos.com.cn/api/pv-web/report/doDbReportCheck";
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(Lists.newArrayList(reportId), headers);
        try {
            ResponseEntity<Map<String, String>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, String>>() {});
            return response.getBody();
        } catch (Exception ex) {
            log.info("报告:[{}]获取失败", reportId);
            Map<String, String> result = Maps.newHashMap();
            result.put(reportId, "查询失败");
            return result;
        }
    }

    public static Map<String, String> getDbReportCheckError(RestTemplate restTemplate, String cookie, List<String> reportIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        String url = "https://www.trialos.com.cn/api/pv-web/report/doDbReportCheck";
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(reportIds, headers);
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, String>>() {});
        return response.getBody();
    }

    public static void finishTask(RestTemplate restTemplate) {
        // 循环用户
        study_info.stream().filter(study -> BooleanUtils.isTrue(study.getStudy())).forEach(studyInfo -> {
            // 根据用户信息重置
            MY_STUDY_COOKIE = studyInfo.getCookie();
            MY_STUDY_USER_ID = studyInfo.getUserId();
            MY_STUDY_COMPANY_ID = studyInfo.getCompanyId();
            MY_STUDY_PASSWORD = studyInfo.getPassword();

            // 内部课程 SourceFrom = MyToDoCourse
            getMyStudyTask(restTemplate, "MyToDoCourse", "");
            // 培训计划 SourceFrom = MyToDoTrainMatrix
            // 先查一次
            getTaskList(restTemplate, "MyToDoTrainMatrix", "").getData().forEach(detail -> {
                // 在查一次
                getMyStudyTask(restTemplate, "MyToDoTrainMatrix", detail.getId());
            });
        });
    }

    private static QueryMyStudyTaskRes getTaskList(RestTemplate restTemplate, String sourceFrom, String trainMatrixId) {
        // 内部课程
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("Name", "");
        parameters.put("CourseCategoryId", "0");
        parameters.put("ChooseCourseTypeId", "0");
        parameters.put("OverdueState", "0");
        parameters.put("CompleteStateId", StringUtils.isBlank(trainMatrixId) ? 60 : 0);
        parameters.put("UserId", MY_STUDY_USER_ID);
        parameters.put("CompanyId", MY_STUDY_COMPANY_ID);
        parameters.put("isLiveCourse", 0);
        parameters.put("TrainMatrixId", trainMatrixId);
        if (StringUtils.isBlank(trainMatrixId)) {
            parameters.put("SourceFrom", sourceFrom);
        }


        QueryMyStudyTask queryMyStudyTask = QueryMyStudyTask.builder()
                .currentPageIndex(0)
                .pageSize(3000)
                .parameters(parameters).build();
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", MY_STUDY_COOKIE);
        String url = "https://www.trialos.com.cn/api/ecollege-service/api/MyTrain/SearchMyTrainList";
        // 入参
        HttpEntity<QueryMyStudyTask> requestEntity = new HttpEntity<>(queryMyStudyTask, headers);
        // 调用
        ResponseEntity<QueryMyStudyTaskRes> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, QueryMyStudyTaskRes.class);
        return response.getBody();
    }

    public static void getMyStudyTask(RestTemplate restTemplate, String sourceFrom, String trainMatrixId) {
        List<QueryMyStudyTaskRes.MyStudyTaskDetail> data1 = getTaskList(restTemplate, sourceFrom, trainMatrixId).getData();
        data1.forEach(taskDetail -> {
            // 如果这个是完成就跳过 20 已完成  30进行中  10 未开始
            if (taskDetail.getCompleteStateId() == 20 || taskDetail.getOverdueState() != null) {
                // 已完成
            } else {
                GetTaskDetailInfoRes res = getTaskDetailInfo(restTemplate, GetTaskDetailInfoReq.builder().courseId(taskDetail.getId()).employeeCoursePlanId(taskDetail.getEmployeeCoursePlanId()).build());
                System.out.println();
                if (res.getData().getCompleteStateId() == 20) {
                    // 已完成的
                } else if (res.getData().getCompleteStateId() == 30 || res.getData().getCompleteStateId() == 10) {
                    // 进行中的
                    // 判断任务
                    GetFileInfoRes studyFileRes = getStudyFiles(restTemplate, taskDetail.getEmployeeCoursePlanId(), "1");
                    if (CollectionUtils.isEmpty(studyFileRes.getData())) {
                        studyFileRes = getStudyFiles(restTemplate, taskDetail.getEmployeeCoursePlanId(), "2");
                    }
                    // 筛选出没有完成最小学时的
                    List<GetFileInfoRes.FileInfo> needLearnFileInfos = studyFileRes.getData().stream().filter(fileInfo -> fileInfo.getLearnedTime() == null || fileInfo.getLearnedTime() < fileInfo.getMinLearningTime()).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(needLearnFileInfos)) {
                        // 所有的都完成了最小学时,就直接验证密码
                        checkTestPassword(restTemplate, taskDetail.getId(), taskDetail.getEmployeeCoursePlanId());
                    } else {
                        // 这些在继续学习
                        needLearnFileInfos.forEach(needLearnFileInfo -> {
                            // 根据最小学习时长,分钟为单位 * 60s 换成秒的
                            int minLearningTime = needLearnFileInfo.getMinLearningTime() * 60;
                            // 每隔30s调一次,除以30
                            int total = (minLearningTime / 30) + 1;
                            for (int i = 0; i < total; i++) {
                                log.info("自动学习:[{}]-[{}]进度:[{}-{}]", taskDetail.getId(), taskDetail.getEmployeeCoursePlanId(), i, total);
                                saveLearnedTime(restTemplate, taskDetail.getId(), taskDetail.getEmployeeCoursePlanId(), needLearnFileInfo.getId());
                                // 休眠一下
                                sleep(5000);
                            }
                        });
                        // 学完在考试
                        doAnswer(restTemplate, taskDetail);
                    }
                } else {
                    log.info("不知道状态的");
                }
            }
        });

    }

    private static void doAnswer(RestTemplate restTemplate, QueryMyStudyTaskRes.MyStudyTaskDetail taskDetail) {
        // 查询问题
        AnswerInfo answerInfo = loadQuestion(restTemplate, taskDetail.getId(), taskDetail.getEmployeeCoursePlanId());
        // 获取结果集
        answerInfo.getData().forEach(answerDetail -> {
            List<String> an = Lists.newArrayList();
            answerDetail.getListQuestionAnswerList().forEach(answerInfo2 -> {
                if (answerInfo2.getIsAnswer() == 1) {
                    System.out.println(answerInfo2.getAnswerChoice());
                    an.add(answerInfo2.getAnswerChoice().split("\\.")[0]);
                }
            });
            String join = String.join("", an);
            answerDetail.setAnswer(join);
            answerDetail.setChoiceAnswer(join);
            System.out.println(join);
        });
        System.out.println();
        // 保存答案
        List<AnswerInfo.AnswerInfo1> data = answerInfo.getData();
        AnswerInfo.SaveDTO save = new AnswerInfo.SaveDTO();
        save.setQuestionDtos(data);
        // 保存
        save(restTemplate, save);

        // 计算分数
        calculateScore(restTemplate, taskDetail.getEmployeeCoursePlanId());
        // 签名
        checkTestPassword(restTemplate, taskDetail.getId(), taskDetail.getEmployeeCoursePlanId());
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    private static void saveLearnedTime(RestTemplate restTemplate, String courseId, String employeeCoursePlanId, String courseResourceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", MY_STUDY_COOKIE);
        String url = "https://www.trialos.com.cn/api/ecollege-service/api/TeamManagement/SaveLearnedTime";
        SaveLearnedTimeReq req = SaveLearnedTimeReq.builder()
                .id(employeeCoursePlanId)
                .employeeCoursePlanId(employeeCoursePlanId)
                .learnedTime(31)
                .courseId(courseId)
                .courseResourceId(courseResourceId)
                .key(String.format("%s-%d-%s-%s", employeeCoursePlanId, 31, courseId, courseResourceId))
                .build();
        // 入参
        HttpEntity<SaveLearnedTimeReq> requestEntity = new HttpEntity<>(req, headers);
        // 调用
        ResponseEntity<CheckTestPasswordRes> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, CheckTestPasswordRes.class);
        System.out.println();
    }

    private static void checkTestPassword(RestTemplate restTemplate, String courseId, String employeeCoursePlanId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", MY_STUDY_COOKIE);
        String url = "https://www.trialos.com.cn/api/ecollege-service/api/MyTrain/CheckTestPassword";
        CheckTestPasswordReq req = CheckTestPasswordReq.builder()
                .courseId(courseId)
                .employeeCoursePlanId(employeeCoursePlanId)
                .password(MY_STUDY_PASSWORD)
                .openType("0")
                .build();
        // 入参
        HttpEntity<CheckTestPasswordReq> requestEntity = new HttpEntity<>(req, headers);
        // 调用
        ResponseEntity<CheckTestPasswordRes> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, CheckTestPasswordRes.class);
        System.out.println();
    }

    private static GetFileInfoRes getStudyFiles(RestTemplate restTemplate, String employeeCoursePlanId, String fileType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", MY_STUDY_COOKIE);
        String url = "https://www.trialos.com.cn/api/ecollege-service/api/CourseResource/QueryCourseResourceByEmployeeCoursePlanId";
        // 入参
        CalculateScoreReq req = CalculateScoreReq.builder().fileType(fileType).employeeCoursePlanId(employeeCoursePlanId).build();
        HttpEntity<CalculateScoreReq> requestEntity = new HttpEntity<>(req, headers);
        // 调用
        ResponseEntity<GetFileInfoRes> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, GetFileInfoRes.class);
        System.out.println();
        return response.getBody();
    }

    private static void calculateScore(RestTemplate restTemplate, String employeeCoursePlanId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", MY_STUDY_COOKIE);
        String url = "https://www.trialos.com.cn/api/ecollege-service/api/MyTrain/CalculateScore";
        // 入参
        CalculateScoreReq req = CalculateScoreReq.builder().testType("1").employeeCoursePlanId(employeeCoursePlanId).build();
        HttpEntity<CalculateScoreReq> requestEntity = new HttpEntity<>(req, headers);
        // 调用
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
        System.out.println();

    }

    private static void save(RestTemplate restTemplate, AnswerInfo.SaveDTO save) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", MY_STUDY_COOKIE);
        String url = "https://www.trialos.com.cn/api/ecollege-service/api/MyTrain/AddEmployeeQuestion";
        // 入参
        HttpEntity<AnswerInfo.SaveDTO> requestEntity = new HttpEntity<>(save, headers);
        // 调用
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
        System.out.println();
    }

    private static AnswerInfo loadQuestion(RestTemplate restTemplate, String courseId, String employeeCoursePlanId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("CourseId", courseId);
        parameters.put("EmployeeId", "");
        parameters.put("EmployeeCoursePlanId", employeeCoursePlanId);
        parameters.put("TestGuid", UUID.randomUUID().toString());
        parameters.put("IsTest", "1");


        QueryMyStudyTask queryMyStudyTask = QueryMyStudyTask.builder()
                .currentPageIndex(0)
                .pageSize(3000)
                .parameters(parameters).build();
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", MY_STUDY_COOKIE);
        String url = "https://www.trialos.com.cn/api/ecollege-service/api/MyTrain/LoadMyQuestions";
        // 入参
        HttpEntity<QueryMyStudyTask> requestEntity = new HttpEntity<>(queryMyStudyTask, headers);
        // 调用
        ResponseEntity<AnswerInfo> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, AnswerInfo.class);
        System.out.println();
        return response.getBody();
    }

    private static GetTaskDetailInfoRes getTaskDetailInfo(RestTemplate restTemplate, GetTaskDetailInfoReq req) {
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", MY_STUDY_COOKIE);
        String url = "https://www.trialos.com.cn/api/ecollege-service/api/MyTrain/QueryMyTrainInfo";
        // 入参
        HttpEntity<GetTaskDetailInfoReq> requestEntity = new HttpEntity<>(req, headers);
        // 调用
        ResponseEntity<GetTaskDetailInfoRes> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, GetTaskDetailInfoRes.class);
        return response.getBody();
    }


    public static void autoMeddra(RestTemplate restTemplate, String cookie, List<String> reportIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        reportIds.forEach(reportId -> {
            log.info("报告:[{}]自动编码", reportId);
            String url = "https://www.trialos.com.cn/api/pvs-report/v4sync/meddra?tenantId=0d95f9126da949449318a7af00a49071&appId=pv-esafety&pageSize=2000&reportId=" + reportId;
            // 调用
            ResponseEntity<ActionResult> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), ActionResult.class);
        });
    }

    public static void fixHomePageTaskData(QueryDBReq req, RestTemplate restTemplate) {
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 查询首页任务与报告任务不一致的
        Map<String, String> nameSqlMap = Maps.newLinkedHashMap();
        nameSqlMap.put(
                "首页任务与报告任务不一致的",
                "select CONCAT('{\"columnValueMap\": {\"task_status\": \"esafety_task_status_completed\",\"gvp_task_status\": \"finished\"},\"id\": \"', t2.id, '\",\"table\": \"gvp_workbench.task_info\"}') as fixDataInfo from pvs_report_all.task_report_info t1, gvp_workbench.task_info t2  where t1.id = t2.unique_key and t1.finish_status = 1 and t2.gvp_task_status != 'finished'"
        );
        nameSqlMap.put(
                "首页任务与递交任务不一致的",
                "select CONCAT('{\"columnValueMap\": {\"task_status\": \"', IF(t1.submit_status = 'b51af9ee-083b-456b-ac7c-1595d3ab2a9e', 'esafety_task_status_completed', 'esafety_task_status_submit_fail'), '\",\"gvp_task_status\":\"', IF(t1.submit_status = 'b51af9ee-083b-456b-ac7c-1595d3ab2a9e', 'finished', 'unfinished'), '\"},\"id\":\"', t2.id, '\",\"table\":\"gvp_workbench.task_info\"}') as fixDataInfo from pvs_report_all.report_submit_task t1, gvp_workbench.task_info t2  where t1.id = t2.unique_key and t1.submit_status in ('b51af9ee-083b-456b-ac7c-1595d3ab2a9e', '3dbf7e2c-62a5-485b-ae74-383e4b479aff') and t2.task_status in ('esafety_task_status_ongoing', 'esafety_task_status_pending')"
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
        return extractColumnValues(res.getData().getColumn_list(), res.getData().getRows(), extractColumns);
    }

    public static List<Map<String, String>> extractColumnValues(List<String> columns, List<List<String>> rows, List<String> extractColumns) {
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
