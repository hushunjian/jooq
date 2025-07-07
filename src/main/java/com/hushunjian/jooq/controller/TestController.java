package com.hushunjian.jooq.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hushunjian.jooq.dao.SchemaDao;
import com.hushunjian.jooq.dao.XianShenReportNoDao;
import com.hushunjian.jooq.generator.tables.records.SchemaFieldRecord;
import com.hushunjian.jooq.generator.tables.records.SchemaModuleRecord;
import com.hushunjian.jooq.generator.tables.records.XianShenReportNoRecord;
import com.hushunjian.jooq.helper.ExcelAnalysisModel;
import com.hushunjian.jooq.helper.ExcelData;
import com.hushunjian.jooq.helper.ExcelDataHelper;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.ConfigMapReq;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.req.QueryLogEventReq;
import com.hushunjian.jooq.res.*;
import com.hushunjian.jooq.service.fix.XianShenBase;
import io.swagger.annotations.ApiOperation;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.SpreadsheetVersion;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequestMapping("test")
@RestController(value = "test")
public class TestController {

    @Resource
    private List<XianShenBase> xianShenBases;

    @Resource
    private XianShenReportNoDao xianShenReportNoDao;

    @Resource
    private SchemaDao schemaDao;

    @Resource
    private RestTemplate restTemplate;

    private static final List<String> v4_services = Lists.newArrayList("esae-template-service");

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2;

    private static final List<String> V5_SERVICES = Lists.newArrayList(
            "caps-web",
            "caps-operation-web",

            "gvp-api",
            "gvp-workbench-web",

            "esafety-web",
            "esafety-service",

            "esafety5-e2b",

            "pvs-submit-service",
            "pvs-submit-web",

            "pvs-report",
            "pvs-report-pipeline",

            "pvs-convert-web",
            "pvs-convert-service",

            "pvs-middle-data-api",
            "pvs-middle-auth",

            "pvs-migration-web",

            "pvs-track-service",

            "pvs-common-service"
    );

    /**
     * 全部替换的map
     */
    private static final Map<String, String> REPLACE_VALUE_MAP = Maps.newHashMap();

    private static final Map<String, Pair<String, String>> V5_REPLACE_PART_VALUE_MAP = Maps.newHashMap();

    private static final Map<String, Pair<String, String>> V4_REPLACE_PART_VALUE_MAP = Maps.newHashMap();

    private static final Map<String, Map<String, Pair<String, String>>> V4_V5_REPLACE_PART_VALUE_MAP = Maps.newHashMap();

    private static final List<String> ignoreKeys = Lists.newArrayList();
    static {
        // 全部替换的
        REPLACE_VALUE_MAP.put("spring.redis.host", "spring.redis.host=192.168.132.57");
        REPLACE_VALUE_MAP.put("spring.redis.password", "spring.redis.password=redis123");
        REPLACE_VALUE_MAP.put("spring.data.mongodb.uri", "spring.data.mongodb.uri=mongodb://admin:Taimei@123.@192.168.132.57:27017/pv?replicaSet=prod_pv_rs&authSource=admin");
        REPLACE_VALUE_MAP.put("spring.rabbitmq.host", "spring.rabbitmq.host=192.168.132.55");
        REPLACE_VALUE_MAP.put("spring.rabbitmq.username", "spring.rabbitmq.username=admin");
        REPLACE_VALUE_MAP.put("spring.rabbitmq.password", "spring.rabbitmq.password=Taimei@123.");
        REPLACE_VALUE_MAP.put("spring.elasticsearch.rest.uris", "spring.elasticsearch.rest.uris=192.168.132.55:9200,192.168.132.56:9200,192.168.132.57:9200");
        REPLACE_VALUE_MAP.put("spring.elasticsearch.rest.username", "spring.elasticsearch.rest.username=admin");
        REPLACE_VALUE_MAP.put("spring.elasticsearch.rest.password", "spring.elasticsearch.rest.password=FBzn39tOUaiZLfGb");
        REPLACE_VALUE_MAP.put("com.taimeitech.framework.zkAddress", "com.taimeitech.framework.zkAddress=192.168.132.57:2181,192.168.132.55:2181,192.168.132.56:2181");
        REPLACE_VALUE_MAP.put("spring.kafka.bootstrap-servers", "spring.kafka.bootstrap-servers=192.168.132.57:9092,192.168.132.56:9092,192.168.132.55:9092");
        REPLACE_VALUE_MAP.put("spring.datasource.username", "spring.datasource.username=root");
        REPLACE_VALUE_MAP.put("spring.datasource.password", "spring.datasource.password=taimei@123");

        // V5部分替换的/
        V5_REPLACE_PART_VALUE_MAP.put("spring.datasource.url", Pair.of("prod-mysql-pv-caps-dmp.taimei.com:3310", "192.168.132.62:3307"));
        // V4部分替换的
        V4_REPLACE_PART_VALUE_MAP.put("spring.datasource.url", Pair.of("prod-mysql-pv-dmp.taimei.com:3310", "192.168.132.61:3307"));

        V4_V5_REPLACE_PART_VALUE_MAP.put("v4", V4_REPLACE_PART_VALUE_MAP);
        V4_V5_REPLACE_PART_VALUE_MAP.put("v5", V5_REPLACE_PART_VALUE_MAP);


        ignoreKeys.addAll(REPLACE_VALUE_MAP.keySet());
        ignoreKeys.add("service.name.sso-web");
        ignoreKeys.add("com.taimeitech.framework.i18n-service-name");
        ignoreKeys.add("spring.datasource.url");
        ignoreKeys.add("com.taimeitech.service.im");
        ignoreKeys.add("com.taimeitech.service.omp");
        ignoreKeys.add("service.name.ssp-mcs");
        ignoreKeys.add("com.taimeitech.service.csp");
        ignoreKeys.add("com.taimeitech.service.fs");
        ignoreKeys.add("service.name.job-service");
        ignoreKeys.add("service.name.zhiyi-app");
        ignoreKeys.add("com.taimeitech.framework.hsm.enabled");
        ignoreKeys.add("private.deployment.tenantIds");
        ignoreKeys.add("zhiyi.app.secret");
        ignoreKeys.add("openApi.host.url");
        ignoreKeys.add("zhiyi.app.key");
        ignoreKeys.add("es.config.apps.esafety.address");
        ignoreKeys.add("pvs.new.version.create.report.tenant.split");
        ignoreKeys.add("es.config.apps.esafety.password");
        ignoreKeys.add("es.config.apps.esafety.username");
        ignoreKeys.add("pvs.create.report.tenant.split");
        ignoreKeys.add("com.taimeitech.service.econgif.handle");
        ignoreKeys.add("com.taimeitech.service.omp-service");
        ignoreKeys.add("service.name.fs-service");
        ignoreKeys.add("service.name.bigdata.datart");
        ignoreKeys.add("taimeitech.service.ssp-mcs");
        ignoreKeys.add("iMensa.orgId");
        ignoreKeys.add("signal.bigdata.host.url");
        ignoreKeys.add("service.name.csp-service");
        ignoreKeys.add("service.name.trailer-web");
        ignoreKeys.add("service.name.omp-service");
    }

    private List<String> readTxtLine(String filePath) {
        List<String> lines = Lists.newArrayList();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                System.out.println("第 " + lineNumber + " 行: " + line);
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return lines;
    }

    @SneakyThrows
    @ApiOperation("genFixSql")
    @PostMapping(value = "genFixSql")
    public void genFixSql() {
        // 读取excel
        ExcelData excelData = readExcelData("C:\\Users\\shunjian.hu\\Desktop", "report_value.xlsx");
        //
        List<String> fixSql = Lists.newArrayList();
        excelData.getSheetRowsMap().get("Sheet1").forEach(row -> {
            String id = row.get("id");
            String aeCountry = row.get("AECountry");
            if (StringUtils.equals(aeCountry, "中国")) {
                fixSql.add(String.format("update report_value set AECountry = 'COUNTRY_CN' where id = '%s';", id));
            }
        });
        //
        exportFixSQLFile(fixSql);
    }

    @SneakyThrows
    @ApiOperation("queryErrorDB")
    @PostMapping(value = "queryErrorDB")
    public void queryErrorDB(@RequestBody QueryDBReq req) {
        List<String> reportIds = Lists.newArrayList(
                "8a8d800d9729399301973544df5a786b",
                "8a8d800d9729399301974493d9882770",
                "8a8d800d97293993019752a0a6276c54",
                "8a8d800d9729399301975daac1e60b79",
                "8a8d800d97293993019771c7d5692fad",
                "8a8d800d97293993019777be305b3b2e",
                "8a8d800d9729399301977e87b09b6104",
                "8a8d800d97293993019782ab0fe902db",
                "8a8d800d97293993019786c1c0f8092a",
                "8a8d800d9729399301978dc19b857a0c",
                "8a8d818296d3a2700196ecbfaffc45cd",
                "8a8d818296d3a2700196ecf8ab234686",
                "8a8d818296d3a2700196ed0810ea03c9",
                "8a8d818296d3a2700196ed82b42a6b32",
                "8a8d818296d3a2700196f0c8214762ad",
                "8a8d818296d3a2700196f10e2c5f11d9",
                "8a8d818296d3a2700196f2b76b670930",
                "8a8d818296d3a2700196f7b1cabd6d86",
                "8a8d818296d3a2700196f81479711257",
                "8a8d818296d3a2700196f84221b624b4",
                "8a8d818296d3a27001970f241fe76cec",
                "8a8d818296d3a27001970f272b5875de",
                "8a8d818296d3a27001970f2a745e0177",
                "8a8d818296d3a27001970f5bf5a86764",
                "8a8d818296d3a27001971b0c96de56ac",
                "8a8d818296d3a27001971e8bcc0f34dd",
                "8a8d818296d3a270019738886caf37a9",
                "8a8d818296d3a27001973dacfea31b37",
                "8a8d818296d3a270019743377507707b",
                "8a8d818296d3a270019753197efc53ac",
                "8a8d818296d3a27001975784219345f1",
                "8a8d818296d3a270019757a6775520b9",
                "8a8d818296d3a27001975d78f60964a6",
                "8a8d818296d3a27001975eba50d8624a",
                "8a8d818296d3a270019764205b90472e",
                "8a8d81fd96d39f860196ecc558427908",
                "8a8d81fd96d39f860196f12d07655f65",
                "8a8d81fd96d39f860196f5962a157f73",
                "8a8d81fd96d39f860196fac2be0e1911",
                "8a8d81fd96d39f860196fc2680d142bf",
                "8a8d81fd96d39f86019709ec82a21786",
                "8a8d81fd96d39f8601972e6903475b52",
                "8a8d81fd96d39f860197388cb107657f",
                "8a8d81fd96d39f86019738a07bb26d6f",
                "8a8d81fd96d39f8601973dde37eb64b1",
                "8a8d81fd96d39f8601975407d847777c",
                "8a8d82f795b3e558019719ff86183a21",
                "8a8dbf2a9729365101972974eb6a4012",
                "8a8dbf2a9729365101973499165333e8",
                "8a8dbf2a9729365101973a5909b72c13",
                "8a8dbf2a9729365101973de183db22f1",
                "8a8dbf2a972936510197434868e86b19",
                "8a8dbf2a972936510197452612833d9f",
                "8a8dbf2a972936510197549e643c583a",
                "8a8dbf2a97293651019768966906705e",
                "8a8dbf2a9729365101977bb0fff51756",
                "8a8dbf2a972936510197887537824837",
                "8a8dbf2a9729365101978cd5229049b3",
                "8a8dbf2a972936510197913588403ead"
        );
        // 读excel
        ExcelData excelData = readExcelData("C:\\Users\\shunjian.hu\\Desktop", "错误字段信息2.xlsx");
        // 表列
        Map<String, Set<String>> tableColumnsMap = Maps.newHashMap();
        // 循环行处理
        excelData.getSheetRowsMap().get("Sheet1").forEach(row -> {
            String table = row.get("表");
            String column = row.get("列");
            tableColumnsMap.computeIfAbsent(table, v -> Sets.newHashSet()).add(column);
        });
        // 循环查表
        tableColumnsMap.forEach((table, columns) -> {
            StringBuilder builder = new StringBuilder(String.format("select id, %s from %s where ", String.join(",", columns), table));
            switch (table) {
                case "report_value":
                    builder.append(" id in ").append(String.format("('%s');", String.join("','", reportIds)));
                    break;
                case "tm_othermedicalhistory":
                case "tm_labdata":
                case "tm_drug":
                case "tm_adverseevent" :
                case "tm_reporter" :
                    builder.append(" ReportId in ").append(String.format("('%s');", String.join("','", reportIds)));
                    break;
                case "tm_drug_dose":
                    builder.append(" drug_id in ").append(String.format("(select id from tm_drug where ReportId in ('%s'));", String.join("','", reportIds)));
                    break;
                default:
                    throw new RuntimeException();
            }
            // 查询sql
            String sql = builder.toString();
            System.out.println(sql);
            req.setSqlContent(sql);
            req.setLimitNum("0");
            req.setInstanceName("prod-mysql-pv-ro");
            req.setDbName("pv");
            req.setCookie(QueryDBHelper.DB_COOKIE);
            req.setCsrfToken(QueryDBHelper.DB_CSRF_TOKEN);
            // 查询数据
            D res = QueryDBHelper.getRes(req, restTemplate);
            // QueryDBHelper.exportExcel(res.getData(), table);
        });
        // 查询meddra
        String sql = String.format("select * from meddrafidldinfo where  ReportId in ('%s') and llt_name_en is null and is_deleted = 0;", String.join("','", reportIds));
        req.setSqlContent(sql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        QueryDBHelper.exportExcel(res.getData(), "meddra");
    }

    @ApiOperation("getMeddra")
    @PostMapping(value = "getMeddra")
    public void getMeddra(@RequestBody ActionResult<List<MeddraInfo>> input) {
        List<MeddraInfo> meddraInfos = input.getData();
        List<String> collect = meddraInfos.get(1).getChilds().stream().map(MeddraChildren::getName).collect(Collectors.toList());
        System.out.println();
    }

    @SneakyThrows
    @ApiOperation("readTxt")
    @PostMapping(value = "readTxt")
    public void readTxt() {
        List<String> paths = Lists.newArrayList("C:\\Users\\shunjian.hu\\Desktop\\下载文件\\20250625144145.log", "C:\\Users\\shunjian.hu\\Desktop\\下载文件\\20250625144023.log");
        Pattern pattern = Pattern.compile("字段:\\[([^\\]]+)\\]");
        List<String> headers = Lists.newArrayList("字段", "错误信息", "字段描述", "表", "列");
        List<List<String>> table = Lists.newArrayList();
        // 读配置
        ReportCheckConfigDTO reportCheckConfig = readConfig();
        // 读字段
        Map<String, FieldChekConfigDTO> dataPathConfigMap = reportCheckConfig.getFieldChekConfigs().stream().collect(Collectors.toMap(FieldChekConfigDTO::getDataPath, config -> config));

        paths.forEach(path -> {
            log.info("开始处理文件:[{}]", path);
            List<String> inputs = readTxtLine(path);
            inputs.forEach(input -> {
                String[] split = input.split("tm-header-tenantid");
                for (String s : split) {
                    String[] error = s.split(" : ");
                    if (error.length > 2) {
                        String errorInfo = error[1];
                        if (StringUtils.contains(errorInfo, "控件") && StringUtils.containsAny(errorInfo, "下拉框", "单选", "多选", "下拉选项")) {
                            String group = findGroup(pattern, errorInfo);
                            System.out.println(group);
                            System.out.println(errorInfo);
                            // 获取字段信息
                            FieldChekConfigDTO config = dataPathConfigMap.get(group);
                            table.add(Lists.newArrayList(group, errorInfo, config.getDescribe(), config.getTable(), config.getColumn()));
                        }
                    }
                }
            });
        });
        QueryDBHelper.exportExcel2(headers, table, "错误字段信息2");


/*        List<String> inputs = readTxtLine("C:\\Users\\shunjian.hu\\Desktop\\入参文件.txt");
        // 替换为你自己的文件路径
        List<V4V5FieldConfig> v4V5FieldConfigs = readTxtLine("C:\\Users\\shunjian.hu\\Desktop\\映射文件.txt").stream().map(line -> {
            String[] split = line.split("\\|");
            return V4V5FieldConfig.builder()
                    .v4FieldPath(split[1])
                    .v4FieldName(split[2])
                    .v4ItemClassId(split[3])
                    .v5FieldPath(split[4])
                    .v5FieldName(split[5])
                    .v5ItemClassId(split[6])
                    .v5ModuleName(split[7])
                    .build();
        }).collect(Collectors.toList());
        System.out.println();
        String[] searchList = new String[v4V5FieldConfigs.size()];
        String[] replacementList = new String[v4V5FieldConfigs.size()];
        // 按照v4FieldPath toMap
        for (int i = 0; i < v4V5FieldConfigs.size(); i++) {
            searchList[i] = "chReport." + v4V5FieldConfigs.get(i).getV4FieldPath();
            replacementList[i] = "report." + v4V5FieldConfigs.get(i).getV5FieldPath();
        }
        // 替换字符串
        inputs.forEach(input -> {
            String output = StringUtils.replaceEach(input, searchList, replacementList);
            System.out.println(output);
        });*/
    }

    @ApiOperation("prodSenderSearch")
    @PostMapping(value = "prodSenderSearch")
    public Map<String, Map<String, List<String>>> prodSenderSearch(@RequestBody QueryDBReq req) {
        req.setCookie(QueryDBHelper.DB_COOKIE);
        req.setCsrfToken(QueryDBHelper.DB_CSRF_TOKEN);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 查询线上发送者字段
        String querySendFieldSql = "select id from report_field where table_name = 'sender'";
        req.setSqlContent(querySendFieldSql);
        // 查询数据
        List<String> senderFieldIds = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id")).stream().map(row -> row.get("id")).collect(Collectors.toList());
        // 查询租户id
        String queryTenantIdSql = "select distinct tenant_id from schema_field ";
        req.setSqlContent(queryTenantIdSql);
        List<String> tenantIds = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("tenant_id")).stream().map(row -> row.get("tenant_id")).collect(Collectors.toList());
        Map<String, Map<String, List<String>>> result = Maps.newHashMap();
        // 循环租户查询
        tenantIds.forEach(tenantId -> {
            // 查询这个租户下的发送者信息字段
            String queryTenantSchemaFieldIdSql = String.format("select id from schema_field where tenant_id = '%s' and report_field_id in ('%s') and is_deleted = 0;", tenantId, String.join("','", senderFieldIds));
            req.setSqlContent(queryTenantSchemaFieldIdSql);
            List<String> tenantSchemaFieldIds = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id")).stream().map(row -> row.get("id")).collect(Collectors.toList());
            // 循环租户字段配置
            tenantSchemaFieldIds.forEach(tenantSchemaFieldId -> {
                // 检索条件中有发送者信息的
                String searchQuerySql = String.format("select id from report_common_query where query_condition like '%s' and is_deleted = 0", ("%" + tenantSchemaFieldId + "%"));
                req.setSqlContent(searchQuerySql);
                List<String> searchQueryIds = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id")).stream().map(row -> row.get("id")).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(searchQueryIds)) {
                    log.info("租户:[{}]高级检索条件中有发送者信息", tenantId);
                    result.computeIfAbsent(tenantId, v -> Maps.newHashMap()).computeIfAbsent("高级检索条件中有发送者信息", v -> Lists.newArrayList()).addAll(searchQueryIds);
                }
            });
            // 字段质疑中有发送者信息的
            String queryOppugnSql = String.format("select id from oppugn_detail where schema_field_id in ('%s') and is_deleted = 0;", String.join("','", tenantSchemaFieldIds));
            req.setSqlContent(queryOppugnSql);
            List<String> oppugnDetailIds = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id")).stream().map(row -> row.get("id")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(oppugnDetailIds)) {
                log.info("租户:[{}]字段质疑中有发送者信息", tenantId);
                result.computeIfAbsent(tenantId, v -> Maps.newHashMap()).computeIfAbsent("字段质疑中有发送者信息", v -> Lists.newArrayList()).addAll(oppugnDetailIds);
            }
            // 质控规则里有发送者信息的
            String queryQcConfigFieldSql = String.format("select id from qc_config_field where schema_field_id in ('%s') and is_deleted = 0;", String.join("','", tenantSchemaFieldIds));
            req.setSqlContent(queryQcConfigFieldSql);
            List<String> qcConfigFieldIds = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id")).stream().map(row -> row.get("id")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(qcConfigFieldIds)) {
                log.info("租户:[{}]质控规则配置中有发送者信息", tenantId);
                result.computeIfAbsent(tenantId, v -> Maps.newHashMap()).computeIfAbsent("质控规则配置中有发送者信息", v -> Lists.newArrayList()).addAll(qcConfigFieldIds);
            }
            // 行列表配置里有发送者信息的
            String queryLineListingFieldSql = String.format("select id from line_listing_template_field where tenant_id = '%s' and field_unique_code like 'sender%s' and is_deleted = 0", tenantId, "%");
            req.setSqlContent(queryLineListingFieldSql);
            List<String> lineListingFieldIds = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id")).stream().map(row -> row.get("id")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(lineListingFieldIds)) {
                log.info("租户:[{}]行列表字段配置中有发送者信息", tenantId);
                result.computeIfAbsent(tenantId, v -> Maps.newHashMap()).computeIfAbsent("行列表字段配置中有发送者信息", v -> Lists.newArrayList()).addAll(lineListingFieldIds);
            }
        });
        System.out.println(JSON.toJSONString(result.keySet()));
        System.out.println(JSON.toJSONString(result));
        System.out.println("33333");
        return result;
    }







    @ApiOperation("mergeReplaceInfo")
    @GetMapping(value = "mergeReplaceInfo")
    public void mergeReplaceInfo() {
        // 指定bat文件所在的文件夹路径
        String dirPath = "C:\\Users\\shunjian.hu\\Desktop\\下载文件\\bat";

        try (Stream<Path> paths = Files.walk(Paths.get(dirPath))) {
            // 过滤出所有的.bat文件
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".bat"))
                    .forEach(TestController::mergeReplaceInfo);
        } catch (IOException e) {
            System.err.println("Error accessing directory: " + e.getMessage());
        }
    }

    private static final Map<String, Map<String, String>> QI_LU_REPLACE_INFO_MAP = Maps.newHashMap();


    static {
        Map<String, String> TEST_INFO_MAP = Maps.newHashMap();
        TEST_INFO_MAP.put("mergeFolder", "E:\\db_merge_test");
        TEST_INFO_MAP.put("dbURL", "192.168.132.35");
        TEST_INFO_MAP.put("dbPort", "3307");
        TEST_INFO_MAP.put("dbUserName", "root");
        QI_LU_REPLACE_INFO_MAP.put("qilubat_test", TEST_INFO_MAP);


        Map<String, String> PROD_INFO_MAP = Maps.newHashMap();
        PROD_INFO_MAP.put("mergeFolder", "E:\\db_merge_prod");
        PROD_INFO_MAP.put("dbURL", "192.168.132.62");
        PROD_INFO_MAP.put("dbPort", "3307");
        PROD_INFO_MAP.put("dbUserName", "root");
        QI_LU_REPLACE_INFO_MAP.put("qilubat_prod", PROD_INFO_MAP);
    }

    private static void mergeReplaceInfo(Path filePath) {
        QI_LU_REPLACE_INFO_MAP.forEach((folderName, infoMap) -> {
            File inputFile = filePath.toFile();
            // 根据输入文件名创建输出文件，但位于不同的目录
            File outputFile = new File("C:\\Users\\shunjian.hu\\Desktop\\下载文件\\" + folderName, inputFile.getName());
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    // 替换需要更改的部分
                    String newLine = currentLine.replace("D:\\db_merge_prod", infoMap.get("mergeFolder"))
                            .replace("prod-polarm-pvcaps001-sh2e.taimei.com", infoMap.get("dbURL"))
                            .replace("capsuser", infoMap.get("dbUserName"))
                            .replace("3306", infoMap.get("dbPort"));
                    writer.write(newLine);
                    writer.newLine(); // 写入新行符

                    // 显式刷新缓冲区，确保数据被写入
                    writer.flush();
                }

            } catch (IOException e) {
                System.err.println("Error processing file " + filePath + ": " + e.getMessage());
                e.printStackTrace(); // 打印堆栈跟踪以帮助诊断问题
            }

            if (!outputFile.exists() || outputFile.length() == 0) {
                System.err.println("Output file is empty or was not created: " + outputFile.getAbsolutePath());
            } else {
                System.out.println("Processed file saved as: " + outputFile.getAbsolutePath());
            }
        });
    }

    @ApiOperation("mergeSql")
    @GetMapping(value = "mergeSql")
    public void mergeSql() {
        // 设置源目录路径
        String sourceDirPath = "C:\\Users\\shunjian.hu\\Desktop\\flyway"; // 替换为你的flyway目录路径

        File sourceDir = new File(sourceDirPath);

        if (sourceDir.isDirectory()) {
            try {
                Files.walk(Paths.get(sourceDirPath))
                        .filter(Files::isDirectory)
                        .forEach(TestController::mergeSqlFilesInDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Source path is not a directory.");
        }

        System.out.println("SQL files merged successfully within each subdirectory.");
    }

    private static void mergeSqlFilesInDir(Path dir) {
        try {
            List<File> sqlFiles = new ArrayList<>();
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".sql"))
                    .forEach(p -> sqlFiles.add(p.toFile()));

            if (!sqlFiles.isEmpty()) { // 只有当存在.sql文件时才创建merge.sql
                File mergeFile = new File(dir.toFile(), "merge.sql");
                try (PrintWriter writer = new PrintWriter(new FileWriter(mergeFile))) {
                    for (File sqlFile : sqlFiles) {
                        List<String> lines = Files.readAllLines(sqlFile.toPath(), StandardCharsets.UTF_8);

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);

                            // 判断是否是最后一行 && 不为空 && 没有以分号结尾
                            if (i == lines.size() - 1 && !line.trim().isEmpty() && !line.trim().endsWith(";")) {
                                log.info("文件名:{}", sqlFile.getName());
                                writer.println(line + ";"); // 在原行末尾添加分号
                            } else {
                                writer.println(line);
                            }
                        }
                    }
                }
                System.out.println("Merged SQL files into: " + mergeFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation("readConfigMapFile")
    @GetMapping(value = "readConfigMapFile")
    public void readConfigMapFile(@RequestParam String filePath) {
        List<String> projects = Lists.newArrayList();
        //projects.add("D:\\project\\service\\pvs-report\\pvs-report-pipeline\\config");
        //projects.add("D:\\project\\service\\pvs-report\\pvs-report-service\\config");
        //projects.add("D:\\project\\service\\pvs-report\\pvs-report-convert\\config");
        projects.add(filePath);
        projects.forEach(project -> {
            log.info("项目:[{}]对比=============================开始", project);
            // 读取prod配置信息
            Properties prodProperties = readProperties(String.format("%s\\%s", project, "prod.properties"));
            // 读取qilu配置信息
            Properties qiluProperties = readProperties(String.format("%s\\%s", project, "qilu-local.properties"));
            // 比对线上有的,本地没有的
            log.info("比对线上有的,本地没有的============================开始");
            compare(prodProperties, qiluProperties);
            log.info("比对线上有的,本地没有的============================结束");
            for (int i = 0; i < 10; i++) {
                System.out.println();
            }
            log.info("比对线上没有的,本地有的============================开始");
            // 比对线上没有的,本地有的
            compare(qiluProperties, prodProperties);
            log.info("比对线上没有的,本地有的============================结束");
            for (int i = 0; i < 10; i++) {
                System.out.println();
            }
            // 比对线上跟本地一致的
            compareEquals(prodProperties, qiluProperties);
            log.info("项目:[{}]对比=============================结束", project);
            for (int i = 0; i < 10; i++) {
                System.out.println();
            }
        });
    }

    private void compareEquals(Properties source, Properties target) {
        source.stringPropertyNames().forEach(key -> {
            // source获取key
            String sourceProperty = source.getProperty(key);
            // target获取key
            String targetProperty = target.getProperty(key);
            if (StringUtils.equals(sourceProperty, targetProperty)) {
                log.info("齐鲁与线上一致的配置key:[{}], value值是:[{}]", key, sourceProperty);
            }
        });
    }


    private void compare(Properties source, Properties target) {
        source.stringPropertyNames().forEach(key -> {
            // source获取key
            String sourceProperty = source.getProperty(key);
            // target获取key
            String targetProperty = target.getProperty(key);
            if (StringUtils.isBlank(targetProperty)) {
                log.info("缺失==key:[{}]", key);
            }
            if (!StringUtils.equals(sourceProperty, targetProperty) && !ignoreKeys.contains(key) && !key.startsWith("spring.datasource.dynamic.datasource") && !StringUtils.contains(key, "dbv4evigi") && !StringUtils.contains(key, ".hsm.")) {
                log.info("值存在差异key:[{}],source:[{}], target:[{}]", key, sourceProperty, targetProperty);
            }
        });
    }

    private Properties readProperties(String path) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            return props;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @ApiOperation("反馈数据错误")
    @PostMapping(value = "errorFeedback")
    public void errorFeedback(@RequestBody QueryLogEventReq req) {
        LogEventRes res = QueryDBHelper.getRes(req, restTemplate);
        // id匹配
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
        // 获取信息
        List<String> messages = res.getQuery().stream().map(info -> info.getInfo().getMessage()).collect(Collectors.toList());
        List<String> ids = messages.stream().map(message -> findGroup(idPattern, message)).collect(Collectors.toList());
        System.out.println();
    }


    @ApiOperation("查询日志")
    @PostMapping(value = "queryLogEvent")
    public void queryLogEvent(@RequestBody QueryLogEventReq req) {
        LogEventRes res = QueryDBHelper.getRes(req, restTemplate);
        // 获取信息
        List<String> messages = res.getQuery().stream().map(info -> info.getInfo().getMessage()).collect(Collectors.toList());
        Pattern pattern = Pattern.compile("字段:\\$([^$$]+)\\$\\s*数据");
        Set<String> errorFieldKeys = Sets.newHashSet();
        messages.forEach(message -> {
            String fieldKey = findGroup(pattern, message);
            errorFieldKeys.add(fieldKey);
        });
        System.out.println();
/*        // 端口id匹配
        Pattern portIdPattern = Pattern.compile("\"portId\"\\s*:\\s*\"([^\"]+)\"");
        // 时间匹配
        Pattern dateTimePattern = Pattern.compile("\"dateTime\"\\s*:\\s*(\\d+)");
        // 文件名匹配
        Pattern fullNamePattern = Pattern.compile("\"fullName\"\\s*:\\s*\"([^\"]+)\"");
        // 提取信息
        List<PortFileRes> portFiles = Lists.newArrayList();
        messages.forEach(message -> {
            // 时间
            String dateTime = DateFormatUtils.format(new Date(Long.valueOf(findGroup(dateTimePattern, message))), "yyyy-MM-dd HH:mm:ss");
            // 端口id
            String portId = findGroup(portIdPattern, message);
            // 文件名
            String fileName = findGroup(fullNamePattern, message);
            System.out.println();
            portFiles.add(PortFileRes.builder().portId(portId).fileName(fileName).dateTime(dateTime).build());
        });
        System.out.println();
        // 按照端口,文件名,时间排序
        portFiles.sort(Comparator.comparing(PortFileRes::getPortId).thenComparing(PortFileRes::getFileName).thenComparing(PortFileRes::getDateTime));
        // 查询端口数据
        Map<String, String> portMap = getPortMap(req.getQueryDBReq(), portFiles.stream().map(PortFileRes::getPortId).distinct().collect(Collectors.toList()));
        // 数据
        System.out.println();
        // 表头
        List<List<String>> headers = Lists.newArrayList("端口名称", "文件名", "接收时间").stream().map(Lists::newArrayList).collect(Collectors.toList());
        // 表格数据
        List<List<String>> table = portFiles.stream().map(portFile -> Lists.newArrayList(portMap.get(portFile.getPortId()), portFile.getFileName(), portFile.getDateTime())).collect(Collectors.toList());
        // 导出excel
        QueryDBHelper.exportExcel(headers, table, "接收到的重复文件");*/
    }


    private Map<String, String> getPortMap(QueryDBReq req, List<String> portIds) {
        // 查询SQL
        req.setLimitNum("0");
        String sql = String.format("select id, port_id from edi_port where id in (\"%s\")", String.join("\", \"", portIds));
        req.setSqlContent(sql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        // 提取数据
        List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(res, Lists.newArrayList("id", "port_id"));
        // 返回数据
        Map<String, String> result = Maps.newHashMap();
        rows.forEach(row -> result.put(row.get("id"), row.get("port_id")));
        return result;
    }


    private String findGroup(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "没找到";
    }

    @ApiOperation("检查ediFile")
    @PostMapping(value = "checkEdiFile")
    public void checkEdiFile(@RequestBody QueryDBReq req) {
        req.setInstanceName("prod-mysql-pv-ro");
        req.setDbName("as2");
        req.setSqlContent("select port_id, count(if(mode= 'receive', 1, null)) as 'receiveFileCount', count(if(mode= 'send', 1, null)) as 'sendFileCount' from t_transaction group by port_id;");
        req.setLimitNum("0");
    }

    @ApiOperation("检查flyway")
    @PostMapping(value = "checkFlyway")
    public void checkFlyway(@RequestBody QueryDBReq req) {
        Map<String, List<String>> dbMap = dbMap();
        // 统计是否有失败的flyway
        String countFailSql = "select count(1) from flyway_schema_history where success = 0;";
        // 设置查询条件
        req.setSqlContent(countFailSql);
        // 限制条数
        req.setLimitNum("0");
        // 循环处理
        dbMap.forEach((instanceName, dbNames) -> {
            // 设置实例
            req.setInstanceName(instanceName);
            // 循环数据库
            dbNames.forEach(dbName -> {
                // 设置数据库
                req.setDbName(dbName);
                // 查询
                D res = QueryDBHelper.getRes(req, restTemplate);
                // 获取总数
                int count = Integer.parseInt(res.getData().getRows().get(0).get(0));
                if (count != 0) {
                    log.error("实例:[{}]数据库:[{}]flyway执行失败", instanceName, dbName);
                }
            });
        });
    }

    @ApiOperation("字段配置树形结构处理")
    @GetMapping(value = "schemaTree")
    public void schemaTree() {
        // 查询所有的模块
        List<SchemaModuleRecord> modules = schemaDao.findAllSchemaModules();
        // 查询所有的字段
        List<SchemaFieldRecord> fields = schemaDao.findAllSchemaFields();
        // 字段按照模块分组
        Map<String, List<SchemaFieldRecord>> moduleFieldsMap = fields.stream().collect(Collectors.groupingBy(SchemaFieldRecord::getModuleId));
        // 根节点
        List<SchemaModuleRecord> roots = Lists.newArrayList();
        // 其他节点
        Map<String, List<SchemaModuleRecord>> parentChildrenMap = Maps.newHashMap();
        // 循环节点
        modules.forEach(module -> {
            if (StringUtils.isBlank(module.getParentId())) {
                roots.add(module);
            } else {
                parentChildrenMap.computeIfAbsent(module.getParentId(), v -> Lists.newArrayList()).add(module);
            }
        });
        // 循环root节点,替换id,parentId
        roots.forEach(root -> reset(root, parentChildrenMap, moduleFieldsMap));
        // 排序
        roots.sort(Comparator.comparingInt(SchemaModuleRecord::getSortIndex));
        // 组装数据
        roots.forEach(root -> print(root, parentChildrenMap, moduleFieldsMap));
        System.out.println();
        // 更新数据
        schemaDao.updateSchemaFields(fields);
        schemaDao.updateSchemaModules(modules);
    }

    private void print(SchemaModuleRecord module, Map<String, List<SchemaModuleRecord>> parentChildrenMap, Map<String, List<SchemaFieldRecord>> moduleFieldsMap) {
        log.info("当前模块[id:{},code:{},sortIndex:{}]", module.getId(), module.getCode(), module.getSortIndex());
        List<SchemaFieldRecord> moduleFields = moduleFieldsMap.getOrDefault(module.getId(), Lists.newArrayList());
        log.info("共有:[{}]个字段", moduleFields.size());
        moduleFields.forEach(moduleField -> {
            log.info("模块下字段:[id:{},描述:{},sortIndex:{}]", moduleField.getId(), moduleField.getDescription(), moduleField.getSortIndex());
        });
        List<SchemaModuleRecord> children = parentChildrenMap.getOrDefault(module.getId(), Lists.newArrayList());
        log.info("共有:[{}]个子级", children.size());
        children.forEach(child -> print(child, parentChildrenMap, moduleFieldsMap));
    }

    private void reset(SchemaModuleRecord module, Map<String, List<SchemaModuleRecord>> parentChildrenMap, Map<String, List<SchemaFieldRecord>> moduleFieldsMap) {
        // 判断当前模块下是否有字段
        List<SchemaFieldRecord> moduleFields = moduleFieldsMap.get(module.getId());
        // 新的id
        String newId = UUID.randomUUID().toString().replace("-", "");
        if (CollectionUtils.isNotEmpty(moduleFields)) {
            // 设置新的模块id
            moduleFields.forEach(field -> {
                field.setId(UUID.randomUUID().toString().replace("-", ""));
                field.setModuleId(newId);
            });
            moduleFieldsMap.put(newId, moduleFields);
        }
        // 判断是否有子级
        List<SchemaModuleRecord> children = parentChildrenMap.get(module.getId());
        // 设置新id
        module.setId(newId);
        if (CollectionUtils.isNotEmpty(children)) {
            // 在循环处理子级
            children.forEach(child -> {
                // 设置新的parentId
                child.setParentId(newId);
                // 递归处理子级
                reset(child, parentChildrenMap, moduleFieldsMap);
            });
            parentChildrenMap.put(newId, children);
        }
    }

    @ApiOperation("修复无需递交状态")
    @PostMapping(value = "fixNoSubmit")
    public void fixNoSubmit(@RequestBody QueryDBReq req) {
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report");
        req.setLimitNum("0");
        // 查询未完成的报告,但是有no_submit的
        String sql = "select id, no_submit, report_status, no_submit_reason from report_value where no_submit is not null and report_status != 'state_code_finish';";
        req.setSqlContent(sql);
        // 查询数据
        D res = QueryDBHelper.getRes(req, restTemplate);
        // 提取数据
        List<Map<String, String>> rowValues = QueryDBHelper.extractColumnValues(res, Lists.newArrayList("id"));
        // 循环组装
        List<FixData> fixDataInfos = rowValues.stream().map(row -> {
            FixData fixData = new FixData();
            fixData.setTable("pvs_report.report_value");
            fixData.setId(row.get("id"));
            Map<String, String> columnValueMap = Maps.newHashMap();
            columnValueMap.put("no_submit", null);
            fixData.setColumnValueMap(columnValueMap);
            return fixData;
        }).collect(Collectors.toList());
        ListUtils.partition(fixDataInfos, 100).forEach(partFixData -> System.out.println(JSON.toJSONString(partFixData, SerializerFeature.WriteMapNullValue)));
    }

    @SneakyThrows
    private static Map<String, Map<String, List<String>>> convert() {
        String mapJson = "{\"225e823c-85d0-11e9-a539-00163e02f99a\":{\"行列表字段配置中有发送者信息\":[\"8a8dbcfe8697f1bb01869aca8801012d\",\"8a8dbcfe8697f1bb01869aca8801012e\",\"8a8dbfab919dd1c60191c0f22ca2788c\",\"8a8dbfab919dd1c60191c0f22ca2788d\",\"8a8dbfab919dd1c60191c0f22ca3788e\",\"8a8dbfab919dd1c60191c0f22ca3788f\",\"8a8dbfab919dd1c60191c0f22ca37890\",\"8a8dbfab919dd1c60191c0f22ca37891\",\"8a8dbfab919dd1c60191c0f22ca37892\",\"8a8dbfab919dd1c60191c0f22ca37893\",\"8a8dbfab919dd1c60191c0f22ca37894\",\"8a8dbfab919dd1c60191c0f22ca37895\",\"8a8dbfab919dd1c60191c0f22ca37896\",\"8a8dbfab919dd1c60191c0f22ca37897\",\"8a8dbfab919dd1c60191c0f22ca37898\",\"8a8dbfab919dd1c60191c0f22ca37899\",\"8a8dbfab919dd1c60191c0f22ca3789a\"]},\"60039b8308d34fbe9bb0a7df0016ce0c\":{\"质控规则配置中有发送者信息\":[\"8a8dbd069025e28d019033042fc3022c\",\"8a8dbd069025e28d019033042fc3022d\",\"8a8dbd739025e40e0190330f9ab00724\",\"8a8dbd739025e40e0190330f9ab00725\"],\"行列表字段配置中有发送者信息\":[\"8a8dbf3c946486aa019471c5ab515c1d\"]},\"2018btyy0001\":{\"行列表字段配置中有发送者信息\":[\"8a8d81dd94fec732019568edb97c1d1d\"],\"高级检索条件中有发送者信息\":[\"8a8dbe0e926bd0f301929338f6e361a7\"]},\"8a8181276bc658ed016bd52a7ad56b67\":{\"高级检索条件中有发送者信息\":[\"8a8dbdea9025df5c01902e147fe440e3\"]},\"pv-esae\":{\"质控规则配置中有发送者信息\":[\"8a8d84868c39bb6f018c3e6969d924c4\",\"8a8d84868c39bb6f018c3e6969d924c5\",\"8a8d84868c39bb6f018c3e6969da24c6\",\"8a8d84868c39bb6f018c3e6969da24c7\",\"8a8d84868c39bb6f018c3e6969da24c8\",\"8a8d84868c39bb6f018c3e6969da24c9\",\"8a8d84868c39bb6f018c3e6969da24ca\",\"8a8d84868c39bb6f018c3e6969da24cb\",\"8a8d84868c39bb6f018c3e6969da24cc\",\"8a8d84868c39bb6f018c3e6969da24cd\",\"8a8d84868c39bb6f018c3e6969da24ce\",\"8a8d84868c39bb6f018c3e6969da24cf\",\"8a8d84868c39bb6f018c3e6969da24d0\",\"8a8d84868c39bb6f018c3e6969da24d1\",\"8a8d84868c39bb6f018c3e6969da24d2\",\"8a8d84868c39bb6f018c3e6969da24d3\",\"8a8d84868c39bb6f018c3e6969da24d4\",\"8a8dba398f611864018fa9de7b755969\",\"8a8dba398f611864018fa9de7b75596a\",\"8a8dba398f611864018fa9de7b75596b\",\"8a8dba398f611864018fa9de7b75596c\",\"8a8dba398f611864018fa9de7b75596d\",\"8a8dba398f611864018fa9de7b75596e\",\"8a8dba398f611864018fa9de7b75596f\",\"8a8dba398f611864018fa9de7b755970\",\"8a8dba398f611864018fa9de7b755971\",\"8a8dba398f611864018fa9de7b755972\",\"8a8dba398f611864018fa9de7b755973\",\"8a8dba398f611864018fa9de7b755974\",\"8a8dba398f611864018fa9de7b755975\",\"8a8dba398f611864018fa9de7b755976\",\"8a8dba398f611864018fa9de7b755977\",\"8a8dba398f611864018fa9de7b755978\",\"8a8dbe1a910da44901917cdaef83660a\"],\"字段质疑中有发送者信息\":[\"8a8d803d94feccf901952133df3561ec\",\"8a8d803d94feccf901952134026161f2\",\"8a8d803d94feccf901954526dd233ef5\",\"8a8d803d94feccf9019589aeac7d7a92\",\"8a8d807b88dd292d018900c53c966987\",\"8a8d80c58aa805f2018ab14ead5f1ad5\",\"8a8d80d08ad127ed018b22d9400d429b\",\"8a8d80db89b112780189ba1dd62531fb\",\"8a8d8181942a2179019448ed2d47172f\",\"8a8d829a95db86e60195f9543bd71fc5\",\"8a8d82bb94feca13019569788e6b3c2e\",\"8a8d831e8b8fb6bc018bb2f3e04f1df6\",\"8a8d839089980d3f0189af9a0a304edd\",\"8a8d84148bcd86a4018c19bae03657f1\",\"8a8d847e8bcd83dd018c14a5df7e6c8e\",\"8a8d856a8c8ab5b4018ca4b25d6c2839\",\"8a8dba36942a1baf019448ed0f954fd7\",\"8a8dbb2f8e984eb3018ec76d35b01289\",\"8a8dbb2f8e984eb3018ec76d4905128e\",\"8a8dbb2f8e984eb3018ecbd5d3517030\",\"8a8dbc6695db85180195f446d16326c4\",\"8a8dbcf287c04c570187c19b4b5b0502\",\"8a8dbf62882ecbcd0188320507480caa\"],\"行列表字段配置中有发送者信息\":[\"8a8dbd069025e28d019029fd9d9922bc\",\"8a8dbd069025e28d019029fd9d9922bd\",\"8a8dbd069025e28d019029fd9d9922be\",\"8a8dbd069025e28d019029fd9d9922bf\",\"8a8dbd069025e28d019029fd9d9922c0\",\"8a8dbd069025e28d019029fd9d9922c1\",\"8a8dbd069025e28d019029fd9d9922c2\",\"8a8dbd069025e28d019029fd9d9922c3\",\"8a8dbd069025e28d019029fd9d9922c4\",\"8a8dbd069025e28d019029fd9d9922c5\",\"8a8dbd069025e28d019029fd9d9922c6\",\"8a8dbd069025e28d019029fd9d9922c7\",\"8a8dbd069025e28d019029fd9d9922c8\",\"8a8dbd069025e28d019029fd9d9922c9\",\"8a8dbd069025e28d019029fd9d9a22ca\"]},\"eSafety20220621\":{\"行列表字段配置中有发送者信息\":[\"8a8d818e914609f801916984455152fa\",\"8a8d818e914609f801916984455152fb\",\"8a8d818e914609f801916984455152fc\",\"8a8d818e914609f801916984455152fd\",\"8a8d818e914609f801916984455152fe\",\"8a8d818e914609f801916984455152ff\",\"8a8d818e914609f80191698445515300\",\"8a8d818e914609f80191698445515301\",\"8a8d818e914609f80191698445525302\",\"8a8d818e914609f80191698445525303\",\"8a8d818e914609f80191698445525304\",\"8a8d818e914609f80191698445525305\",\"8a8d818e914609f80191698445525306\",\"8a8d818e914609f80191698445525307\",\"8a8d818e914609f80191698445525308\",\"8a8d86f08fc95f36018fcd75c0e44a45\",\"8a8d86f08fc95f36018fcd75c0e44a46\",\"8a8d86f08fc95f36018fcd75c0e44a47\",\"8a8d86f08fc95f36018fcd75c0e44a48\",\"8a8d86f08fc95f36018fcd75c0e44a49\",\"8a8d86f08fc95f36018fcd75c0e44a4a\",\"8a8d86f08fc95f36018fcd75c0e44a4b\",\"8a8dbd0c9059686e019106cf64095a1d\",\"8a8dbd0c9059686e019106cf64095a1e\",\"8a8dbd0c9059686e019106cf64095a1f\",\"8a8dbd0c9059686e019106cf64095a20\",\"8a8dbd0c9059686e019106cf64095a21\",\"8a8dbd0c9059686e019106cf64095a22\",\"8a8dbd0c9059686e019106cf64095a23\",\"8a8dbd0c9059686e019106cf64095a24\",\"8a8dbd0c9059686e019106cf64095a25\",\"8a8dbd0c9059686e019106cf64095a26\",\"8a8dbd0c9059686e019106cf64095a27\",\"8a8dbd0c9059686e019106cf64095a28\",\"8a8dbd0c9059686e019106cf64095a29\",\"8a8dbd0c9059686e019106cf64095a2a\",\"8a8dbd0c9059686e019106cf64095a2b\"]},\"8a8181e07e41b2fa017e4c2865860f36\":{\"行列表字段配置中有发送者信息\":[\"8a8d82048bcd87e2018bef81a4e436d9\"]},\"f0edceb0-6349-11e9-b21d-ef616d53ac34\":{\"行列表字段配置中有发送者信息\":[\"8a8d8242949273dd0194de0af0056512\",\"8a8d8242949273dd0194de0af0056513\"]},\"8a8181e07e41b2fa017e4c2865860f36_TEST\":{\"行列表字段配置中有发送者信息\":[\"8a8d82048bcd87e2018bef81a4e436d9_copy\"]},\"eSAE20250218\":{\"行列表字段配置中有发送者信息\":[\"8a8d835295b3e4f60195d5345e724d50\",\"8a8d835295b3e4f60195d5345e734d51\",\"8a8d835295b3e4f60195d5345e734d52\",\"8a8d835295b3e4f60195d5345e734d53\",\"8a8d835295b3e4f60195d5345e734d54\",\"8a8d835295b3e4f60195d5345e734d55\",\"8a8d835295b3e4f60195d5345e734d56\",\"8a8d835295b3e4f60195d5345e734d57\",\"8a8d835295b3e4f60195d5345e734d58\",\"8a8d835295b3e4f60195d5345e734d59\",\"8a8d835295b3e4f60195d5345e734d5a\",\"8a8d835295b3e4f60195d5345e734d5b\",\"8a8d835295b3e4f60195d5345e734d5c\",\"8a8d835295b3e4f60195d5345e734d5d\",\"8a8d835295b3e4f60195d5345e734d5e\"]},\"8a8181e0827d746b0182820c256c2a6c\":{\"高级检索条件中有发送者信息\":[\"8a8d82d18d11fe93018d5d45957f4645\",\"8a8d853f8d11f6ba018d5d45d8bc3c64\"]},\"adb0ac3bf54b4f199de4a6e9006815b0\":{\"行列表字段配置中有发送者信息\":[\"8a8dbc59920a02f401928ddbda0f1137\",\"8a8dbc59920a02f401928ddbda0f1138\",\"8a8dbc59920a02f401928ddbda0f1139\",\"8a8dbc59920a02f401928ddbda0f113a\",\"8a8dbc59920a02f401928ddbda0f113b\",\"8a8dbc59920a02f401928ddbda0f113c\",\"8a8dbc59920a02f401928ddbda0f113d\",\"8a8dbc59920a02f401928ddbda0f113e\",\"8a8dbc59920a02f401928ddbda0f113f\",\"8a8dbc59920a02f401928ddbda0f1140\",\"8a8dbc59920a02f401928ddbda0f1141\",\"8a8dbc59920a02f401928ddbda0f1142\",\"8a8dbc59920a02f401928ddbda0f1143\",\"8a8dbc59920a02f401928ddbda0f1144\",\"8a8dbc59920a02f401928ddbda0f1145\"]},\"TM-PV-238\":{\"质控规则配置中有发送者信息\":[\"8a8d81ca8df49e0b018e35d670001ac4\",\"8a8d81ca8df49e0b018e35d670001ac5\",\"8a8d81ca8df49e0b018e35d670001ac6\",\"8a8d81ca8df49e0b018e35d670001ac7\",\"8a8d81ca8df49e0b018e35d670001ac8\",\"8a8d81ca8df49e0b018e35d670001ac9\",\"8a8d81ca8df49e0b018e35d670001aca\",\"8a8d81ca8df49e0b018e35d670001acb\",\"8a8d81ca8df49e0b018e35d670001acc\",\"8a8d81ca8df49e0b018e35d670001acd\",\"8a8d81ca8df49e0b018e35d670001ace\",\"8a8d81ca8df49e0b018e35d670001acf\",\"8a8d81ca8df49e0b018e35d670001ad0\",\"8a8d81ca8df49e0b018e35d670001ad1\",\"8a8d81ca8df49e0b018e35d670001ad2\",\"8a8d873d8e84be58018ee5ed6448189c\",\"8a8d873d8e84be58018ee5ed6448189d\",\"8a8d873d8e84be58018ee5f685131ad6\",\"8a8d873d8e84be58018ee5f685131ad7\"],\"字段质疑中有发送者信息\":[\"8a8d81a1949276d80194f41eb94c2ae6\",\"8a8d88b98e452352018e7973284b55f6\",\"8a8dbd99905972a5019096672bae5c60\",\"8a8dbd9990da0d090190e3c7db9d3e22\"],\"行列表字段配置中有发送者信息\":[\"8a8d82bb94feca1301952681d6d97bc7\",\"8a8d82bb94feca1301952681d6d97bc8\",\"8a8d82bb94feca1301952681d6d97bc9\",\"8a8d82bb94feca1301952681d6d97bca\",\"8a8d82bb94feca1301952681d6d97bcb\",\"8a8d82bb94feca1301952681d6d97bcc\",\"8a8d82bb94feca1301952681d6d97bcd\",\"8a8d82bb94feca1301952681d6d97bce\",\"8a8d82bb94feca1301952681d6d97bcf\",\"8a8d82bb94feca1301952681d6d97bd0\",\"8a8d82bb94feca1301952681d6d97bd1\",\"8a8d82bb94feca1301952681d6d97bd2\",\"8a8d82bb94feca1301952681d6d97bd3\",\"8a8d82bb94feca1301952681d6d97bd4\",\"8a8d82bb94feca1301952681d6d97bd5\",\"8a8d87368e4521ec018e7983a39747e0\",\"8a8d87368e4521ec018e7983a39747e1\",\"8a8d87368e4521ec018e7983a39747e2\",\"8a8d87368e4521ec018e7983a39747e3\",\"8a8d87368e4521ec018e7983a39747e4\",\"8a8d87368e4521ec018e7983a39747e5\",\"8a8d87368e4521ec018e7983a39747e6\",\"8a8d87368e4521ec018e7983a39747e7\",\"8a8d87368e4521ec018e7983a39747e8\",\"8a8d87368e4521ec018e7983a39747e9\",\"8a8d87368e4521ec018e7983a39747ea\",\"8a8d87368e4521ec018e7983a39747eb\",\"8a8d87368e4521ec018e7983a39747ec\",\"8a8d87368e4521ec018e7983a39747ed\",\"8a8d87368e4521ec018e7983a39747ee\"]},\"eSafety20230717\":{\"行列表字段配置中有发送者信息\":[\"8a8dbb298963c9100189761df11e6757\"]},\"203d0ad0-6347-11e9-b21d-ef616d53ac34\":{\"行列表字段配置中有发送者信息\":[\"8a8dbd0c9059686e0190776be81a4771\",\"8a8dbd0c9059686e0190776be81a4772\",\"8a8dbd0c9059686e0190776be81a4773\"]},\"edcd8dd0-6347-11e9-b21d-ef616d53ac34_TEST\":{\"行列表字段配置中有发送者信息\":[\"8a8d80688963c7ea018996248ee769ed\",\"8a8d80688963c7ea018996248ee769ee\",\"8a8d80688963c7ea018996248ee769ef\",\"8a8d80688963c7ea018996248ee769f0\",\"8a8d80688963c7ea018996248ee769f1\",\"8a8d80688963c7ea018996248ee769f2\",\"8a8d80688963c7ea018996248ee769f3\",\"8a8d80688963c7ea018996248ee769f4\",\"8a8d80688963c7ea018996248ee769f5\",\"8a8d80688963c7ea018996248ee769f6\",\"8a8d80688963c7ea018996248ee769f7\",\"8a8d80688963c7ea018996248ee769f8\",\"8a8d80688963c7ea018996248ee769f9\",\"8a8d80688963c7ea018996248ee769fa\",\"8a8d80688963c7ea018996248ee769fb\"]},\"local03-eng-pv\":{\"行列表字段配置中有发送者信息\":[\"8a8d839e899816d10189b089fdd86048\",\"8a8d839e899816d10189b089fdd86049\"]},\"8a8d83018cd42fa3018e3b7d56ea7d30\":{\"行列表字段配置中有发送者信息\":[\"8a8d803d94feccf901955b09870c6500\"]},\"8a81812775ff694d01762c6b8c5804de\":{\"高级检索条件中有发送者信息\":[\"8a8d804a930651d601931995ed6947c1\"]},\"cc6c1020-6348-11e9-b21d-ef616d53ac34\":{\"行列表字段配置中有发送者信息\":[\"8a8d81f393b529d90193ce431eb309d2\",\"8a8d81f393b529d90193ce431eb309d3\",\"8a8d81f393b529d90193ce431eb309d4\",\"8a8d81f393b529d90193ce431eb309d5\",\"8a8d81f393b529d90193ce431eb309d6\",\"8a8d81f393b529d90193ce431eb309d7\",\"8a8d81f393b529d90193ce431eb309d8\",\"8a8d81f393b529d90193ce431eb309d9\",\"8a8d81f393b529d90193ce431eb309da\",\"8a8d81f393b529d90193ce431eb309db\",\"8a8d81f393b529d90193ce431eb309dc\",\"8a8d81f393b529d90193ce431eb309dd\",\"8a8d81f393b529d90193ce431eb309de\",\"8a8d81f393b529d90193ce431eb309df\",\"8a8d81f393b529d90193ce431eb309e0\"]},\"eSafety20240618\":{\"字段质疑中有发送者信息\":[\"8a8dbea19097317201909a7675ef2ccd\"]},\"8a8dbc368819044b01886cf7327f16de_TEST\":{\"行列表字段配置中有发送者信息\":[\"8a8d80f8886d2f4501886d3104c900ce_copy\"]},\"0dd84a7f1b4ea24984644563e1232830\":{\"质控规则配置中有发送者信息\":[\"8a8d80a487b85e9f01880f2f70484a9b\",\"8a8d80a487b85e9f01880f2f70484a9c\"]},\"1110\":{\"质控规则配置中有发送者信息\":[\"2c9c812882cfa9230182cfe05c40002c\",\"2c9c812882cfa9230182cfe05c40002d\"]},\"912201012440638092\":{\"行列表字段配置中有发送者信息\":[\"8a8d832c8aa80eb5018ab654b8d16203\",\"8a8d832c8aa80eb5018ab654b8d16204\"]},\"123c99a0-6347-11e9-b21d-ef616d53ac34\":{\"行列表字段配置中有发送者信息\":[\"8a8d82e98a899136018a923efa5151b7\",\"8a8d82e98a899136018a923efa5151b8\",\"8a8d82e98a899136018a923efa5151b9\",\"8a8d82e98a899136018a923efa5151ba\",\"8a8d82e98a899136018a923efa5151bb\",\"8a8d82e98a899136018a923efa5151bc\",\"8a8d82e98a899136018a923efa5151bd\",\"8a8d82e98a899136018a923efa5151be\",\"8a8d82e98a899136018a923efa5151bf\",\"8a8d82e98a899136018a923efa5151c0\",\"8a8d82e98a899136018a923efa5151c1\",\"8a8d82e98a899136018a923efa5151c2\",\"8a8d82e98a899136018a923efa5151c3\",\"8a8d82e98a899136018a923efa5151c4\",\"8a8d82e98a899136018a923efa5151c5\"]},\"global01-pv\":{\"字段质疑中有发送者信息\":[\"8a8d803a89f8db7b0189f8e7035000b2\",\"8a8d803a89f8db7b0189f8e7143600b6\",\"8a8d81ae89f8de000189f8e70bb90132\"]},\"8a81813e77d90d800177ddb8d883041b\":{\"字段质疑中有发送者信息\":[\"8a8dbe0e926bd0f3019279884be14d2c\"]},\"ae283e70-634f-11e9-a5dd-d5f968110a45\":{\"行列表字段配置中有发送者信息\":[\"8a8d805f93d4b1d00193d84dec0233b4\",\"8a8d805f93d4b1d00193d84dec0233b5\",\"8a8d805f93d4b1d00193d84dec0233b6\",\"8a8d805f93d4b1d00193d84dec0233b7\",\"8a8d805f93d4b1d00193d84dec0233b8\",\"8a8d805f93d4b1d00193d84dec0233b9\",\"8a8d805f93d4b1d00193d84dec0233ba\",\"8a8d805f93d4b1d00193d84dec0233bb\",\"8a8d805f93d4b1d00193d84dec0233bc\",\"8a8d805f93d4b1d00193d84dec0233bd\",\"8a8d805f93d4b1d00193d84dec0233be\",\"8a8d805f93d4b1d00193d84dec0233bf\",\"8a8d805f93d4b1d00193d84dec0233c0\",\"8a8d805f93d4b1d00193d84dec0233c1\",\"8a8d805f93d4b1d00193d84dec0233c2\"]},\"8a81c090776cbcf2017849a9cfe70968\":{\"字段质疑中有发送者信息\":[\"8a8d88b9910da159019112c01fd3430e\"]},\"eSafety20241216\":{\"行列表字段配置中有发送者信息\":[\"8a8dba36942a1baf01943e5e6b3b268e\",\"8a8dba36942a1baf01943e5e6b3b268f\",\"8a8dba36942a1baf01943e5e6b3b2690\",\"8a8dba36942a1baf01943e5e6b3b2691\",\"8a8dba36942a1baf01943e5e6b3b2692\",\"8a8dba36942a1baf01943e5e6b3b2693\",\"8a8dba36942a1baf01943e5e6b3b2694\",\"8a8dba36942a1baf01943e5e6b3b2695\",\"8a8dba36942a1baf01943e5e6b3b2696\",\"8a8dba36942a1baf01943e5e6b3b2697\",\"8a8dba36942a1baf01943e5e6b3b2698\",\"8a8dba36942a1baf01943e5e6b3b2699\",\"8a8dba36942a1baf01943e5e6b3b269a\",\"8a8dba36942a1baf01943e5e6b3b269b\",\"8a8dba36942a1baf01943e5e6b3b269c\"]},\"91310115MA1K3Q5R7K\":{\"行列表字段配置中有发送者信息\":[\"8a8d819e890f19b80189253df72854b7\",\"8a8d819e890f19b80189253df72854b8\",\"8a8d819e890f19b80189253df72854b9\",\"8a8d819e890f19b80189253df72854ba\",\"8a8d819e890f19b80189253df72854bb\",\"8a8d819e890f19b80189253df72854bc\",\"8a8d819e890f19b80189253df72854bd\",\"8a8d819e890f19b80189253df72854be\",\"8a8d819e890f19b80189253df72854bf\",\"8a8d819e890f19b80189253df72854c0\",\"8a8d819e890f19b80189253df72854c1\",\"8a8d819e890f19b80189253df72854c2\",\"8a8d819e890f19b80189253df72854c3\",\"8a8d819e890f19b80189253df72854c4\",\"8a8d819e890f19b80189253df72854c5\"]},\"8a8dbc368819044b01886cf7327f16de\":{\"行列表字段配置中有发送者信息\":[\"8a8d80f8886d2f4501886d3104c900ce\"]},\"91330100609120766P\":{\"行列表字段配置中有发送者信息\":[\"8a8d826b94834ffb019490b9fbe65194\",\"8a8d826b94834ffb019490b9fbe65195\",\"8a8d826b94834ffb019490b9fbe65196\",\"8a8d826b94834ffb019490b9fbe65197\",\"8a8d826b94834ffb019490b9fbe65198\",\"8a8d826b94834ffb019490b9fbe65199\",\"8a8d826b94834ffb019490b9fbe6519a\",\"8a8d826b94834ffb019490b9fbe6519b\",\"8a8d826b94834ffb019490b9fbe6519c\",\"8a8d826b94834ffb019490b9fbe6519d\",\"8a8d826b94834ffb019490b9fbe6519e\",\"8a8d826b94834ffb019490b9fbe6519f\",\"8a8d826b94834ffb019490b9fbe651a0\",\"8a8d826b94834ffb019490b9fbe651a1\",\"8a8d826b94834ffb019490b9fbe651a2\"]},\"8a81813e795b25a9017964c48644163e_TEST\":{\"行列表字段配置中有发送者信息\":[\"8a8d81f48a78c559018a88250ece4389\",\"8a8d81f48a78c559018a88250ece438a\",\"8a8d81f48a78c559018a88250ece438b\",\"8a8d81f48a78c559018a88250ece438c\",\"8a8d81f48a78c559018a88250ece438d\",\"8a8d81f48a78c559018a88250ece438e\",\"8a8d81f48a78c559018a88250ece438f\",\"8a8d81f48a78c559018a88250ece4390\",\"8a8d81f48a78c559018a88250ece4391\",\"8a8d81f48a78c559018a88250ece4392\",\"8a8d81f48a78c559018a88250ece4393\",\"8a8d81f48a78c559018a88250ece4394\",\"8a8d81f48a78c559018a88250ece4395\",\"8a8d81f48a78c559018a88250ece4396\",\"8a8d81f48a78c559018a88250ece4397\"]},\"8a81813e795b25a9017964c48644163e\":{\"行列表字段配置中有发送者信息\":[\"8a8d81b98a78c752018a882def7d47e0\",\"8a8d81b98a78c752018a882def7d47e1\",\"8a8d81b98a78c752018a882def7d47e2\",\"8a8d81b98a78c752018a882def7d47e3\",\"8a8d81b98a78c752018a882def7d47e4\",\"8a8d81b98a78c752018a882def7d47e5\",\"8a8d81b98a78c752018a882def7d47e6\",\"8a8d81b98a78c752018a882def7d47e7\",\"8a8d81b98a78c752018a882def7d47e8\",\"8a8d81b98a78c752018a882def7d47e9\",\"8a8d81b98a78c752018a882def7d47ea\",\"8a8d81b98a78c752018a882def7d47eb\",\"8a8d81b98a78c752018a882def7d47ec\",\"8a8d81b98a78c752018a882def7d47ed\",\"8a8d81b98a78c752018a882def7d47ee\"]},\"eSAE20221205\":{\"字段质疑中有发送者信息\":[\"8a8d80db89b112780189dd35adc15582\",\"8a8d80db89b112780189dd35ebea558a\",\"8a8d80db89b112780189dd364d69559c\",\"8a8d83e789b111d30189dd3625045ad0\"]},\"8a8181e07f26bf7b017f2fecf8d82484_TEST\":{\"行列表字段配置中有发送者信息\":[\"8a8d81cc93f85581019406dbce885998\",\"8a8d81cc93f85581019406dbce885999\",\"8a8d81cc93f85581019406dbce88599a\",\"8a8d81cc93f85581019406dbce88599b\",\"8a8d81cc93f85581019406dbce88599c\",\"8a8d81cc93f85581019406dbce88599d\",\"8a8d81cc93f85581019406dbce88599e\",\"8a8d81cc93f85581019406dbce88599f\",\"8a8d81cc93f85581019406dbce8859a0\",\"8a8d81cc93f85581019406dbce8859a1\",\"8a8d81cc93f85581019406dbce8859a2\",\"8a8d81cc93f85581019406dbce8859a3\",\"8a8d81cc93f85581019406dbce8859a4\",\"8a8d81cc93f85581019406dbce8859a5\"]},\"eSafety20240102\":{\"行列表字段配置中有发送者信息\":[\"8a8d82e98a899136018a923efa5151b7_copy\",\"8a8d82e98a899136018a923efa5151b8_copy\",\"8a8d82e98a899136018a923efa5151b9_copy\",\"8a8d82e98a899136018a923efa5151ba_copy\",\"8a8d82e98a899136018a923efa5151bb_copy\",\"8a8d82e98a899136018a923efa5151bc_copy\",\"8a8d82e98a899136018a923efa5151bd_copy\",\"8a8d82e98a899136018a923efa5151be_copy\",\"8a8d82e98a899136018a923efa5151bf_copy\",\"8a8d82e98a899136018a923efa5151c0_copy\",\"8a8d82e98a899136018a923efa5151c1_copy\",\"8a8d82e98a899136018a923efa5151c2_copy\",\"8a8d82e98a899136018a923efa5151c3_copy\",\"8a8d82e98a899136018a923efa5151c4_copy\",\"8a8d82e98a899136018a923efa5151c5_copy\"]},\"8a8d83d68ca07a5b018ca418a3dd175f_TEST\":{\"行列表字段配置中有发送者信息\":[\"8a8d81af8cedf3e2018cf803496d5de3\",\"8a8d81af8cedf3e2018cf803496d5de4\",\"8a8d81af8cedf3e2018cf803496d5de5\",\"8a8d81af8cedf3e2018cf803496d5de6\",\"8a8d81af8cedf3e2018cf803496d5de7\",\"8a8d81af8cedf3e2018cf803496d5de8\",\"8a8d81af8cedf3e2018cf803496d5de9\",\"8a8d81af8cedf3e2018cf803496d5dea\",\"8a8d81af8cedf3e2018cf803496d5deb\",\"8a8d81af8cedf3e2018cf803496d5dec\",\"8a8d81af8cedf3e2018cf803496d5ded\",\"8a8d81af8cedf3e2018cf803496d5dee\",\"8a8d81af8cedf3e2018cf803496d5def\",\"8a8d81af8cedf3e2018cf803496d5df0\",\"8a8d81af8cedf3e2018cf803496d5df1\"]},\"local01-pv\":{\"行列表字段配置中有发送者信息\":[\"8a8d82428a4b6311018a63de82180fc2\",\"8a8d82428a4b6311018a63de82180fc3\",\"8a8d82428a4b6311018a63de82180fc4\",\"8a8d82428a4b6311018a63de82180fc5\",\"8a8d82428a4b6311018a63de82180fc6\",\"8a8d82428a4b6311018a63de82180fc7\",\"8a8d82428a4b6311018a63de82180fc8\",\"8a8d82428a4b6311018a63de82180fc9\",\"8a8d82428a4b6311018a63de82180fca\",\"8a8d82428a4b6311018a63de82180fcb\",\"8a8d82428a4b6311018a63de82180fcc\",\"8a8d82428a4b6311018a63de82180fcd\",\"8a8d82428a4b6311018a63de82180fce\",\"8a8d82428a4b6311018a63de82180fcf\",\"8a8d82428a4b6311018a63de82180fd0\",\"8a8d82428a4b6311018a63de82180fd1\",\"8a8d82428a4b6311018a63de82180fd2\"]},\"8a8d82d68997a902018aab79b4be28b7\":{\"行列表字段配置中有发送者信息\":[\"8a8dbd5a920a01f0019279ac857c0fc3\",\"8a8dbd5a920a01f0019279ac857c0fc4\",\"8a8dbd5a920a01f0019279ac857c0fc5\",\"8a8dbd5a920a01f0019279ac857c0fc6\",\"8a8dbd5a920a01f0019279ac857c0fc7\",\"8a8dbd5a920a01f0019279ac857c0fc8\",\"8a8dbd5a920a01f0019279ac857c0fc9\",\"8a8dbd5a920a01f00192b32b773f29b1\",\"8a8dbd5a920a01f00192b32b773f29b2\",\"8a8dbd5a920a01f00192b32b773f29b3\",\"8a8dbd5a920a01f00192b32b773f29b4\",\"8a8dbd5a920a01f00192b32b773f29b5\",\"8a8dbd5a920a01f00192b32b773f29b6\",\"8a8dbd5a920a01f00192b32b773f29b7\"]},\"XMWJGL\":{\"行列表字段配置中有发送者信息\":[\"8a8d81f8934265f401937194b8e44b1f\",\"8a8d81f8934265f401937194b8e44b20\",\"8a8d81f8934265f401937194b8e44b21\",\"8a8d81f8934265f401937194b8e44b22\",\"8a8d81f8934265f401937194b8e44b23\",\"8a8dbd5a920a01f00192d79f4139603e\",\"8a8dbd5a920a01f00192d79f4139603f\",\"8a8dbd5a920a01f00192d79f41396040\",\"8a8dbd5a920a01f00192d79f41396041\",\"8a8dbd5a920a01f00192d79f41396042\",\"8a8dbd5a920a01f00192d79f41396043\",\"8a8dbd5a920a01f00192d79f41396044\",\"8a8dbd5a920a01f00192d79f41396045\",\"8a8dbd5a920a01f00192d79f41396046\",\"8a8dbd5a920a01f00192d79f41396047\",\"8a8dbd5a920a01f00192d79f41396048\",\"8a8dbd5a920a01f00192d79f41396049\",\"8a8dbd5a920a01f00192d79f4139604a\",\"8a8dbd5a920a01f00192d79f4139604b\",\"8a8dbd5a920a01f00192d79f4139604c\"]},\"eSAE20240708\":{\"行列表字段配置中有发送者信息\":[\"8a8dbd5a920a01f001929ed5e2df63d4\",\"8a8dbd5a920a01f001929ed5e2df63d5\",\"8a8dbd5a920a01f001929ed5e2df63d6\",\"8a8dbd5a920a01f001929ed5e2df63d7\",\"8a8dbd5a920a01f001929ed5e2df63d8\",\"8a8dbd5a920a01f001929ed5e2df63d9\",\"8a8dbd5a920a01f001929ed5e2df63da\",\"8a8dbd5a920a01f001929ed5e2df63db\",\"8a8dbd5a920a01f001929ed5e2e063dc\",\"8a8dbd5a920a01f001929ed5e2e063dd\",\"8a8dbd5a920a01f001929ed5e2e063de\",\"8a8dbd5a920a01f001929ed5e2e063df\",\"8a8dbd5a920a01f001929ed5e2e063e0\",\"8a8dbd5a920a01f001929ed5e2e063e1\",\"8a8dbd5a920a01f001929ed5e2e063e2\"]}}\n";
        ObjectMapper objectMapper = new ObjectMapper();
        // 将 JSON 字符串解析为 Map<String, Map<String, List<String>>>
        return objectMapper.readValue(mapJson, new TypeReference<Map<String, Map<String, List<String>>>>() {});
    }

    @SneakyThrows
    @ApiOperation("获取批量导出信息")
    @PostMapping(value = "getBatchExportInfo")
    public void getBatchExportInfo(@RequestBody QueryDBReq req) {
        req.setCookie(QueryDBHelper.DB_COOKIE);
        req.setCsrfToken(QueryDBHelper.DB_CSRF_TOKEN);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 查询任务
        String sql = "select param from task_info_batch_process_item where batch_no = '202504062034008a8d829a95db86e601960b17f0c13b5a'";
        req.setSqlContent(sql);
        List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("param"));
        List<BatchExportInfo> reqs = Lists.newArrayList();
        rows.forEach(row -> {
            BatchSubmitInfo submitInfo = JSON.parseObject(row.get("param"), BatchSubmitInfo.class);
            // 导出信息
            reqs.addAll(submitInfo.getAutoSubmitReqList());
        });
        System.out.println();
        System.out.println(JSON.toJSONString(reqs));
        System.out.println();
    }

    @SneakyThrows
    @ApiOperation("autoMeddra")
    @PostMapping(value = "autoMeddra")
    public void autoMeddra(@RequestBody List<String> reportIds) {
        QueryDBHelper.autoMeddra(restTemplate, QueryDBHelper.ESAFETY_5_COOKIE, reportIds);
    }

    @SneakyThrows
    @ApiOperation("finishMyStudyTask")
    @PostMapping(value = "finishMyStudyTask")
    public void finishMyStudyTask() {
        QueryDBHelper.finishTask(restTemplate);
    }

    @SneakyThrows
    @ApiOperation("getAnswer")
    @PostMapping(value = "getAnswer")
    public List<List<String>> getAnswer(@RequestBody AnswerInfo answerInfo) {
        List<List<String>> result = Lists.newArrayList();
        answerInfo.getData().stream().forEach(answerInfo1 -> {
            List<String> an = Lists.newArrayList();
            answerInfo1.getListQuestionAnswerList().forEach(answerInfo2 -> {
                if (answerInfo2.getIsAnswer() == 1) {
                    System.out.println(answerInfo2.getAnswerChoice());
                    an.add(answerInfo2.getAnswerChoice().split("\\.")[0]);
                }
            });
            result.add(an);
        });
        return result;
    }

    @SneakyThrows
    @ApiOperation("测试easyExcel")
    @GetMapping(value = "testEasyExcel")
    public void testEasyExcel(@RequestParam Integer rowIndex) {
        // 表头
        List<String> headers = Lists.newArrayList("column1", "column2", "column3", "column4");
        // 表格数据
        List<List<String>> table = Lists.newArrayList();
        for (int i = 0; i < rowIndex; i++) {
            List<String> rows = Lists.newArrayList();
            for (String header : headers) {
                rows.add(String.valueOf(i));
            }
            table.add(rows);
        }
        QueryDBHelper.exportExcel(headers.stream().map(Lists::newArrayList).collect(Collectors.toList()), table, "test");
    }

    @SneakyThrows
    @ApiOperation("获取公司信息")
    @PostMapping(value = "getCompanyInfo")
    public void getCompanyInfo() {
        // 转换数据
        Map<String, Map<String, List<String>>> map = prodSenderSearch(new QueryDBReq());
        Map<String, List<String>> result = Maps.newHashMap();
        map.forEach((tenantId, moduleInfoMap) -> {
            // 查询租户信息
            CompanyInfo companyInfo = QueryDBHelper.getCompanyInfo(restTemplate, QueryDBHelper.ESAFETY_5_COOKIE, tenantId);
            // 公司名称
            moduleInfoMap.forEach((module, moduleInfos) -> {
                result.computeIfAbsent(module, v -> Lists.newArrayList()).add(companyInfo.getName());
            });
        });
        List<List<String>> table = Lists.newArrayList();
        // 导出excel
        result.forEach((module, companyInfos) -> companyInfos.forEach(companyInfo -> table.add(Lists.newArrayList(module, companyInfo))));
        QueryDBHelper.exportExcel2(Lists.newArrayList("模块", "公司名称"), table, "线上发送者信息情况");
        System.out.println();
    }



    @SneakyThrows
    @ApiOperation("填充字段校验里的最大长度")
    @PostMapping(value = "fillFieldCheckMaxLength")
    public void fillFieldCheckMaxLength(@RequestBody QueryDBReq req) {
        req.setCookie(QueryDBHelper.DB_COOKIE);
        req.setCsrfToken(QueryDBHelper.DB_CSRF_TOKEN);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pv-ro");
        req.setDbName("pv");
        // 读取配置
        ReportCheckConfigDTO reportCheckConfig = readConfig();
        // 按照dataPath分成父子级
        Map<String, List<FieldChekConfigDTO>> parentChildrenMap = Maps.newLinkedHashMap();
        // 循环配置,处理父子级
        reportCheckConfig.getFieldChekConfigs().forEach(checkConfig -> {
            String substring = checkConfig.getDataPath().substring(0, checkConfig.getDataPath().lastIndexOf("."));
            parentChildrenMap.computeIfAbsent(substring, v -> Lists.newArrayList()).add(checkConfig);
        });
        // 对象与表的关系
        Map<String, String> tableMap = Maps.newHashMap();
        tableMap.put("reportValueDto", "report_value");
        tableMap.put("tmReporterDtoList", "tm_reporter");
        tableMap.put("tmDocumentretrievalDtoList", "tm_documentretrieval");
        tableMap.put("patientInformationDto", "report_patient_information");
        tableMap.put("tmOthermedicalhistoryDtoList", "tm_othermedicalhistory");
        tableMap.put("tmLabdataDtoList", "tm_labdata");
        tableMap.put("tmDrugDtoList.tmMedicationReasonDtoList", "tm_medication_reason");
        tableMap.put("tmDrugDtoList.tmDrugDoseDtoList", "tm_drug_dose");
        tableMap.put("tmDrugDtoList.tmDrugPsurItemDtoList", "tm_drug_psur_item");
        tableMap.put("tmDrugDtoList", "tm_drug");
        tableMap.put("tmAdverseeventDtoList", "tm_adverseevent");
        tableMap.put("tmCausalityDtoList", "tm_causality");
        tableMap.put("tmCausalityDtoList.allEvaList", "tm_company_reporter");
        tableMap.put("tmRelatedtocodeDtoList", "tm_relatedtocode");
        tableMap.put("tmMedWatchDto", "tm_med_watch");
        tableMap.put("tmCaseSummaryDtoList", "tm_case_summary");
        tableMap.put("tmCauseOfDeathDtoList", "tm_causeofdeath");
        tableMap.put("tmParentRelatedMedicalHistoryDtoList", "tm_parent_related_medical_history");
        tableMap.put("tmParentPreviousMedicalHistoryDtoList", "tm_parent_previous_medical_history");
        tableMap.put("tmBabyInfoDtoList", "tm_baby_info");

        // 查询线上对应表里字段的长度
        parentChildrenMap.forEach((javaObjName, fieldCheckConfigs) -> {
            // 表名
            String tableName = tableMap.get(javaObjName);
            // 查询这个表下所有列
            String sql = String.format("SELECT COLUMN_NAME AS 'Column Name', DATA_TYPE AS 'Data Type', CHARACTER_MAXIMUM_LENGTH AS 'Max Length' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '%s';", tableName);
            req.setSqlContent(sql);
            // 查询数据
            D res = QueryDBHelper.getRes(req, restTemplate);
            // 提取数据
            List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(res, Lists.newArrayList("Column Name", "Data Type", "Max Length"));
            // 处理数据
            Map<String, Pair<String, String>> columnDataTypeInfoMap = Maps.newHashMap();
            // 列配置
            Map<String, String> columnMap = Maps.newHashMap();
            rows.forEach(row -> {
                // 列名
                String columnName = row.get("Column Name");
                // 数据类型
                String dataType = row.get("Data Type");
                // 字段长度
                String maxLength = row.get("Max Length");
                // 处理列名,去掉下划线,然后全传小写
                String finalColumnName = columnName.replace("_", "").toLowerCase();
                columnDataTypeInfoMap.put(finalColumnName, Pair.of(dataType, maxLength));
                columnMap.put(finalColumnName, columnName);
            });
            // 特殊映射
            Map<String, String> specialMapping = Maps.newHashMap();
            specialMapping.put("tmDrugDtoList.tmDrugDoseDtoList.doseStartDate", "start_date");
            specialMapping.put("tmDrugDtoList.tmDrugDoseDtoList.doseEndDate", "end_date");
            columnMap.put("startdate", "start_date");
            columnMap.put("enddate", "end_date");
            // 再循环配置,根据字段找最大值
            fieldCheckConfigs.forEach(fieldChekConfig -> {
                String fieldName;
                if (specialMapping.containsKey(fieldChekConfig.getDataPath())) {
                    fieldName = specialMapping.get(fieldChekConfig.getDataPath()).replace("_", "").toLowerCase();
                } else {
                    // 取字段名都转小写
                    fieldName = fieldChekConfig.getDataPath().substring(fieldChekConfig.getDataPath().lastIndexOf(".") + 1).toLowerCase();
                }
                // 取数据库的
                Pair<String, String> pair = columnDataTypeInfoMap.get(fieldName);
                if (pair == null) {
                    System.out.println("=====================问题数据" + fieldChekConfig.getDataPath());
                } else {
                    String dataType = pair.getKey();
                    if (StringUtils.contains(dataType, "char") || StringUtils.contains(dataType, "text")) {
                        // 有数据,设置最大长度
                        fieldChekConfig.setMaxLength(Integer.valueOf(pair.getValue()));
                    } else {
                        System.out.println("=======================不是字符串类型数据" + fieldChekConfig.getDataPath());
                    }
                    // 设置表
                    fieldChekConfig.setTable(tableName);
                    fieldChekConfig.setColumn(columnMap.get(fieldName));
                }
            });
        });
        // 排序一下
        reportCheckConfig.getFieldChekConfigs().sort(Comparator.comparing(FieldChekConfigDTO::getTable));
        System.out.println();
    }

    public ReportCheckConfigDTO readConfig() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("/ReportFieldCheckConfig.json")) {
            if (inputStream == null) {
                throw new IOException("File not found");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            // 将输入流转换成ReportCheckConfigDTO对象
            return objectMapper.readValue(inputStream, ReportCheckConfigDTO.class);
        }
    }

    @ApiOperation("修复先声数据")
    @PostMapping(value = "fixXianShen")
    public void fixXianShen(@RequestBody QueryDBReq req) {
        // 补充导出数据
        // fillExportInfo(req, restTemplate);
        // 查询所有的报告编号
        List<String> allReportNos = xianShenReportNoDao.findAllReportNos();
        Map<String, ExcelData> excelDataMap = readExcelData(Lists.newArrayList("新校验文件.xls"));
        // 去重的
        Map<String, Set<String>> distinctErrorReportsMap = Maps.newHashMap();
        // 所有的
        Map<String, List<String>> errorReportMap = Maps.newHashMap();
        // 模块下错误信息的字段分类
        Map<String, Map<String, Set<String>>> moduleErrorFieldsMap = Maps.newHashMap();
        // 表头
        List<List<String>> headers = Lists.newArrayList();
        // 表格
        List<List<String>> table = Lists.newArrayList();
        for (Entry<String, ExcelData> entry : excelDataMap.entrySet()) {
            // excel数据
            ExcelData excelData = entry.getValue();
            headers = excelData.getSheetHeadValues().get("E2B R3校验").stream().map(header -> Lists.newArrayList(header)).collect(Collectors.toList());
            // 行数据
            List<Map<String, String>> rows = excelData.getSheetRowsMap().get("E2B R3校验");
            // 移除掉不需要处理的
            rows.removeIf(row -> !allReportNos.contains(row.get("报告编号")));
            // 取提示信息,取报告
            rows.forEach(row -> {
                String key = String.format("%s|%s|%s", row.get("提示信息"), row.get("页面名称"), row.get("字段"));
                // 所有的
                errorReportMap.computeIfAbsent(key, v -> Lists.newArrayList()).add(row.get("报告编号"));
                // 去重的
                distinctErrorReportsMap.computeIfAbsent(key, v -> Sets.newHashSet()).add(row.get("报告编号"));
                // 模块字段错误信息
                moduleErrorFieldsMap.computeIfAbsent(row.get("页面名称"), v -> Maps.newHashMap()).computeIfAbsent(row.get("提示信息"), v -> Sets.newHashSet()).add(row.get("字段"));
                // 表格追加
                table.add(Lists.newArrayList(row.values()));
            });
        }
        // 导出还需要处理的excel
        QueryDBHelper.exportExcel(headers, table, "还需要处理的校验");
        Map<String, Set<String>> distinctSortMap = distinctErrorReportsMap.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
        System.out.println("");
        // 读取解决方案excel
        ExcelData excelData = readExcelData("处理方式.xlsx");
        if (excelData == null) {
            return;
        }
        // 有解决方法的错误
        List<String> errorHandleMethods = excelData.getSheetRowsMap().get("Sheet1").stream().map(row -> String.format("%s|%s|%s", row.get("错误"), row.get("模块"), row.get("字段"))).collect(Collectors.toList());
        // 处理结果
        Map<Boolean, Map<String, Set<String>>> handleMap = Maps.newHashMap();
        // 判断问题是否有处理方式了
        distinctSortMap.forEach((error, errorReports) -> handleMap.computeIfAbsent(errorHandleMethods.contains(error), v -> Maps.newHashMap()).put(error, errorReports));
        if (!handleMap.containsKey(true)) {
            log.info("没有可以处理的");
            return;
        }
        List<String> fixSql = Lists.newArrayList();
        // 排序
        xianShenBases.sort(Comparator.comparing(XianShenBase::getOrder));
        // 循环处理
        xianShenBases.forEach(fix -> {
            // 需要修复的报告
            Set<String> reportNos = handleMap.get(true).get(fix.fixField());
            if (CollectionUtils.isEmpty(reportNos)) {
                log.info("[{}]没有需要修复的报告", fix.fixField());
            } else {
                log.info("[{}]修复开始", fix.fixField());
                List<String> errorFixSql = fix.fixData(req, xianShenReportNoDao.findByReportNos(reportNos), restTemplate);
                fixSql.addAll(errorFixSql);
                log.info("[{}]修复完成", fix.fixField());
            }
        });
        System.out.println();
    }

    @ApiOperation("本地化修改nacos配置")
    @GetMapping(value = "test")
    public void test() {
        Map<String, List<String>> v4v5ServicesMap = Maps.newLinkedHashMap();
        v4v5ServicesMap.put("v4", v4_services);
        v4v5ServicesMap.put("v5", V5_SERVICES);
        Map<String, String> result = Maps.newLinkedHashMap();
        v4v5ServicesMap.forEach((v, vServices) -> {
            // 查询配置
            Map<String, String> servicePropertiesMap = vServices.stream().collect(Collectors.toMap(service -> service, this::getStr, (o, n) -> n, LinkedHashMap::new));
            replacePropertiesValue(servicePropertiesMap, result, v);
        });
        System.out.println();
    }

    @ApiOperation("修复meddraVersion")
    @PostMapping(value = "fixMeddraVersion")
    public void fixMeddraVersion(@RequestBody QueryDBReq req) {
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report");
        req.setLimitNum("0");
        // 租户id
        String tenantId = "ce5faba59c92474bb583a7940009e225";
        // 查询租户下没有meddra_version的数据
        String queryMeddraSql = String.format("select * from meddra_field_info where tenant_id = '%s' and (meddra_version is null or meddra_version = '') order by pt_code, llt_code, hlt_code, hlgt_code, soc_code;", tenantId);
        // 设置查询条件
        req.setSqlContent(queryMeddraSql);
        // 查询数据
        D res = QueryDBHelper.getRes(req, restTemplate);
        // 获取列信息
        List<String> extractColumns = Lists.newArrayList("id", "pt_code", "llt_code", "hlt_code", "hlgt_code", "soc_code");
        // 提取数据
        List<Map<String, String>> rowValues = QueryDBHelper.extractColumnValues(res, extractColumns);
        // 分组一下
        Map<String, List<String>> uniqueMap = Maps.newHashMap();
        rowValues.forEach(row -> {
            String unique = String.format("%s|%s|%s|%s|%s", row.get("pt_code"), row.get("llt_code"), row.get("hlt_code"), row.get("hlgt_code"), row.get("soc_code"));
            // 追加id
            uniqueMap.computeIfAbsent(unique, v -> Lists.newArrayList()).add(row.get("id"));
        });
        List<String> fixSql = Lists.newArrayList();
        // 循环,查出来当前数据库中存在的
        uniqueMap.forEach((unique, ids) -> {
            String[] split = unique.split("\\|");
            // 查询版本号的SQL
            String queryMeddraVersionSql = String.format("select meddra_version from meddra_field_info where tenant_id = '%s' and (meddra_version is not null and meddra_version != '') and pt_code = '%s' and llt_code = '%s' and hlt_code = '%s' and hlgt_code = '%s' and soc_code = '%s' order by create_time desc limit 1;", tenantId, split[0], split[1], split[2], split[3], split[4]);
            req.setSqlContent(queryMeddraVersionSql);
            // 查询
            String meddraVersion = QueryDBHelper.getRes(req, restTemplate).getData().getRows().get(0).get(0);
            if (StringUtils.isBlank(meddraVersion)) {
                System.out.println("数据库下都没有");
            }
            System.out.println(unique + "==对应版本号==" + meddraVersion);
            // 更新SQL
            ids.forEach(id -> fixSql.add(String.format("update meddra_field_info set meddra_version = '%s' where id = '%s';", meddraVersion, id)));
        });
        // 导出建表SQL
        newFile(("C:\\Users\\shunjian.hu\\Desktop" + "\\" + "fixMeddraVersion.sql"), String.join("\n", fixSql));
    }

    @ApiOperation("pv数据库校验")
    @PostMapping(value = "doPvDbReportCheck")
    public void doPvDbReportCheck() {
        List<String> reportIds = Lists.newArrayList(
                "8a8d800d9729399301972a32cc3670d8",
                "8a8d800d9729399301973544df5a786b",
                "8a8d800d97293993019739978faf41e7",
                "8a8d800d9729399301973df6b0e40b17",
                "8a8d800d9729399301973e1327887651",
                "8a8d800d9729399301973f1373e47122",
                "8a8d800d97293993019743ea071b0821",
                "8a8d800d9729399301974493d9882770",
                "8a8d800d972939930197449be5743ddb",
                "8a8d800d97293993019748ddfe1366a6",
                "8a8d800d97293993019752a0a6276c54",
                "8a8d800d97293993019752b94a971b80",
                "8a8d800d97293993019752ba851a20a9",
                "8a8d800d97293993019753b02a735278",
                "8a8d800d97293993019757d011b071b9",
                "8a8d800d972939930197588ad53b581c",
                "8a8d800d9729399301975cfe0bc97fe1",
                "8a8d800d9729399301975daac1e60b79",
                "8a8d800d972939930197623f16980643",
                "8a8d800d97293993019763f7c59f5d35",
                "8a8d800d9729399301976895cc1a3f8c",
                "8a8d800d972939930197689f05ff4db9",
                "8a8d800d97293993019768bde5f111ea",
                "8a8d800d97293993019771b1c2bd2a48",
                "8a8d800d97293993019771c7d5692fad",
                "8a8d800d972939930197764e7c331abd",
                "8a8d800d97293993019776a110a6192f",
                "8a8d800d97293993019776d7741624fe",
                "8a8d800d97293993019777be305b3b2e",
                "8a8d800d9729399301977bb3db7f6602",
                "8a8d800d9729399301977bbcf0a2138c",
                "8a8d800d9729399301977e87b09b6104",
                "8a8d800d972939930197825da6f218ab",
                "8a8d800d97293993019782a98c2f7c4a",
                "8a8d800d97293993019782ab0fe902db",
                "8a8d800d97293993019786344da16f30",
                "8a8d800d9729399301978696b53e7a6e",
                "8a8d800d97293993019786a98eaa25ad",
                "8a8d800d97293993019786c1c0f8092a",
                "8a8d800d972939930197874cd5d56ef4",
                "8a8d800d9729399301978787199f21b3",
                "8a8d800d9729399301978b150d884f20",
                "8a8d800d9729399301978b66d2f93e9f",
                "8a8d800d9729399301978b67930f4494",
                "8a8d800d9729399301978bc4ce354237",
                "8a8d800d9729399301978dc19b857a0c",
                "8a8d800d9729399301979a95456c1e68",
                "8a8d800d9729399301979bd05eb246d9",
                "8a8d800d9729399301979bd4a98c5155",
                "8a8d800d972939930197a0b2ce7f1941",
                "8a8d800d972939930197a513cc545a7c",
                "8a8d800d972939930197a676f6d15d27",
                "8a8d800d972939930197a6bb24c30d70",
                "8a8d800d972939930197a6fb1c2f4cfe",
                "8a8d818296d3a2700196eb846dfc2986",
                "8a8d818296d3a2700196ecbfaffc45cd",
                "8a8d818296d3a2700196ecf8ab234686",
                "8a8d818296d3a2700196ed0810ea03c9",
                "8a8d818296d3a2700196ed82b42a6b32",
                "8a8d818296d3a2700196f0c8214762ad",
                "8a8d818296d3a2700196f10e2c5f11d9",
                "8a8d818296d3a2700196f2b76b670930",
                "8a8d818296d3a2700196f6d2a9c335eb",
                "8a8d818296d3a2700196f7b1cabd6d86",
                "8a8d818296d3a2700196f81479711257",
                "8a8d818296d3a2700196f84221b624b4",
                "8a8d818296d3a2700196fb13c3c928ba",
                "8a8d818296d3a27001970f241fe76cec",
                "8a8d818296d3a27001970f272b5875de",
                "8a8d818296d3a27001970f2a745e0177",
                "8a8d818296d3a27001970f5bf5a86764",
                "8a8d818296d3a27001971aa76ea02bd6",
                "8a8d818296d3a27001971b0c96de56ac",
                "8a8d818296d3a27001971e8bcc0f34dd",
                "8a8d818296d3a270019734b4628347d2",
                "8a8d818296d3a270019734b4dfa94cb9",
                "8a8d818296d3a270019738886caf37a9",
                "8a8d818296d3a27001973dacfea31b37",
                "8a8d818296d3a270019743377507707b",
                "8a8d818296d3a2700197449426d833cc",
                "8a8d818296d3a270019753197efc53ac",
                "8a8d818296d3a27001975784219345f1",
                "8a8d818296d3a270019757a6775520b9",
                "8a8d818296d3a27001975d78f60964a6",
                "8a8d818296d3a27001975d7ad2d36e6f",
                "8a8d818296d3a27001975eba50d8624a",
                "8a8d818296d3a270019764205b90472e",
                "8a8d818296d3a2700197688025dc16fb",
                "8a8d818296d3a270019781b515fd0d7f",
                "8a8d818296d3a2700197827469c12ea3",
                "8a8d818296d3a27001979ba9b7fa4626",
                "8a8d81fd96d39f860196d839dd462277",
                "8a8d81fd96d39f860196ecc558427908",
                "8a8d81fd96d39f860196f12d07655f65",
                "8a8d81fd96d39f860196f5962a157f73",
                "8a8d81fd96d39f860196fac2be0e1911",
                "8a8d81fd96d39f860196fc2680d142bf",
                "8a8d81fd96d39f86019709ec82a21786",
                "8a8d81fd96d39f8601970ab656975ce9",
                "8a8d81fd96d39f8601971f5b72c4286b",
                "8a8d81fd96d39f86019720b6a0cd32bc",
                "8a8d81fd96d39f8601972e6903475b52",
                "8a8d81fd96d39f86019733ecfaa9411d",
                "8a8d81fd96d39f860197388cb107657f",
                "8a8d81fd96d39f86019738a07bb26d6f",
                "8a8d81fd96d39f8601973dde37eb64b1",
                "8a8d81fd96d39f8601973efe511a2ad4",
                "8a8d81fd96d39f86019744d065db483c",
                "8a8d81fd96d39f8601975407d847777c",
                "8a8d81fd96d39f86019761e515867fef",
                "8a8d81fd96d39f860197770ee2f32237",
                "8a8d81fd96d39f86019786fa675049e8",
                "8a8d81fd96d39f8601979ae281603cc9",
                "8a8d81fd96d39f860197aa7707934ba1",
                "8a8d82eb95b3e5780196d484b8166c66",
                "8a8d82eb95b3e5780196d7ed13973ac9",
                "8a8d82eb95b3e5780196d8a7afda5c6b",
                "8a8d82eb95b3e5780196e6a0e2b37fd5",
                "8a8d82eb95b3e5780196e74cc601512e",
                "8a8d82eb95b3e5780196e76e925c7a10",
                "8a8d82eb95b3e5780196e846cbc378bd",
                "8a8d82eb95b3e5780196edf0d56b7cf9",
                "8a8d82eb95b3e5780196f35ca6dd3742",
                "8a8d82eb95b3e5780196f5f2d45427bd",
                "8a8d82eb95b3e5780196f79399ec44cc",
                "8a8d82eb95b3e5780196facaaa4d4186",
                "8a8d82eb95b3e5780196fc3a66de2312",
                "8a8d82eb95b3e5780196fc4f082908a4",
                "8a8d82eb95b3e5780197028ad6467f62",
                "8a8d82eb95b3e57801970aae51b74926",
                "8a8d82eb95b3e57801970b2de92a5e10",
                "8a8d82eb95b3e578019710e1c4572ceb",
                "8a8d82eb95b3e57801971619c41a5ae4",
                "8a8d82eb95b3e57801972052772e7af7",
                "8a8d82eb95b3e578019724d25d4b668b",
                "8a8d82f795b3e5580196d19c1acd4734",
                "8a8d82f795b3e5580196d1d6b03e05e9",
                "8a8d82f795b3e5580196d2054f0d6884",
                "8a8d82f795b3e5580196d36574671718",
                "8a8d82f795b3e5580196dd0c525f73d8",
                "8a8d82f795b3e5580196dd68a36516bd",
                "8a8d82f795b3e5580196de31e3ce3eb5",
                "8a8d82f795b3e5580196e878b2841bad",
                "8a8d82f795b3e5580196ebb23c2a0dcb",
                "8a8d82f795b3e5580196ebbd82f93a89",
                "8a8d82f795b3e5580196f21edd9d61c5",
                "8a8d82f795b3e5580196f71ebbb20c7b",
                "8a8d82f795b3e5580196fbc39ea04116",
                "8a8d82f795b3e5580196fca7692d6931",
                "8a8d82f795b3e558019702a834f22855",
                "8a8d82f795b3e558019707e5def55b8c",
                "8a8d82f795b3e55801970ce7e46309d6",
                "8a8d82f795b3e55801970f5bf39e0276",
                "8a8d82f795b3e5580197159bd932533c",
                "8a8d82f795b3e558019717141e4825df",
                "8a8d82f795b3e558019719ff86183a21",
                "8a8d82f795b3e55801971ab96c8f4a52",
                "8a8d82f795b3e55801971be994ad3620",
                "8a8d82f795b3e55801971bfc799057f9",
                "8a8d82f795b3e5580197201a1aa075c1",
                "8a8d82f795b3e558019720bf43023cb4",
                "8a8dbf2a9729365101972974eb6a4012",
                "8a8dbf2a9729365101972a8ff2775fc5",
                "8a8dbf2a972936510197338290d451b2",
                "8a8dbf2a9729365101973489aacc6e4a",
                "8a8dbf2a9729365101973499165333e8",
                "8a8dbf2a9729365101973a5909b72c13",
                "8a8dbf2a9729365101973b766ffb23fd",
                "8a8dbf2a9729365101973de183db22f1",
                "8a8dbf2a9729365101973de5e5bd2cb0",
                "8a8dbf2a9729365101973f4be45f33ff",
                "8a8dbf2a972936510197434868e86b19",
                "8a8dbf2a97293651019744b857c478ed",
                "8a8dbf2a972936510197452612833d9f",
                "8a8dbf2a972936510197549e643c583a",
                "8a8dbf2a97293651019754a0762d5b29",
                "8a8dbf2a97293651019757cecd3a64a0",
                "8a8dbf2a9729365101975c9f16244c79",
                "8a8dbf2a97293651019762c04cd60f2e",
                "8a8dbf2a9729365101976391b5d30f77",
                "8a8dbf2a97293651019764bd60a3093f",
                "8a8dbf2a97293651019764c8ead30bf5",
                "8a8dbf2a9729365101976726566b6590",
                "8a8dbf2a972936510197672c248e00b7",
                "8a8dbf2a97293651019768966906705e",
                "8a8dbf2a972936510197766c56b001c6",
                "8a8dbf2a972936510197783a36b43fed",
                "8a8dbf2a9729365101977bb0fff51756",
                "8a8dbf2a9729365101977bc0e8775749",
                "8a8dbf2a9729365101977bed261c03a1",
                "8a8dbf2a97293651019781130e4c1eac",
                "8a8dbf2a97293651019781226223662e",
                "8a8dbf2a972936510197826f686d4c8f",
                "8a8dbf2a97293651019785fb1aad2533",
                "8a8dbf2a97293651019786d3ad472a6c",
                "8a8dbf2a97293651019786fb2f9f3940",
                "8a8dbf2a972936510197887537824837",
                "8a8dbf2a9729365101978cd5229049b3",
                "8a8dbf2a972936510197908d54d81ff9",
                "8a8dbf2a972936510197913588403ead",
                "8a8dbf2a9729365101979be9f0b752e8",
                "8a8dbf2a972936510197a4f486206bf9",
                "8a8dbf2a972936510197a4fadb3b2075",
                "8a8dbf2a972936510197a50731246846",
                "8a8dbf2a972936510197a515fe4f0d50",
                "8a8dbf2a972936510197a7449c575d5d",
                "8a8dbf2a972936510197ab3faca609a5"
        );
        String cookie = "gr_user_id=8065e17c-9fe0-4464-be05-cc8651fce673; b322f7b164f88f10_gr_last_sent_cs1=8a81c08a7965a66b01798247b8c04c33; b322f7b164f88f10_gr_cs1=8a81c08a7965a66b01798247b8c04c33; eSafety5_unblind_warn=true; Hm_lvt_88897af8840c3689db7cb8c0886b1026=1748600945,1750993244; HMACCOUNT=04F024FD93C91795; acw_tc=0a472f8917510045153796299e006b1d144827203e3a46d570a5dece56fe15; Hm_lpvt_88897af8840c3689db7cb8c0886b1026=1751005138; token=abcdd2e3aba14bd5be92d2e50ca19421";
        Map<String, String> result = QueryDBHelper.getDbReportCheckError(restTemplate, cookie, reportIds);
        /*for (int i = 0; i < reportIds.size(); i++) {
            String reportId = reportIds.get(i);
            result.putAll(QueryDBHelper.getDbReportCheckError(restTemplate, cookie, reportId));
            log.info("处理进度:[{}-{}]", i, reportIds.size());
        }*/
        System.out.println();
    }

    @ApiOperation("genReportExcel")
    @PostMapping(value = "genReportExcel")
    public void genReportExcel(@RequestBody QueryDBReq req) {
        // 用文件信息的
        Map<String, String> errorMap = req.getFileInfoMap();
        // 查询报告信息
        String sql = String.format("select id, IF(versions is null or versions = '', Safetyreportid, CONCAT(Safetyreportid, '-', versions)) as report_no from report_value where id in ('%s');", String.join("','", errorMap.keySet()));
        req.setSqlContent(sql);
        req.setInstanceName("prod-mysql-pv-ro");
        req.setDbName("pv");
        req.setLimitNum("0");
        req.setCookie(QueryDBHelper.DB_COOKIE);
        req.setCsrfToken(QueryDBHelper.DB_CSRF_TOKEN);
        // 查询
        List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "report_no"));
        // 转成报告信息
        Map<String, String> reportNoMap = Maps.newHashMap();
        rows.forEach(row -> {
            String id = row.get("id");
            String reportNo = row.get("report_no");
            reportNoMap.put(id, reportNo);
        });
        List<String> headers = Lists.newArrayList("报告编号", "错误信息");
        List<List<String>> table = Lists.newArrayList();
        // 组装数据
        errorMap.forEach((reportId, error) -> {
            if (!StringUtils.equals(error, "查询失败")) {
                table.add(Lists.newArrayList(reportNoMap.get(reportId), error));
            }
        });
        QueryDBHelper.exportExcel2(headers, table, "报告校验信息");
    }


    @ApiOperation("导出线上数据到本地,传入搜索条件")
    @PostMapping(value = "test3")
    public void test3(@RequestBody QueryDBReq req) {
        initCellMaxTextLength();
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println();
        QueryDBHelper.exportExcel(res.getData(), req.getFileName());
    }

    @ApiOperation("导出先声报告数据")
    @PostMapping(value = "exportXianShenReport")
    public void exportXianShenReport(@RequestBody QueryDBReq req) {
        req.setLimitNum("0");
        initCellMaxTextLength();
        // count
        String countSql = "select count(1) from report_value where tenant_id = 'af632df0-3999-4a35-9c2d-7269b4ecc6a0' and newest_version = 1 and id not in (select ReportId from foreigntaskdtl where tenant_id = 'af632df0-3999-4a35-9c2d-7269b4ecc6a0') and is_deleted = 0";
        req.setSqlContent(countSql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        // 查询总数
        Integer count = Integer.valueOf(res.getData().getRows().get(0).get(0));
        // 头
        List<String> column_list = Lists.newArrayList();
        // 数据
        List<List<String>> rows = Lists.newArrayList();
        // 总页数
        int totalPage = totalPage(count, 5000);;
        // 分页查询
        for (int i = 0; i < totalPage; i++) {
            // 导出当前页数据
            String querySql = String.format("select id, IF(versions is null or versions = '', Safetyreportid, CONCAT(Safetyreportid, '-', versions)) as report_no,classify_of_report,ReceivedFromId as received_from_id, reportnullificationamendment as invalid_fix, createnewversionreason as 'invalid_fix_reason', fulfillexpeditecriteria as 'accelerate_report',SourceInfoId as 'source_info_id', worldwideuniquenumber as 'world_unique_num', mah_id, firstreceivedreportdate as 'first_received_date', Report_Receive_Date as 'report_receive_date'   from report_value where tenant_id = 'af632df0-3999-4a35-9c2d-7269b4ecc6a0' and newest_version = 1 and id not in (select ReportId from foreigntaskdtl where tenant_id = 'af632df0-3999-4a35-9c2d-7269b4ecc6a0') and is_deleted = 0 limit %d, %d;", i * 5000, 5000);
            req.setSqlContent(querySql);
            D queryRes = QueryDBHelper.getRes(req, restTemplate);
            column_list = queryRes.getData().getColumn_list();
            rows.addAll(queryRes.getData().getRows());
        }
        QueryDBHelper.exportExcel(new DData(column_list, rows), req.getFileName());
    }

    @ApiOperation("导出线上数据到本地,写代码")
    @PostMapping(value = "exportData")
    public void exportData(@RequestBody QueryDBReq req) {
        // 导出最大
        req.setLimitNum("0");
        // 需要导出的库和表
        Map<String, List<String>> dbTablesMap = Maps.newHashMap();
        //dbTablesMap.put("esafety5_e2b", Lists.newArrayList("e2b_r3_estri_item_config", "e2b_r3_m2estri_config"));
        //dbTablesMap.put("pvs_middle_data", Lists.newArrayList("items", "item_class"));
        dbTablesMap.put("pvs_common", Lists.newArrayList("email_template"));
        dbTablesMap.forEach((db, tables) -> tables.forEach(table -> {
            // 设置库
            req.setDbName(db);
            // 设置查询条件
            req.setSqlContent(String.format("select * from %s", table));
            // 设置文件名
            req.setFileName(table);
            test3(req);
        }));
    }


    @Deprecated
    @ApiOperation("test4")
    @PostMapping(value = "test4")
    public void test4(@RequestBody QueryDBReq req) {
        // 查询5.0的所有库下的所有建表语句
        Map<String, List<String>> dbMap = Maps.newHashMap();
        // 5.0数据库
        //dbMap.put("prod-mysql-pvcaps-ro", Lists.newArrayList("caps", "dsm", "esafety5_e2b", "esafety_web", "gvp", "gvp_workbench", "pvs_convert", "pvs_middle_data", "pvs_migration", "pvs_report", "pvs_submit_esafety", "pvs_task"));
        // common数据库
        //dbMap.put("prod-mysql-pvscommon-ro", Lists.newArrayList("pvs_common"));
        // 测试
        dbMap.put("prod-mysql-pvcaps-ro", Lists.newArrayList("pvs_submit_esafety"));
        // 每一个库下的建表语句
        Map<String, List<String>> instanceDBCreateTableSqlMap = Maps.newHashMap();
        // 配置数据库下的建表语句
        Map<String, List<String>> configTableInsertSqlMap = Maps.newHashMap();
        // 是否是配置表
        List<String> configTables = Lists.newArrayList("regulatory");
        // 循环实例
        dbMap.forEach((instanceName, instanceDbs) -> {
            req.setInstanceName(instanceName);
            instanceDbs.forEach(instanceDb -> {
                // 循环库
                req.setDbName(instanceDb);
                // 输出所有的表
                String showTablesSql = "show tables";
                req.setSqlContent(showTablesSql);
                // 查询
                D res = QueryDBHelper.getRes(req, restTemplate);;
                // 输出所有的表
                List<List<String>> allTables = res.getData().getRows();
                // 然后循环每一个表,输出建表语句
                allTables.forEach(tables -> {
                    String table = tables.get(0);
                    // 输出这个表的建表语句
                    String showCreateTableSql = "SHOW CREATE TABLE " + table;
                    req.setSqlContent(showCreateTableSql);
                    // 查询
                    D res1 = QueryDBHelper.getRes(req, restTemplate);
                    List<List<String>> createTables = res1.getData().getRows();
                    System.out.println(createTables.get(0).get(1));
                    instanceDBCreateTableSqlMap.computeIfAbsent(instanceDb, v -> Lists.newArrayList()).add(createTables.get(0).get(1));
                    // 判断这个表是不是配置表
                    if (configTables.contains(table)) {
                        // 输出这个表所有的列
                        String showColumnSql = String.format("SELECT column_name FROM information_schema.columns WHERE table_name = '%s'", table);
                        req.setSqlContent(showColumnSql);
                        D res2 = QueryDBHelper.getRes(req, restTemplate);
                        // 列
                        List<String> columns = Lists.newArrayList();
                        res2.getData().getRows().forEach(resColumns -> columns.add(resColumns.get(0)));
                        String sql = String.format("CONCAT(\"INSERT INTO `%s`(`%s`) VALUES ('\", %s ,\"');\")", table, String.join("`, `", columns),
                                columns.stream().map(column -> String.format("IFNULL(%s,'')", column)).collect(Collectors.joining(", \"', '\", ")));
                        String configSql = String.format("SELECT %s FROM %s WHERE tenant_id IN ('default', 'system')", sql, table);
                        // 查询数据
                        req.setSqlContent(configSql);
                        D res3 = QueryDBHelper.getRes(req, restTemplate);
                        List<String> configTableInsertSql = Lists.newArrayList();
                        res3.getData().getRows().forEach(row -> configTableInsertSql.add(row.get(1)));
                        configTableInsertSqlMap.put(table, configTableInsertSql);
                    }
                });
            });
        });
        System.out.println();
    }

    private Map<String, List<String>> dbMap() {
        // 需要处理的数据库
        Map<String, List<String>> dbMap = Maps.newHashMap();
        // common数据库
        dbMap.put("prod-mysql-pvscommon-ro", Lists.newArrayList("pvs_common"));
        // 5.0数据库
        dbMap.put("prod-mysql-pvcaps-ro", Lists.newArrayList(
                "caps",
                "esafety5_e2b",
                "esafety_web",
                "gvp",
                "gvp_workbench",
                "pvs_convert",
                "pvs_middle_data",
                "pvs_migration",
                "pvs_report",
                "pvs_submit_esafety",
                "pvs_task"
        ));
        return dbMap;
    }


    private List<String> IGNORE_EXPORT_TABLES = Lists.newArrayList(
            "clean_table_record",
            "trial_migration_record",
            "e2b_r2_transmission_identification",
            "e2b_r3_transmission_identification_batch",
            "e2b_r3_transmission_identification_message",
            "report_auth_drug",
            "report_auth_project",
            "signature_log",
            "signature_log_detail",
            "schema_import_history",
            "icsr_identification"
    );

    private void exportDbTables(QueryDBReq req, Map<String, Map<String, List<String>>> instanceDbTablesMap) {
        List<DbTable> all = Lists.newArrayList();
        instanceDbTablesMap.forEach((instanceName, dbs) -> dbs.forEach((db, tables) -> {
            // 设置实例
            req.setInstanceName(instanceName);
            log.info("导出数据库[{}]表结构和数据开始==================================", db);
            // 设置当前数据库
            req.setDbName(db);
            // 设置limit,设置成最大,0现在就是默认的最大
            req.setLimitNum("0");
            tables.forEach(table -> exportDbTable(table, instanceName, db, req, all));
            log.info("导出数据库[{}]表结构和数据结束==================================", db);
        }));
        // 导出
        exportFile(all);
    }

    private void exportDbTable(String table, String instanceName, String db, QueryDBReq req, List<DbTable> all) {
        log.info("处理表:[{}]", table);
        // 库表信息
        DbTable.DbTableBuilder builder = DbTable.builder().instanceName(instanceName).db(db).tableName(table);
        // 建表语句
        String createTableSql = showCreateTableSql(req, table);
        builder.createTableSql(createTableSql);
        // 判断是否需要导出数据
        if (needExportData(req, table, createTableSql)) {
            // 查询数据
            Pair<List<List<String>>, List<List<String>>> pair = findSystemData(req, table, createTableSql);
            // 导出数据
            builder.tableExcelData(pair);
        }
        all.add(builder.build());
    }

    private void exportFile(List<DbTable> all) {
        String path = "C:\\Users\\shunjian.hu\\Desktop\\" + (new Date()).getTime();
        // 创建一个临时文件夹
        mkdir(path);
        // 生成导出数据,按照实例分组
        all.stream().collect(Collectors.groupingBy(DbTable::getInstanceName)).forEach((instanceName, dbs) -> {
            // 在文件夹下在创建实例文件夹
            String instanceNamePath = path + "\\" + instanceName;
            // 创建实例文件夹
            mkdir(instanceNamePath);
            // 分组数据库实例
            dbs.stream().collect(Collectors.groupingBy(DbTable::getDb)).forEach((db, dbTables) -> {
                String dbPath = instanceNamePath + "\\" + db;
                // 循环每一个数据库实例,创建文件夹
                mkdir(dbPath);
                // 所有的建表语句
                Map<String, String> createTableSqlMap = Maps.newLinkedHashMap();
                // 导入的excel数据
                Map<String, Pair<List<List<String>>, List<List<String>>>> exportDataMap = Maps.newLinkedHashMap();
                // 循环每一个数据库
                for (DbTable dbTable : dbTables) {
                    // 建表语句
                    createTableSqlMap.put(dbTable.getTableName(), dbTable.getCreateTableSql());
                    // 导出数据
                    if (dbTable.getTableExcelData() != null) {
                        exportDataMap.put(dbTable.getTableName(), dbTable.getTableExcelData());
                    }
                }
                // Sheet1为SQL语句
                exportDataMap.put("Sheet1", createTableSql(createTableSqlMap));
                // 导出excel数据
                QueryDBHelper.exportExcel(exportDataMap, dbPath, db);
                // 导出建表SQL
                newFile((dbPath + "\\" + "createTable.sql"), String.join(";\n", createTableSqlMap.values()));
            });
        });
        log.info("导出文件结束");
    }

    private void newFile(String filePath, String fileContent) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // 写入文本内容到文件，如果文件不存在则会创建新的文件
            writer.write(fileContent);
        } catch (IOException e) {
            System.out.println("发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @ApiOperation("导出线上表结构和数据,自己写的")
    @PostMapping(value = "exportProdTableAndData")
    public void exportProdTableAndData(@RequestBody QueryDBReq req) {
        initCellMaxTextLength();
        // 配置数据
        Map<String, Map<String, List<String>>> instanceDbTablesMap = Maps.newHashMap();
        //
        instanceDbTablesMap.computeIfAbsent("prod-mysql-pvcaps-ro", v -> Maps.newHashMap()).computeIfAbsent("esafety5_e2b", v -> Lists.newArrayList()).add("e2b_task");
        instanceDbTablesMap.computeIfAbsent("prod-mysql-pvcaps-ro", v -> Maps.newHashMap()).computeIfAbsent("esafety5_e2b", v -> Lists.newArrayList()).add("e2b_task_detail");
        //instanceDbTablesMap.computeIfAbsent("prod-mysql-pvcaps-ro", v -> Maps.newHashMap()).computeIfAbsent("pvs_middle_data", v -> Lists.newArrayList()).add("items");
        //instanceDbTablesMap.computeIfAbsent("prod-mysql-pvcaps-ro", v -> Maps.newHashMap()).computeIfAbsent("pvs_middle_data", v -> Lists.newArrayList()).add("item_class");
        // instanceDbTablesMap.computeIfAbsent("prod-mysql-pvscommon-ro", v -> Maps.newHashMap()).computeIfAbsent("pvs_common", v -> Lists.newArrayList()).add("email_template");
        exportDbTables(req, instanceDbTablesMap);
    }

    @ApiOperation("获取线上所有的表的建表语句")
    @PostMapping(value = "getAllTables")
    public void getAllTables(@RequestBody QueryDBReq req) {
        req.setCookie(QueryDBHelper.DB_COOKIE);
        req.setCsrfToken(QueryDBHelper.DB_CSRF_TOKEN);
        // 需要处理的数据库
        Map<String, List<String>> dbMap = Maps.newHashMap();
        dbMap.put("prod-mysql-pvcaps-ro", Lists.newArrayList("pvs_report_all", "gvp_workbench"));
        // 实例下所有的表和建表语句
        Map<String, Map<String, Map<String, String>>> instanceCreateTableSqlMap = Maps.newHashMap();
        dbMap.forEach((instanceName, dbs) -> dbs.forEach(db -> {
            // 设置实例
            req.setInstanceName(instanceName);
            log.info("导出数据库[{}]表结构和数据开始==================================", db);
            // 设置当前数据库
            req.setDbName(db);
            // 设置limit,设置成最大,0现在就是默认的最大
            req.setLimitNum("0");
            // 获取数据库下所有的表
            List<String> dbTables = showAllTables(req);
            // 输出所有的表的建表语句
            instanceCreateTableSqlMap.computeIfAbsent(instanceName, v -> Maps.newHashMap())
                    .computeIfAbsent(db, v -> Maps.newHashMap())
                    .putAll(dbTables.stream().collect(Collectors.toMap(table -> table, table -> showCreateTableSql(req, table))));
            log.info("导出数据库[{}]表结构和数据结束==================================", db);
        }));
        String tenantId = "4028e4be6ae84aeb016ae84aeb300000";
        // 判断建表语句里包含租户id的
        Map<String, Map<String, Map<String, List<String>>>> hasTenantIdTableMap = Maps.newHashMap();
        List<String> deleteSql = Lists.newArrayList();
        instanceCreateTableSqlMap.forEach((instanceName, dbTableInfo) -> dbTableInfo.forEach((db, tableInfos) -> tableInfos.forEach((table, createTableSql) -> {
            if (StringUtils.equals(table, "signal_report_snapshoot")) {
                // 信号报告表单独处理
                // 先查询信号
                String querySignalDetailIdSql = String.format("select id from signal_statistics_detail where tenant_id = '%s';", tenantId);
                String countSignalDetailIdSql = String.format("select count(1) from signal_statistics_detail where tenant_id = '%s';", tenantId);
                req.setInstanceName(instanceName);
                req.setDbName(db);
                // 查询所有数据
                List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(getAllData(req, countSignalDetailIdSql, querySignalDetailIdSql), Lists.newArrayList("id"));
                // 循环处理
                ListUtils.partition(rows, 200).forEach(part -> {
                    String ids = part.stream().map(row -> row.get("id")).collect(Collectors.joining("','"));
                    // 删除SQL
                    deleteSql.add(String.format("delete from signal_report_snapshoot where signal_statistics_detail_id in ('%s')", ids));
                });
            } else {
                if (StringUtils.contains(createTableSql, "`tenant_id`")) {
                    req.setInstanceName(instanceName);
                    req.setDbName(db);
                    String countSql = String.format("select count(1) from %s where tenant_id = '%s'", table, tenantId);
                    req.setSqlContent(countSql);
                    Integer count = countSql(req);
                    if (count != 0) {
                        hasTenantIdTableMap.computeIfAbsent(instanceName, v -> Maps.newHashMap())
                                .computeIfAbsent(db, v -> Maps.newHashMap())
                                .computeIfAbsent("tenantId", v -> Lists.newArrayList()).add(table);
                        // 删除SQL
                        deleteSql.add(String.format("delete from %s.%s where tenant_id = '%s'", db, table, tenantId));
                    }
                } else if (StringUtils.contains(createTableSql, "`company_id`")) {
                    req.setInstanceName(instanceName);
                    req.setDbName(db);
                    String countSql = String.format("select count(1) from %s where company_id = '%s'", table, tenantId);
                    req.setSqlContent(countSql);
                    Integer count = countSql(req);
                    if (count != 0) {
                        hasTenantIdTableMap.computeIfAbsent(instanceName, v -> Maps.newHashMap())
                                .computeIfAbsent(db, v -> Maps.newHashMap())
                                .computeIfAbsent("companyId", v -> Lists.newArrayList()).add(table);
                        // 删除SQL
                        deleteSql.add(String.format("delete from %s.%s where company_id = '%s'", db, table, tenantId));
                    }
                }
            }
        })));
        System.out.println();
    }

    @ApiOperation("导出线上表结构和数据,所有的")
    @PostMapping(value = "exportProdTable")
    public void exportProdTable(@RequestBody QueryDBReq req) {
        // 放到最大
        initCellMaxTextLength();
        // 需要处理的数据库
        Map<String, List<String>> dbMap = Maps.newHashMap();
        // 默认的
        // dbMap.putAll(dbMap());
        // dbMap.put("prod-mysql-pvscommon-ro", Lists.newArrayList("pvs_common"));
        // dbMap.put("prod-mysql-pvcaps-ro", Lists.newArrayList("pvs_middle_data"));
        dbMap.put("prod-mysql-pvcaps-ro", Lists.newArrayList("pvs_report"));
        // 所有导出的数据
        List<DbTable> all = Lists.newArrayList();
        // 循环实例,处理实例下所有的数据库
        dbMap.forEach((instanceName, dbs) -> dbs.forEach(db -> {
            // 设置实例
            req.setInstanceName(instanceName);
            log.info("导出数据库[{}]表结构和数据开始==================================", db);
            // 设置当前数据库
            req.setDbName(db);
            // 设置limit,设置成最大,0现在就是默认的最大
            req.setLimitNum("0");
            // 获取数据库下所有的表
            List<String> dbTables = showAllTables(req);
            // 循环所有的表,输出建表语句和表里面的初始化数据
            dbTables.forEach(table -> exportDbTable(table, instanceName, db, req, all));
            log.info("导出数据库[{}]表结构和数据结束==================================", db);
        }));
        List<String> tables = Lists.newArrayList();
        // 导出文件
        all.stream().filter(table -> StringUtils.containsAny(table.getCreateTableSql(), "tenant_id", "company_id")).forEach(table -> {
            System.out.println(table.getTableName());
            tables.add(table.getTableName());
        });
        //exportFile(all);
        System.out.println();
        log.info("处理完成");
    }

    private Pair<List<List<String>>, List<List<String>>> createTableSql(Map<String, String> createTableSqlMap) {
        // 建表语句标题
        List<List<String>> headers = Lists.newArrayList("CreateTableSQL").stream().map(Lists::newArrayList).collect(Collectors.toList());
        // 建表语句行
        List<String> tableSql = Lists.newArrayList();
        createTableSqlMap.forEach((table, tableCreateSql) -> tableSql.add(tableCreateSql + ";"));
        List<List<String>> table = tableSql.stream().map(Lists::newArrayList).collect(Collectors.toList());
        return Pair.of(headers, table);
    }

    private File mkdir(String folderPath) {
        File file = new File(folderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private Pair<List<List<String>>, List<List<String>>> findSystemData(QueryDBReq req, String tableName, String createTableSql) {
        String tenantColumn = null;
        if (StringUtils.contains(createTableSql, "`tenant_id`")) {
            // 包含tenant_id
            tenantColumn = "tenant_id";
        } else if (StringUtils.contains(createTableSql, "`company_id`")) {
            // 包含company_id
            tenantColumn = "company_id";
        }
        if (BooleanUtils.isTrue(req.getExportAll())) {
            // 导出全部
            tenantColumn = null;
        }
        // 页数据
        D pageSystemData = null;
        // 所有数据
        List<List<String>> allSystemData = Lists.newArrayList();
        // 查询总数
        int count = countSystemData(req, tableName, tenantColumn);
        log.info("处理表[{}]数据导出总条数:[{}]", tableName, count);
        // 总页数
        int totalPage = totalPage(count, 5000);;
        // 分页查询
        for (int i = 0; i < totalPage; i++) {
            log.info("处理表[{}]数据导出,进度:[{}-{}]", tableName, i, totalPage);
            // 当前页数据
            pageSystemData = findPageSystemData(req, tableName, tenantColumn, i * 5000, 5000);
            // 追加行数据
            allSystemData.addAll(pageSystemData.getData().getRows().stream().map(Lists::newArrayList).collect(Collectors.toList()));
        }
        if (pageSystemData == null) {
            throw new RuntimeException("查询失败");
        }
        if (createTableSql.contains("bit") && CollectionUtils.isNotEmpty(allSystemData)) {
            replaceBitValue(req, tableName, allSystemData, pageSystemData.getData().getColumn_list());
        }
        // 标题
        List<List<String>> headers = pageSystemData.getData().getColumn_list().stream().map(Lists::newArrayList).collect(Collectors.toList());
        return Pair.of(headers, allSystemData);
    }

    private void replaceBitValue(QueryDBReq req, String tableName, List<List<String>> allSystemData, List<String> columns) {
        // 判断当前表的列是不是有bit
        req.setSqlContent(String.format("SHOW COLUMNS FROM %s;", tableName));
        D showColumnsRes = QueryDBHelper.getRes(req, restTemplate);
        List<Integer> bitColumns = Lists.newArrayList();
        // 获取bit类型的字段
        showColumnsRes.getData().getRows().forEach(row -> {
            if (StringUtils.contains(row.get(1), "bit")) {
                // 获取这个字段所在下标
                bitColumns.add(columns.indexOf(row.get(0)));
            }
        });
        allSystemData.forEach(systemData -> {
            bitColumns.forEach(bitIndex -> {
                // 获取数据
                String indexValue = systemData.get(bitIndex);
                if (indexValue != null) {
                    indexValue = indexValue.equals("false") ? "0" : "1";
                }
                systemData.set(bitIndex, indexValue);
            });
        });
    }

    private static final Map<String, Pair<String, String>> TABLE_MAP = Maps.newHashMap();


    private D findPageSystemData(QueryDBReq req, String tableName, String tenantColumn, int off, int size) {
        String findSql;
        if (StringUtils.isBlank(tenantColumn)) {
            findSql = String.format("SELECT * FROM %s LIMIT %d, %d;", tableName, off, size);
        } else {
            findSql = String.format("SELECT * FROM %s WHERE %s IN ('system', 'default', 'sae-preview-system-default', 'baseCompanyId', 'System') LIMIT %d, %d;", tableName, tenantColumn, off, size);
        }
        if (TABLE_MAP.containsKey(tableName)) {
            findSql = String.format(TABLE_MAP.get(tableName).getValue(), off, size);
        }
        // 设置最大
        req.setLimitNum("5000");
        req.setSqlContent(findSql);
        // 查询
        return QueryDBHelper.getRes(req, restTemplate);
    }

    static {
        TABLE_MAP.put("scene_info", Pair.of("select count(1) from scene_info;", "SELECT * FROM scene_info LIMIT %d, %d;"));
    }

    private Integer countSystemData(QueryDBReq req, String tableName, String tenantColumn) {
        String countSql;
        if (StringUtils.isBlank(tenantColumn)) {
            countSql = String.format("SELECT count(1) FROM %s;", tableName);
        } else {
            countSql = String.format("SELECT count(1) FROM %s WHERE %s IN ('system', 'default', 'sae-preview-system-default', 'baseCompanyId', 'System');", tableName, tenantColumn);
        }
        if (TABLE_MAP.containsKey(tableName)) {
            countSql = TABLE_MAP.get(tableName).getKey();
        }
        req.setSqlContent(countSql);
        return countSql(req);
    }

    private Integer countSql(QueryDBReq req) {
        // 查询
        D res = QueryDBHelper.getRes(req, restTemplate);
        // 获取总数
        return Integer.valueOf(res.getData().getRows().get(0).get(0));
    }

    private boolean hasSystemData(QueryDBReq req, String tableName, String tenantColumn) {
        return countSystemData(req, tableName, tenantColumn) != 0;
    }



    private boolean needExportData(QueryDBReq req, String tableName, String createTableSql) {
        if (BooleanUtils.isTrue(req.getExportAll())) {
            return true;
        }
        // MQ相关的
        if (StringUtils.startsWith(tableName, "mq_message")
                // 迁移相关的
                || StringUtils.startsWith(tableName, "migration")
                // 试用租户相关的
                || StringUtils.startsWith(tableName, "trial_execute")
                // 其他忽略的
                || IGNORE_EXPORT_TABLES.contains(tableName)) {
            // mq相关的表忽略导出,迁移的表忽略导出
            return false;
        }
        if (StringUtils.contains(createTableSql, "`tenant_id`")) {
            // 包含tenant_id
            return hasSystemData(req, tableName, "tenant_id");
        } else if (StringUtils.contains(createTableSql, "`company_id`")) {
            // 包含company_id
            return hasSystemData(req, tableName, "company_id");
        } else {
            // 不包含租户字段的一定是需要导出的
            return true;
        }
    }


    private String showCreateTableSql(QueryDBReq req, String tableName) {
        // 输出这个表的建表语句
        String showCreateTableSql = "SHOW CREATE TABLE " + tableName;
        req.setSqlContent(showCreateTableSql);
        // 查询
        D res = QueryDBHelper.getRes(req, restTemplate);
        return res.getData().getRows().get(0).get(1);
    }


    private List<String> showAllTables(QueryDBReq req) {
        // 输出所有的表
        String showTablesSql = "show tables";
        req.setSqlContent(showTablesSql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        List<String> allTables = Lists.newArrayList();
        res.getData().getRows().forEach(tables -> allTables.add(tables.get(0)));
        return allTables;
    }



    @Data
    @Builder
    static class DbTable {

        private String instanceName;

        private String db;

        private String tableName;

        private String createTableSql;

        private Pair<List<List<String>>, List<List<String>>> tableExcelData;
    }





    public void initCellMaxTextLength() {
        SpreadsheetVersion[] values = SpreadsheetVersion.values();
        for (SpreadsheetVersion value : values) {
            if (value.getMaxTextLength() != Integer.MAX_VALUE) {
                Field field;
                try {
                    field = value.getClass().getDeclaredField("_maxTextLength");
                    field.setAccessible(true);
                    field.set(value, Integer.MAX_VALUE);
                } catch (Exception ex) {
                }
            }
        }
    }


    private String replacePropertiesValue(String v, String service, String source) {
        // 先判断全部替换的
        for (Map.Entry<String, String> entry : REPLACE_VALUE_MAP.entrySet()) {
            if (StringUtils.containsIgnoreCase(source, entry.getKey())) {
                String result = entry.getValue();
                System.out.println("v:" + v + ";service:" + service + ";全部替换:[原值:" + source + ";新值:" + result + "];");
                return result;
            }
        }
        // 在判断部分替换的
        Map<String, Pair<String, String>> vPartMap = V4_V5_REPLACE_PART_VALUE_MAP.get(v);
        for (Map.Entry<String, Pair<String, String>> entry : vPartMap.entrySet()) {
            Pair<String, String> pair = entry.getValue();
            if (StringUtils.containsIgnoreCase(source, entry.getKey())) {
                String result = StringUtils.replace(source, pair.getKey(), pair.getValue());
                System.out.println("v:" + v + ";service:" + service + ";部分替换:[原值:" + source + ";新值:" + result + "];");
                return result;
            }
        }
        return source;
    }

    private String getStr(String dataId) {
        String url = "http://db.taimeicloud.cn/nacos/v1/cs/configs";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("dataId", dataId);
        params.add("group", "PVS_GROUP");
        params.add("namespaceId", "PROD");
        params.add("tenant", "PROD");
        params.add("show", "all");
        params.add("accessToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2OTIxNDc4NDIsImlkZW50aXR5IjoyMTA5LCJvcmlnX2lhdCI6MTY5MTcxNTg0Miwicm9sZWtleSI6ImRldmVsb3BlciIsInN1YiI6InB2c19hZG1pbiIsInVzZXJuYW1lIjoic2h1bmppYW4uaHUifQ.Y--EzbUT_crmEgEubHep3y5_-dWkpQ3L9B8Zb7mrizk");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.queryParams(params).build().encode().toUri();

        ResponseEntity<A> forEntity = restTemplate.getForEntity(uri, A.class);
        return forEntity.getBody().getContent();
    }

    private Map<String, ExcelData> readExcelData(List<String> fileNames) {
        Map<String, ExcelData> result = Maps.newHashMap();
        fileNames.forEach(fileName -> result.put(fileName, readExcelData(fileName)));
        return result;
    }

    private ExcelData readExcelData(String fileName) {
        String filePath = String.format("C:\\Users\\shunjian.hu\\Desktop\\%s", fileName);
        try {
            return ExcelDataHelper.readExcelData(new FileInputStream(filePath));
        } catch (FileNotFoundException exception) {
            log.info("文件:[{}]不存在,结束", fileName);
            return null;
        }
    }

    private ExcelData readExcelData(String folderPath, String fileName) {
        String filePath = String.format("%s\\%s", folderPath, fileName);
        try {
            return ExcelDataHelper.readExcelData(new FileInputStream(filePath));
        } catch (FileNotFoundException exception) {
            log.info("文件:[{}]不存在,结束", fileName);
            return null;
        }
    }


    private ExcelData readExcelData(String fileName, ExcelAnalysisModel analysisModel) {
        String filePath = String.format("C:\\Users\\shunjian.hu\\Desktop\\%s", fileName);
        try {
            return ExcelDataHelper.readExcelData(new FileInputStream(filePath), analysisModel);
        } catch (FileNotFoundException exception) {
            log.info("文件:[{}]不存在,结束", fileName);
            return null;
        }
    }

    @Data
    static class A {
        private String content;
    }

    @ApiOperation("视频转GIF")
    @PostMapping(value = "videoToGif")
    public void videoToGif() {

    }

/*    public static void convertToGif(String videoPath, String gifPath) {
        try {
            // 打开视频文件
            FrameGrab grab = FrameGrab.createFrameGrab(new File(videoPath));

            // 创建GIF写入器
            ImageOutputStream output = ImageIO.createImageOutputStream(new File(gifPath));
            GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_RGB, 100, true);

            // 读取视频帧并写入GIF
            Picture picture;
            while ((picture = grab.getNativeFrame()) != null) {
                BufferedImage image = AWTUtil.toBufferedImage(picture);
                writer.writeToSequence(image);
            }

            // 关闭资源
            writer.close();
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @SneakyThrows
    @ApiOperation("导出反馈数据文件")
    @PostMapping(value = "exportFeedbackFiles")
    public void exportFeedbackFiles(@RequestBody QueryDBReq req) {
        // 端口
        String portId = "8a818137864aaaba01865e9b75f80619";
        // 查询反馈数据SQL
        String queryFileSql = String.format("select t1.mode, t2.id, t2.full_name, t2.fs_id from t_transaction t1, t_file t2 where t1.port_id = '%s' and t1.file_id = t2.id and t1.mode = 'receive'", portId);
        req.setSqlContent(queryFileSql);
        // 查询
        D res = QueryDBHelper.getRes(req, restTemplate);
        String feedbackFolder = "C:\\Users\\shunjian.hu\\Desktop\\feedback";
        // 创建目录
        mkdir(feedbackFolder);
        Map<String, String> fileMap = Maps.newHashMap();
        res.getData().getRows().forEach(row -> {
            String fileName = row.get(2);
            String fileId = row.get(3);
            fileMap.put(fileId, fileName);
        });
        // 导出文件
        QueryDBHelper.download(fileMap, restTemplate, feedbackFolder, req.getDownloadFileCookie());
    }

    @SneakyThrows
    @ApiOperation("读取excel获取文件")
    @PostMapping(value = "readExcelAndDownloadProdFiles")
    public void readExcelAndDownloadProdFiles() {
        //
        ExcelData excelData = ExcelDataHelper.readExcelData(new FileInputStream("C:\\Users\\shunjian.hu\\Desktop\\下载文件\\复旦张江原始资料.xls"));
        //
        List<Map<String, String>> rows = excelData.getSheetRowsMap().get("Sheet1");
        //
        String baseFolderPath = "C:\\Users\\shunjian.hu\\Desktop\\excel_download";
        // 创建目录
        mkdir(baseFolderPath);
        //
        Map<String, List<Map<String, String>>> fileNameGroup = rows.stream().collect(Collectors.groupingBy(row -> row.get("FileName")));
        fileNameGroup.forEach((fileName, fileRows) -> {
            fileRows.sort(Comparator.comparing(row -> row.get("CreateTime")));
            int i = fileName.lastIndexOf(".");
            String substring = fileName.substring(0, i);
            String substring1 = fileName.substring(i + 1);
            for (int i1 = 0; i1 < fileRows.size(); i1++) {
                Map<String, String> row = fileRows.get(i1);
                String finalFileName = String.format("%s(%d).%s", substring, i1 + 1, substring1);
                String fileId = row.get("ObjectId");
                QueryDBHelper.download(fileId, finalFileName, restTemplate, baseFolderPath, QueryDBHelper.DOWNLOAD_FILE_COOKIE);
                log.info("导出进度:[{}-{}],fileId:[{}],fileName:[{}]", i, rows.size(), fileId, fileName);
            }
        });
    }

    @SneakyThrows
    @ApiOperation("下载线上文件")
    @PostMapping(value = "downloadProdFiles")
    public void downloadProdFiles(@RequestBody QueryDBReq req) {
        String baseFolderPath = "C:\\Users\\shunjian.hu\\Desktop\\export_file_info";
        AtomicInteger index = new AtomicInteger(1);
        // 创建目录
        mkdir(baseFolderPath);
        req.getFileInfoMap().forEach((fileId, fileName) -> {
            QueryDBHelper.download(fileId, fileName, restTemplate, baseFolderPath, req.getDownloadFileCookie());
            log.info("导出进度:[{}-{}],fileId:[{}],fileName:[{}]", index.getAndIncrement(), req.getFileInfoMap().size(), fileId, fileName);
        });
    }

    @ApiOperation("修复线上数据")
    @PostMapping(value = "fixProdData")
    public void fixProdData(@RequestBody QueryDBReq req) {
        QueryDBHelper.fixHomePageTaskData(req, restTemplate);
    }

    @SneakyThrows
    @ApiOperation("移动文件")
    @PostMapping(value = "moveFile")
    public void moveFile() {
        // 源文件夹路径列表
        List<String> sourceFolders = Lists.newArrayList(
                "C:\\Users\\shunjian.hu\\Desktop\\export_file\\0",
                "C:\\Users\\shunjian.hu\\Desktop\\export_file\\1",
                "C:\\Users\\shunjian.hu\\Desktop\\export_file\\2",
                "C:\\Users\\shunjian.hu\\Desktop\\export_file\\3",
                "C:\\Users\\shunjian.hu\\Desktop\\export_file\\4",
                "C:\\Users\\shunjian.hu\\Desktop\\export_file\\5",
                "C:\\Users\\shunjian.hu\\Desktop\\export_file\\6",
                "C:\\Users\\shunjian.hu\\Desktop\\export_file\\7"
        );

        // 目标文件夹路径
        String targetFolder = "C:\\Users\\shunjian.hu\\Desktop\\export_file\\all";
        // 移动文件
        moveFilesConcurrently(sourceFolders, targetFolder);
        System.out.println("文件移动完成！");
    }

    public static void moveFilesConcurrently(List<String> sourceFolders, String targetFolder) throws IOException, InterruptedException, ExecutionException {
        // 确保目标文件夹存在
        Path targetPath = Paths.get(targetFolder);
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        for (String sourceFolder : sourceFolders) {
            Path sourcePath = Paths.get(sourceFolder);

            if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
                System.err.println("源文件夹不存在或不是目录: " + sourceFolder);
                continue;
            }

            // 遍历源文件夹中的所有文件和子文件夹
            Files.walk(sourcePath)
                    .filter(Files::isRegularFile) // 只处理普通文件
                    .forEach(file -> {
                        try {
                            // 获取文件的相对路径
                            Path relativePath = sourcePath.relativize(file);

                            // 构造目标文件路径
                            Path targetFilePath = targetPath.resolve(relativePath);

                            // 提交任务到线程池
                            completionService.submit(() -> {
                                try {
                                    // 确保目标文件的父目录存在
                                    Files.createDirectories(targetFilePath.getParent());

                                    // 移动文件到目标文件夹
                                    Files.move(file, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                                    System.out.println("移动文件: " + file + " -> " + targetFilePath);
                                } catch (IOException e) {
                                    System.err.println("移动文件失败: " + file);
                                    e.printStackTrace();
                                }
                                return null;
                            });
                        } catch (Exception e) {
                            System.err.println("准备移动文件时出错: " + file);
                            e.printStackTrace();
                        }
                    });
        }

        // 等待所有任务完成
        int totalTasks = sourceFolders.stream().mapToInt(sourceFolder -> {
            try {
                return (int) Files.walk(Paths.get(sourceFolder)).filter(Files::isRegularFile).count();
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }).sum();

        for (int i = 0; i < totalTasks; i++) {
            Future<Void> future = completionService.take(); // Wait for a task to complete
            future.get(); // Check for exceptions
        }

        // 关闭线程池
        executorService.shutdown();
    }

    @SneakyThrows
    @ApiOperation("诺和诺德导出递交文件")
    @PostMapping(value = "exportNuoHeSubmitFiles")
    public void exportNuoHeSubmitFiles(@RequestBody QueryDBReq req) {
        req.setInstanceName("prod-mysql-pv-ro");
        req.setDbName("pv");
        req.setLimitNum("0");
        req.setCookie(QueryDBHelper.DB_COOKIE);
        req.setCsrfToken(QueryDBHelper.DB_CSRF_TOKEN);
        req.setDownloadFileCookie(QueryDBHelper.DOWNLOAD_FILE_COOKIE);
        long start = System.currentTimeMillis();
        // 查询数据
        String countSql = "select count(1) from as2_transaction t left join as2_receive_file arf on arf.transaction_id = t.id where t.transaction_type = 0 and t.ack_status = 1 and t.tenant_id = '251ca460-6348-11e9-b21d-ef616d53ac34' and t.update_time <= '2025-04-21 10:00:00' order by t.update_time asc";
        String querySql = "select arf.fs_file_id as 'ack_file_id',t.file_id as 'xml_file_id',t.report_no,t.sender,t.update_time from as2_transaction t left join as2_receive_file arf on arf.transaction_id = t.id where t.transaction_type = 0 and t.ack_status = 1 and t.tenant_id = '251ca460-6348-11e9-b21d-ef616d53ac34' and t.update_time <= '2025-04-21 10:00:00' order by t.update_time asc;";
        Pair<List<List<String>>, List<List<String>>> result = getPageData(req, countSql, querySql);
        // 导出数据
        List<List<String>> data = result.getValue();
        log.info("导出总条数:[{}]", data.size());
        // 数据按照报告编号和接收方,取最后一个
        Map<String, List<String>> reportSenderRowMap = data.stream().collect(Collectors.toMap(row -> String.format("%s-%s", row.get(2), row.get(3)), row -> row, (o, n) -> n));
        data = Lists.newArrayList(reportSenderRowMap.values());
        log.info("根据报告和发送者信息去重后总条数:[{}]", data.size());
        String baseFolderPath = "C:\\Users\\shunjian.hu\\Desktop\\export_file";
        // 创建目录
        mkdir(baseFolderPath);
        // 10000个一个文件夹
        List<List<List<String>>> partition = ListUtils.partition(data, 10000);
        for (int a = 5; a < partition.size(); a++) {
            List<List<String>> part = partition.get(a);
            // 创建文件夹
            String partFolderPath = baseFolderPath + "\\" + a;
            mkdir(partFolderPath);
            // 循环处理数据
            for (int i = 0; i < part.size(); i++) {
                List<String> row = part.get(i);
                String ackFileId = row.get(0);
                String xmlFileId = row.get(1);
                String reportNo = row.get(2);
                String sender = row.get(3);
                String receiver;
                switch (sender) {
                    case "NOVOPRODAS2":
                        receiver = "CDE";
                        break;
                    case "novo5899":
                        receiver = "CDR";
                        break;
                    default:
                        receiver = sender;
                        log.info("发送文件id:[{}]出现其他情况", xmlFileId);
                        break;
                }
                // 导出递交的xml文件
                QueryDBHelper.download(xmlFileId, String.format("%s-%s.xml", reportNo, receiver), restTemplate, partFolderPath, req.getDownloadFileCookie());
                // 导出收到的ack文件
                QueryDBHelper.download(ackFileId, String.format("%s-%sack.xml", reportNo, receiver), restTemplate, partFolderPath, req.getDownloadFileCookie());
                log.info("导出进度:[{}-{}]", i, part.size());
                System.out.println();
            }
            Thread.sleep(10000);
            log.info("分文件夹处理进度:[{}-{}]", a, partition.size());
        }
        log.info("总耗时:[{}]", System.currentTimeMillis() - start);
    }

    @SneakyThrows
    @ApiOperation("华东医药集团导出递交文件")
    @PostMapping(value = "exportSubmitFiles")
    public void exportSubmitFiles(@RequestBody QueryDBReq req) {
        long start = System.currentTimeMillis();
        // 查询数据
        String countSql = "select count(1) from as2_transaction t1, report_value t2 where t1.tenant_id = 'HZZMHDZY' and t1.report_id = t2.id and t2.classify_of_report in ('e9b3ace4-11af-11ea-8022-000c29ee981b', 'e9b3aced-11af-11ea-8022-000c29ee981b') and t1.ack_receive_time is not null and ack_status = 1 and t1.create_time between '2024-12-14 00:00:00' and '2025-02-16 23:59:59';";
        String querySql = "select t1.report_id, t1.report_no, t1.receiver, t1.file_id, t1.file_name from as2_transaction t1, report_value t2 where t1.tenant_id = 'HZZMHDZY' and t1.report_id = t2.id and t2.classify_of_report in ('e9b3ace4-11af-11ea-8022-000c29ee981b', 'e9b3aced-11af-11ea-8022-000c29ee981b') and t1.ack_receive_time is not null and ack_status = 1 and t1.create_time between '2024-12-14 00:00:00' and '2025-02-16 23:59:59' order by t1.report_no;";
        Pair<List<List<String>>, List<List<String>>> result = getPageData(req, countSql, querySql);
        // 导出数据
        List<List<String>> data = result.getValue();
        log.info("导出总条数:[{}]", data.size());
        String baseFolderPath = "C:\\Users\\shunjian.hu\\Desktop\\export_file";
        // 创建目录
        mkdir(baseFolderPath);
        // 循环处理数据
        for (int i = 0; i < data.size(); i++) {
            List<String> row = data.get(i);
            String reportId = row.get(0);
            String reportNo = row.get(1);
            String receiver = row.get(2);
            String fileId = row.get(3);
            String fileName = row.get(4);
            // 导出文件
            QueryDBHelper.download(fileId, String.format("%s-%s.xml", reportNo, receiver), restTemplate, baseFolderPath, req.getDownloadFileCookie());
            log.info("报告:[id{},reportNo:{}]接收方:[{}]文件[{}]导出完成,进度:[{}-{}]", reportId, reportNo, receiver, fileName, i, data.size());
            System.out.println();
        }
        log.info("总耗时:[{}]", System.currentTimeMillis() - start);
    }

    @SneakyThrows
    @ApiOperation("根据报告编号查询报告id")
    @PostMapping(value = "getReportId")
    public void getReportId(@RequestBody QueryDBReq req) {
        // 读取文件
        ExcelData excelData = readExcelData("update-基于昨日诉求，匹配逻辑去掉版本号.xlsx");
        // 获取报告编号
        List<Map<String, String>> rows = excelData.getSheetRowsMap().get("Sheet1");
        // 报告编号
        List<String> reportNos = Lists.newArrayList();
        rows.forEach(row -> {
            String reportNo1 = row.get("报告编号");
            if (StringUtils.isNotBlank(reportNo1)) {
                reportNos.add(reportNo1);
            }
        });
        String tenantId = "2018SCBLYY";
        // 查询这些编号的报告
        // String queryReportInfoSql = String.format("select id AS 'reportId', IF(versions is null or versions = '', safety_report_id, CONCAT(safety_report_id, '-', versions)) AS 'reportNo', locale, safety_report_unique_identifier, world_unique_num, project_id from report_value where tenant_id = '%s' and IF(versions is null or versions = '', safety_report_id, CONCAT(safety_report_id, '-', versions)) in ('%s');", tenantId, String.join("','", reportNos));
        // String queryReportInfoSql = String.format("select id AS 'reportId', IF(versions is null or versions = '', safety_report_id, CONCAT(safety_report_id, '-', versions)) AS 'reportNo', locale from report_value where tenant_id = '%s' and locale = '%s' and IF(versions is null or versions = '', safety_report_id, CONCAT(safety_report_id, '-', versions)) in ('%s');", tenantId, "zh_CN", String.join("','", reportNos));
        String queryReportInfoSql = String.format("select IF(versions is null or versions = '', safety_report_id, CONCAT(safety_report_id, '-', versions)) AS '报告编号', report_status as '报告状态' from report_value where tenant_id = '%s' and locale = '%s' and IF(versions is null or versions = '', safety_report_id, CONCAT(safety_report_id, '-', versions)) in ('%s');", tenantId, "zh_CN", String.join("','", reportNos));
        req.setSqlContent(queryReportInfoSql);
        // String countSql = String.format("select count(1) from report_value where tenant_id = '%s' and IF(versions is null or versions = '', safety_report_id, CONCAT(safety_report_id, '-', versions)) in ('%s');", tenantId, String.join("','", reportNos));
        String countSql = String.format("select count(1) from report_value where tenant_id = '%s' and locale = '%s' and IF(versions is null or versions = '', safety_report_id, CONCAT(safety_report_id, '-', versions)) in ('%s');", tenantId, "zh_CN", String.join("','", reportNos));
        // 导出所有
        exportPageData(req, countSql, queryReportInfoSql);

    }

    @SneakyThrows
    @ApiOperation("数据修复")
    @PostMapping(value = "fixData")
    public void fixData(@RequestBody QueryDBReq req) {
        req.setLimitNum("0");
        // 读取excel数据
        ExcelData excelData = readExcelData("需要修改的.xlsx");
        // 获取记录
        List<Map<String, String>> rows = excelData.getSheetRowsMap().get("Sheet1");
        // 修复SQL
        List<String> fixSql = Lists.newArrayList();
        // 循环处理
        rows.forEach(row -> {
            // 报告id
            String reportId = row.get("reportId");
            // 语言
            String locale = row.get("locale");
            // C.1.1
            String safetyReportUniqueIdentifier = row.get("safety_report_unique_identifier");
            // C.1.8.1
            String worldUniqueNum = row.get("world_unique_num");
            // 项目id
            String projectId = row.get("project_id");
            // C.3.2
            String newSenderOrganization = StringUtils.equals(locale, "zh_CN") ? "恒瑞源正(深圳)生物科技有限公司" : "HRYZ BIOTECH CO., LTD.";
            // 如果C.1.1有值,则忽略更新
            if (StringUtils.isBlank(safetyReportUniqueIdentifier)) {
                fixSql.add(String.format("update report_value set safety_report_unique_identifier = '%s', version = version + 1 where id = '%s';", worldUniqueNum, reportId));
            }
            // 查询发送者信息
            String querySenderSql = String.format("select id, sender_type, sender_organization from sender where report_id = '%s' and is_deleted = 0;", reportId);
            // 设置查询SQL
            req.setSqlContent(querySenderSql);
            // 查询
            List<Map<String, String>> senderRows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "sender_type", "sender_organization"));
            if (CollectionUtils.isEmpty(senderRows)) {
                String id = UUID.randomUUID().toString().replace("-", "");
                // 需要insert的
                fixSql.add(String.format("INSERT INTO `pvs_report`.`sender`(`id`, `sender_type`, `sender_organization`, `report_id`, `tenant_id`, `create_time`, `is_deleted`, `locale`, `multi_record_key`, `project_id`, `sort_index`, `version`) VALUES ('%s', 'cc656825-6865-11e8-a602-000c29399d7c', '%s', '%s', '%s', NOW(), 0, '%s', '%s', '%s', 0, 0);", id, newSenderOrganization, reportId, "0dd84a7f1b4ea24984644563e1232830", locale, id, projectId));
            } else {
                // 需要update的
                if (senderRows.size() != 1) {
                    throw new RuntimeException();
                }
                Map<String, String> sender = senderRows.get(0);
                // id
                String id = sender.get("id");
                // senderType
                String senderType = sender.get("sender_type");
                // senderOrganization
                String senderOrganization = sender.get("sender_organization");
                // 打印原信息
                fixSql.add(String.format("-- 原信息 -> id:%s;sender_type:%s;sender_organization:%s", id, senderType, senderOrganization));
                // 更新后信息
                fixSql.add(String.format("update sender set sender_type = 'cc656825-6865-11e8-a602-000c29399d7c', sender_organization = '%s', version = version + 1 where id = '%s';", newSenderOrganization, id));
            }
        });
        // 导出修复SQL
        exportFixSQLFile(fixSql);
    }

    @SneakyThrows
    @ApiOperation("修复租户时间里有uk的")
    @PostMapping(value = "fixTenantDateUk")
    public void fixTenantDateUk(@RequestBody QueryDBReq req) {
        // 查询可能有问题的表里的记录
        Map<String, Pair<String, String>> importUkDateMap = Maps.newLinkedHashMap();
        // 不良事件的
        importUkDateMap.put("不良事件",
                Pair.of(
                        String.format(
                                "select count(1) from adverse_event where tenant_id = '%s' and is_deleted = 0 and (date_of_onset like '%s' or date_of_resolution like '%s');", req.getTenantId(), "%u%", "%u%"
                        ),
                        String.format(
                                "select IF(t2.versions is null or t2.versions = '', t2.safety_report_id, CONCAT(t2.safety_report_id, '-', versions)) AS '报告编号', IF(t2.locale = 'zh_CN', '中文报告', '英文报告') as '报告语言',  t1.id as '不良事件id', t1.event_name as '不良事件名称', t1.date_of_onset as '不良事件开始事件', t1.date_of_resolution as '不良事件结束事件' from adverse_event t1, report_value t2 where t1.report_id = t2.id and t1.tenant_id = '%s' and t1.is_deleted = 0 and (t1.date_of_onset like '%s' or t1.date_of_resolution like '%s')", req.getTenantId(), "%u%", "%u%"
                        )
                )
        );
        // 实验室检查的
        importUkDateMap.put("实验室检查",
                Pair.of(
                        String.format(
                                "select count(1) from patient_lab_data where tenant_id = '%s' and is_deleted = 0 and lab_date like '%s';", req.getTenantId(), "%u%"
                        ),
                        String.format(
                                "select IF(t2.versions is null or t2.versions = '', t2.safety_report_id, CONCAT(t2.safety_report_id, '-', versions)) AS '报告编号', IF(t2.locale = 'zh_CN', '中文报告', '英文报告') as '报告语言',  t1.id as '实验室检查id', t1.lab_test as '检查项', t1.lab_date as '实验室检查时间' from patient_lab_data t1, report_value t2 where t1.report_id = t2.id and t1.tenant_id = '%s' and t1.is_deleted = 0 and (t1.lab_date like '%s')", req.getTenantId(), "%u%"
                        )
                )
        );
        // 相关病史
        importUkDateMap.put("相关病史",
                Pair.of(
                        String.format(
                                "select count(1) from patient_medical_history where tenant_id = '%s' and is_deleted = 0 and (start_date like '%s' or stop_date like '%s');", req.getTenantId(), "%u%", "%u%"
                        ),
                        String.format(
                               "select IF(t2.versions is null or t2.versions = '', t2.safety_report_id, CONCAT(t2.safety_report_id, '-', versions)) AS '报告编号', IF(t2.locale = 'zh_CN', '中文报告', '英文报告') as '报告语言',  t1.id as '病史id', t1.start_date as '开始时间', t1.stop_date as '结束时间' from patient_medical_history t1, report_value t2 where t1.report_id = t2.id and t1.tenant_id = '%s' and t1.is_deleted = 0 and (t1.start_date like '%s' or t1.stop_date like '%s')", req.getTenantId(), "%u%", "%u%"
                        )
                )
        );
        // 相关药物史
        importUkDateMap.put("相关药物史",
                Pair.of(
                        String.format(
                                "select count(1) from patient_drug_history where tenant_id = '%s' and is_deleted = 0 and (start_date like '%s' or stop_date like '%s');", req.getTenantId(), "%u%", "%u%"
                        ),
                        String.format(
                                "select IF(t2.versions is null or t2.versions = '', t2.safety_report_id, CONCAT(t2.safety_report_id, '-', versions)) AS '报告编号', IF(t2.locale = 'zh_CN', '中文报告', '英文报告') as '报告语言',  t1.id as '相关药物史id', t1.start_date as '开始时间', t1.stop_date as '结束时间' from patient_drug_history t1, report_value t2 where t1.report_id = t2.id and t1.tenant_id = '%s' and t1.is_deleted = 0 and (t1.start_date like '%s' or t1.stop_date like '%s')", req.getTenantId(), "%u%", "%u%"
                        )
                )
        );
        // 剂量信息
        importUkDateMap.put("剂量信息",
                Pair.of(
                        String.format(
                                "select count(1) from drug_dose where tenant_id = '%s' and is_deleted = 0 and (start_date like '%s' or end_date like '%s');", req.getTenantId(), "%u%", "%u%"
                        ),
                        String.format(
                                "select IF(t2.versions is null or t2.versions = '', t2.safety_report_id, CONCAT(t2.safety_report_id, '-', versions)) AS '报告编号', IF(t2.locale = 'zh_CN', '中文报告', '英文报告') as '报告语言',  t1.id as '剂量id', t1.start_date as '开始时间', t1.end_date as '结束时间' from drug_dose t1, report_value t2 where t1.report_id = t2.id and t1.tenant_id = '%s' and t1.is_deleted = 0 and (t1.start_date like '%s' or t1.end_date like '%s')", req.getTenantId(), "%u%", "%u%"
                        )
                )
        );
        // 循环导出文件
        importUkDateMap.forEach((fileName, queryPair) -> {
            log.info("开始处理:[{}]", fileName);
            // 设置count条件
            req.setSqlContent(queryPair.getKey());
            Integer count = countSql(req);
            log.info("总数:[{}]", count);
            // 总页数
            int totalPage = totalPage(count, 5000);;
            // 所有数据
            List<List<String>> allData = Lists.newArrayList();
            // 页数据
            D pageData = null;
            // 分页查询
            for (int i = 0; i < totalPage; i++) {
                log.info("分页进度:[{}-{}]", i, totalPage);
                // 当前页数据
                pageData = findPageDate(req, queryPair.getValue(), i * 5000, 5000);
                // 追加行数据
                allData.addAll(pageData.getData().getRows().stream().map(Lists::newArrayList).collect(Collectors.toList()));
            }
            // 标题
            List<List<String>> headers = pageData.getData().getColumn_list().stream().map(Lists::newArrayList).collect(Collectors.toList());
            QueryDBHelper.exportExcel(headers, allData, fileName);
        });
        Thread.sleep(2000);
        // 生成修复SQL
        fixSql(Lists.newArrayList(importUkDateMap.keySet()));
    }



    @SneakyThrows
    @ApiOperation("导出分页数据")
    @PostMapping(value = "exportPageData")
    public void exportPageData(@RequestBody QueryDBReq req) {
        //String countSql = "select count(1) from pvs_middle_data.schema_field as sf inner join schema_module as sm on sm.id=sf.module_id inner join pvs_middle_data.item_class as iclass on iclass.id=sf.item_class_id inner join pvs_middle_data.items as item on item.item_class_id=iclass.id inner join esafety5_e2b.e2b_r3_estri_item_config as uite on uite.item_unique_code=item.unique_code COLLATE utf8mb4_bin inner join esafety5_e2b.e2b_r3_m2estri_config as ucum on ucum.id=uite.estri_id where sf.tenant_id='system' and sf.is_deleted=0 and sf.type='VIEW' and iclass.is_deleted=0 and item.is_deleted=0 and uite.is_deleted=0 and sf.report_field_id in ('4028c09b889f1ace01889f1ace45000b','09d72147-984e-11eb-ad65-0050568ea815','0a2bc7ce-984e-11eb-ad65-0050568ea815','0a3a7c4d-984e-11eb-ad65-0050568ea815','0a3d1eab-984e-11eb-ad65-0050568ea815','ac7f09c2-b951-11eb-ad65-0050568ea815','0a65c694-984e-11eb-ad65-0050568ea815','f2ff9f87-b940-11eb-ad65-0050568ea815','0697a9f7-b942-11eb-ad65-0050568ea815','505284da-b942-11eb-ad65-0050568ea815','81b1aa64efee11eea64100163e0a5e7b','81b1e3f7efee11eea64100163e0a5e7b','8ac081438ebbc430018ebd177c8001d4','81b1c6b7efee11eea64100163e0a5e7b','81b1caa9efee11eea64100163e0a5e7b','81b1cb51efee11eea64100163e0a5e7b','81b1cb7befee11eea64100163e0a5e7b','81b1d9dbefee11eea64100163e0a5e7b','81b212adefee11eea64100163e0a5e7b','81b2048fefee11eea64100163e0a5e7b','81b1cd13efee11eea64100163e0a5e7b') order by sm.name ,sf.report_field_id;";
        //String querySql = "select sm.name as '模块名',sf.description as '字段描述',uite.element_number as 'element number', iclass.name as '字典名字', item.unique_code as '字典项code' ,item.name '字典项名字',ucum.code 'E2B Code',sm.id as '模块ID' ,sf.id as 'schemaField ID' ,sf.report_field_id as '字段 ID', iclass.id as '字典classID',item.id as '字典项ID',uite.id as 'e2b item ID' ,ucum.id as 'e2b ucum ID' from pvs_middle_data.schema_field as sf inner join schema_module as sm on sm.id=sf.module_id inner join pvs_middle_data.item_class as iclass on iclass.id=sf.item_class_id inner join pvs_middle_data.items as item on item.item_class_id=iclass.id inner join esafety5_e2b.e2b_r3_estri_item_config as uite on uite.item_unique_code=item.unique_code COLLATE utf8mb4_bin inner join esafety5_e2b.e2b_r3_m2estri_config as ucum on ucum.id=uite.estri_id where sf.tenant_id='system' and sf.is_deleted=0 and sf.type='VIEW' and iclass.is_deleted=0 and item.is_deleted=0 and uite.is_deleted=0 and sf.report_field_id in ('4028c09b889f1ace01889f1ace45000b','09d72147-984e-11eb-ad65-0050568ea815','0a2bc7ce-984e-11eb-ad65-0050568ea815','0a3a7c4d-984e-11eb-ad65-0050568ea815','0a3d1eab-984e-11eb-ad65-0050568ea815','ac7f09c2-b951-11eb-ad65-0050568ea815','0a65c694-984e-11eb-ad65-0050568ea815','f2ff9f87-b940-11eb-ad65-0050568ea815','0697a9f7-b942-11eb-ad65-0050568ea815','505284da-b942-11eb-ad65-0050568ea815','81b1aa64efee11eea64100163e0a5e7b','81b1e3f7efee11eea64100163e0a5e7b','8ac081438ebbc430018ebd177c8001d4','81b1c6b7efee11eea64100163e0a5e7b','81b1caa9efee11eea64100163e0a5e7b','81b1cb51efee11eea64100163e0a5e7b','81b1cb7befee11eea64100163e0a5e7b','81b1d9dbefee11eea64100163e0a5e7b','81b212adefee11eea64100163e0a5e7b','81b2048fefee11eea64100163e0a5e7b','81b1cd13efee11eea64100163e0a5e7b') order by sm.name ,sf.report_field_id;";

        //String countSql = "";
        //String querySql = "";


        String countSql = "select count(1) from drug  where tenant_id = 'eSafety20241216' and create_time = '1970-01-01 00:00:00';";
        String querySql = "";

        exportPageData(req, countSql, querySql);
    }

    private Pair<List<List<String>>, List<List<String>>> getPageData(QueryDBReq req, String countSql, String querySql) {
        req.setSqlContent(countSql);
        Integer count = countSql(req);
        log.info("总数:[{}]", count);
        // 总页数
        int totalPage = totalPage(count, 5000);
        // 所有数据
        List<List<String>> allData = Lists.newArrayList();
        // 页数据
        D pageData = null;
        // 分页查询
        for (int i = 0; i < totalPage; i++) {
            log.info("分页进度:[{}-{}]", i, totalPage);
            // 当前页数据
            pageData = findPageDate(req, querySql, i * 5000, 5000);
            // 追加行数据
            allData.addAll(pageData.getData().getRows().stream().map(Lists::newArrayList).collect(Collectors.toList()));
        }
        List<List<String>> headers = Lists.newArrayList();
        // 标题
        if (pageData != null) {
            headers = pageData.getData().getColumn_list().stream().map(Lists::newArrayList).collect(Collectors.toList());
        }
        return Pair.of(headers, allData);
    }

    private D getAllData(QueryDBReq req, String countSql, String querySql) {
        req.setSqlContent(countSql);
        Integer count = countSql(req);
        log.info("总数:[{}]", count);
        // 总页数
        int totalPage = totalPage(count, 5000);
        // 所有数据
        List<List<String>> allData = Lists.newArrayList();
        // 页数据
        D pageData = null;
        // 分页查询
        for (int i = 0; i < totalPage; i++) {
            log.info("分页进度:[{}-{}]", i, totalPage);
            // 当前页数据
            pageData = findPageDate(req, querySql, i * 5000, 5000);
            // 追加行数据
            allData.addAll(pageData.getData().getRows().stream().map(Lists::newArrayList).collect(Collectors.toList()));
        }
        D res = new D();
        res.setData(new DData(pageData.getData().getColumn_list(), allData));
        return res;
    }

    private static int totalPage(int totalCount, int pageSize) {
        if (pageSize == 0) {
            return 0;
        } else {
            return totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        }
    }


    private void exportPageData(QueryDBReq req, String countSql, String querySql) {
        Pair<List<List<String>>, List<List<String>>> result = getPageData(req, countSql, querySql);
        QueryDBHelper.exportExcel(result.getKey(), result.getValue(), req.getFileName());
    }

    private D findPageDate(QueryDBReq req, String findSql, int off, int size) {
        if (findSql.endsWith(";")) {
            findSql = findSql.substring(0, findSql.length() - 1);
        }
        findSql = String.format("%s LIMIT %d, %d;", findSql, off, size);
        // 设置最大
        req.setLimitNum("5000");
        req.setSqlContent(findSql);
        // 查询
        return QueryDBHelper.getRes(req, restTemplate);
    }

    private static final Map<String, List<List<String>>> map = Maps.newHashMap();


    private static final List<String> errors = Lists.newArrayList();

    @SneakyThrows
    @ApiOperation("修复租户的Meddra信息")
    @PostMapping(value = "fixTenantMeddra")
    public void fixTenantMeddra() {
        List<String> tenantIds = Lists.newArrayList(
                "fa0bbb299b0f4c6d82f6a76f0094406d",
                "59a16a8889a7431382b9a8490057be0f",
                "PVS64",
                "37cb9ef0-6347-11e9-b21d-ef616d53ac34",
                "9137130072755352X1",
                "2018CQTJ0001",
                "adb0ac3bf54b4f199de4a6e9006815b0",
                "561a1ad0-6347-11e9-b21d-ef616d53ac34",
                "54759290-6347-11e9-b21d-ef616d53ac34",
                "2018WEM0001",
                "63a825b0-6348-11e9-b21d-ef616d53ac34",
                "2018PVS011",
                "60029d50-6348-11e9-b21d-ef616d53ac34",
                "42a6b440-6347-11e9-b21d-ef616d53ac34",
                "55791fe0-6347-11e9-b21d-ef616d53ac34",
                "YBYE201896",
                "52fb3220-6348-11e9-b21d-ef616d53ac34",
                "5088f9a0-6348-11e9-b21d-ef616d53ac34",
                "f9762c50-6347-11e9-b21d-ef616d53ac34",
                "26565ce0-6348-11e9-b21d-ef616d53ac34",
                "574a40f0-6348-11e9-b21d-ef616d53ac34",
                "d4bdbea0-634c-11e9-b8fa-6fbfe9591074",
                "cf349410-634a-11e9-b21d-ef616d53ac34",
                "75f5a9cdab534fb7ae8ea75c00984956",
                "9fdd2950-6347-11e9-b21d-ef616d53ac34",
                "8a8181276c335a2d016c5f62dd2e6c2f",
                "4e36a530-6348-11e9-b21d-ef616d53ac34",
                "abb4c710-6347-11e9-b21d-ef616d53ac34",
                "9113000023565800XC",
                "d65f39a0-634c-11e9-b8fa-6fbfe9591074",
                "8a8181e07a288e03017a377bc4bc0af4",
                "8a8181276be11a45016bf38a73ac627d",
                "fec1c6c0-634b-11e9-b8fa-6fbfe9591074",
                "91330100609120766P",
                "8b68c9d3-afe5-4996-a237-83e54ae299bc",
                "2018HZMS",
                "91371524706276068X",
                "eSafety2023031",
                "cb7f019c508d43a0997ca855006b10c8",
                "1110",
                "fd6a35caab9a71c54c9d4727b378ea17",
                "ed16ea26-16fd-465f-a94d-adf449fec06e",
                "eSafety20230626",
                "SHXDHP2018910",
                "2018SCBLYY",
                "pv-esae",
                "cbf6768cf1234a76aa15a807009d6ead",
                "2018btyy0001"
        );
        // 循环租户
        tenantIds.forEach(tenantId -> {
            QueryDBReq req = new QueryDBReq();
            req.setTenantId(tenantId);
            fixMeddra(req);
        });
    }

    @SneakyThrows
    @ApiOperation("修复Meddra")
    @PostMapping(value = "fixMeddra")
    public void fixMeddra(@RequestBody QueryDBReq req) {
        String tenantId = req.getTenantId();
        req.setCookie(QueryDBHelper.DB_COOKIE);
        req.setCsrfToken(QueryDBHelper.DB_CSRF_TOKEN);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 需要查询的表
        List<String> tables = Lists.newArrayList("drug", "drug_dose", "drug_reason");
        // 查出来的所有数据,每个id在哪个表出现
        Map<String, List<String>> idTablesMap = Maps.newHashMap();
        // 循环表查询
        tables.forEach(table -> {
            // count
            String countSql = String.format("select count(1) from %s where tenant_id = '%s' and is_deleted = 0", table, tenantId);
            // 查询
            String querySql = String.format("select id from %s where tenant_id = '%s' and is_deleted = 0", table, tenantId);
            // 分页查数据
            List<List<String>> rows = getPageData(req, countSql, querySql).getValue();
            // 提取数据
            rows.forEach(row -> idTablesMap.computeIfAbsent(row.get(0), v -> Lists.newArrayList()).add(table));
        });
        // 移除掉只有一个的
        idTablesMap.entrySet().removeIf(entry -> entry.getValue().size() == 1);
        // 在查询这些里面有meddra信息的
        List<String> needCheckIds = Lists.newArrayList(idTablesMap.keySet());
        // 所有有meddra信息的itemIds
        Set<String> hasMeddraItemIds = Sets.newHashSet();
        // 每200个查询一次
        ListUtils.partition(needCheckIds, 500).forEach(partIds -> {
            String getItemIds = String.format("select distinct item_id from meddra_field_info where tenant_id = '%s' and item_id in ('%s')", tenantId, String.join("','", partIds));
            req.setSqlContent(getItemIds);
            // 有信息的
            QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("item_id")).forEach(row -> hasMeddraItemIds.add(row.get("item_id")));
        });
        Map<String, String> tableIdReNameMap = Maps.newHashMap();
        tableIdReNameMap.put("drug_dose", "dose");
        tableIdReNameMap.put("drug_reason", "reason");
        List<String> fixSql = Lists.newArrayList();
        // 查询meddra信息
        ListUtils.partition(Lists.newArrayList(hasMeddraItemIds), 500).forEach(part -> {
            String queryMeddraSql = String.format("select * from meddra_field_info where item_id in ('%s')", String.join("','", part));
            req.setSqlContent(queryMeddraSql);
            D res = QueryDBHelper.getRes(req, restTemplate);
            // 提取信息
            List<Map<String, String>> meddraInfos = QueryDBHelper.extractColumnValues(res, Lists.newArrayList("id", "field_name"));
            // 循环信息判断
            meddraInfos.forEach(meddraInfo -> {
                // 根据字段名称判断
                String fieldName = meddraInfo.get("field_name");
                switch (fieldName) {
                    case "reason":
                        String newId = meddraInfo.get("id") + "_reason";
                        // 换掉meddra里的itemId
                        fixSql.add(String.format("update meddra_field_info set item_id = '%s' where id = '%s'", newId, meddraInfo.get("id")));
                        break;
                    case "A":
                    case "B":
                    default:
                        throw new RuntimeException();
                }
            });
        });
        // 找出所有id有多个的
        for (Entry<String, List<String>> entry : idTablesMap.entrySet()) {
            entry.getValue().forEach(table -> {
                String reName = tableIdReNameMap.get(table);
                if (StringUtils.isNotBlank(reName)) {
                    fixSql.add(String.format("update %s set id = '%s_%s' where id = '%s';", table, entry.getKey(), reName, entry.getKey()));
                }
            });
        }
        System.out.println();
        // 生成修复SQL
        exportFixSQLFile(fixSql);
    }

    @SneakyThrows
    @ApiOperation("导出字典项错误的数据")
    @PostMapping(value = "exportErrorItemsData")
    public void exportErrorItemsData(@RequestBody QueryDBReq req) {
        // exportErrorItemsData(req, req.getQueryErrorSqlInfos());

        // 读SQL
        ExcelData excelData = readExcelData("SQL.xlsx");
        List<List<String>> headers = Lists.newArrayList();
        List<List<String>> table = Lists.newArrayList();
        AtomicInteger num = new AtomicInteger(1);
        for (Map<String, String> sql : excelData.getSheetRowsMap().get("Sheet1")) {
            log.info("开始处理第:[{}]条SQL", num.getAndIncrement());
            // querySql
            String querySql = sql.get("查询SQL");
            // 组装countSql
            String countSql = String.format("select count(1) from (%s) a", querySql.replace(";", ""));
            if (!map.containsKey(querySql)) {
                try {
                    Pair<List<List<String>>, List<List<String>>> pageData = getPageData(req, countSql, querySql);
                    if (CollectionUtils.isEmpty(headers) && CollectionUtils.isNotEmpty(pageData.getKey())) {
                        headers = pageData.getKey();
                    }
                    table.addAll(pageData.getValue());
                    QueryDBHelper.exportExcel(headers, pageData.getValue(), UUID.randomUUID().toString());
                    map.put(querySql, pageData.getValue());
                    System.out.println("");
                } catch (Exception ex) {
                    errors.add(querySql);
                    log.info("错误");
                }
            }
        }
        QueryDBHelper.exportExcel(headers, table, req.getFileName());
    }

    @SneakyThrows
    @ApiOperation("导出字典项错误的数据,出入SQL的")
    @PostMapping(value = "exportErrorItemsData2")
    public void exportErrorItemsData2(@RequestBody QueryDBReq req) {
        exportErrorItemsData(req, req.getQueryErrorSqlInfos());
    }

    private void exportErrorItemsData(QueryDBReq req, List<String> querySqlInfos) {
        List<String> errorSqlInfos = Lists.newArrayList();
        querySqlInfos.forEach(querySql -> {
            try {
                // 组装countSql
                String countSql = String.format("select count(1) from (%s) a", querySql.replace(";", ""));
                Pair<List<List<String>>, List<List<String>>> pageData = getPageData(req, countSql, querySql);
                QueryDBHelper.exportExcel(pageData.getKey(), pageData.getValue(), UUID.randomUUID().toString());
            } catch (Exception ex) {
                log.info("错误");
                errorSqlInfos.add(querySql);
            }
        });
        System.out.println();
    }


    @SneakyThrows
    @ApiOperation("修复时间里有uk的")
    @PostMapping(value = "fixDateUkSql")
    public void fixSql(@RequestBody List<String> fileNames) {
        // 输出的更新语句
        List<String> result = Lists.newArrayList();
        List<String> files = Lists.newArrayList("", "", "", "");
        files.add("不良事件.xlsx");
        files.add("实验室检查.xlsx");
        files.add("相关病史.xlsx");
        files.add("相关药物史.xlsx");
        files.add("剂量信息.xlsx");
        if (CollectionUtils.isNotEmpty(fileNames)) {
            files.addAll(fileNames);
        }
        List<FixData> fixDataInfos = Lists.newArrayList();
        Set<String> dates = Sets.newHashSet();
        for (String fileName : files) {
            ExcelData excelData = readExcelData(fileName);
            if (excelData == null || CollectionUtils.isEmpty(excelData.getSheetRowsMap().get("Sheet1"))) {
                continue;
            }
            // 获取内容
            List<Map<String, String>> rows = excelData.getSheetRowsMap().get("Sheet1");
            switch (fileName) {
                case "不良事件.xlsx":
                    rows.forEach(row -> {
                        // 调整内容
                        String dateOfOnset = handleDateStr(row.get("不良事件开始时间"));
                        dates.add(dateOfOnset);
                        String dateOfResolution = handleDateStr(row.get("不良事件结束时间"));
                        dates.add(dateOfResolution);
                        String id = row.get("不良事件id");
                        result.add(String.format("UPDATE adverse_event SET date_of_onset = '%s', date_of_resolution = '%s' WHERE id = '%s';", dateOfOnset, dateOfResolution, id));
                        FixData fixData = new FixData();
                        fixData.setId(id);
                        fixData.setTable("adverse_event");
                        Map<String, String> columnValueMap = Maps.newHashMap();
                        columnValueMap.put("date_of_onset", dateOfOnset);
                        columnValueMap.put("date_of_resolution", dateOfResolution);
                        fixData.setColumnValueMap(columnValueMap);
                        fixDataInfos.add(fixData);
                    });
                    break;
                case "实验室检查.xlsx":
                    rows.forEach(row -> {
                        // 调整内容
                        String labDate = handleDateStr(row.get("实验室检查时间"));
                        dates.add(labDate);
                        String id = row.get("实验室检查id");
                        result.add(String.format("UPDATE patient_lab_data SET lab_date = '%s' WHERE id = '%s';", labDate, id));
                        FixData fixData = new FixData();
                        fixData.setId(id);
                        fixData.setTable("patient_lab_data");
                        Map<String, String> columnValueMap = Maps.newHashMap();
                        columnValueMap.put("lab_date", labDate);
                        fixData.setColumnValueMap(columnValueMap);
                        fixDataInfos.add(fixData);
                    });
                    break;
                case "相关病史.xlsx":
                    rows.forEach(row -> {
                        // 调整内容
                        String startDate = handleDateStr(row.get("开始时间"));
                        dates.add(startDate);
                        String stopDate = handleDateStr(row.get("结束时间"));
                        dates.add(stopDate);
                        String id = row.get("病史id");
                        result.add(String.format("UPDATE patient_medical_history SET start_date = '%s', stop_date = '%s' WHERE id = '%s';", startDate, stopDate, id));
                        FixData fixData = new FixData();
                        fixData.setId(id);
                        fixData.setTable("patient_medical_history");
                        Map<String, String> columnValueMap = Maps.newHashMap();
                        columnValueMap.put("start_date", startDate);
                        columnValueMap.put("stop_date", stopDate);
                        fixData.setColumnValueMap(columnValueMap);
                        fixDataInfos.add(fixData);
                    });
                    break;
                case "相关药物史.xlsx":
                    rows.forEach(row -> {
                        // 调整内容
                        String startDate = handleDateStr(row.get("开始时间"));
                        dates.add(startDate);
                        String stopDate = handleDateStr(row.get("结束时间"));
                        dates.add(stopDate);
                        String id = row.get("相关药物史id");
                        result.add(String.format("UPDATE patient_drug_history SET start_date = '%s', stop_date = '%s' WHERE id = '%s';", startDate, stopDate, id));
                        FixData fixData = new FixData();
                        fixData.setId(id);
                        fixData.setTable("patient_drug_history");
                        Map<String, String> columnValueMap = Maps.newHashMap();
                        columnValueMap.put("start_date", startDate);
                        columnValueMap.put("stop_date", stopDate);
                        fixData.setColumnValueMap(columnValueMap);
                        fixDataInfos.add(fixData);
                    });
                    break;
                case "剂量信息.xlsx":
                    rows.forEach(row -> {
                        // 调整内容
                        String startDate = handleDateStr(row.get("开始时间"));
                        dates.add(startDate);
                        String stopDate = handleDateStr(row.get("结束时间"));
                        dates.add(stopDate);
                        String id = row.get("剂量id");
                        result.add(String.format("UPDATE drug_dose SET start_date = '%s', end_date = '%s' WHERE id = '%s';", startDate, stopDate, id));
                        FixData fixData = new FixData();
                        fixData.setId(id);
                        fixData.setTable("drug_dose");
                        Map<String, String> columnValueMap = Maps.newHashMap();
                        columnValueMap.put("start_date", startDate);
                        columnValueMap.put("end_date", stopDate);
                        fixData.setColumnValueMap(columnValueMap);
                        fixDataInfos.add(fixData);
                    });
                    break;
                default:
            }
        }
        System.out.println(dates.size());
        System.out.println(result.size());
        System.out.println("================更新SQL");
        for (String updateSql : result) {
            System.out.println(updateSql);
        }
        // 导出
        exportFixSQLFile(result);
        System.out.println("================更新SQL");
    }

    private void exportFixSQLFile(List<String> result) {
        String updateSQLFilePath = String.format("C:\\Users\\shunjian.hu\\Desktop\\updateSql\\%s", System.nanoTime());
        // 生成文件夹
        mkdir(updateSQLFilePath);
        // 生成更新文件
        newFile(updateSQLFilePath + "\\" + "updateSQL.sql", String.join("\n", result));
    }

    /**
     * 时间字段replace搜索项
     */
    public static final String[] DATE_REPLACE_SEARCH_ITEMS = new String[]{"年", "月", "日", "时", "分", "秒", "/"};

    /**
     * 时间字段replace替换项
     */
    public static final String[] DATE_REPLACE_REPLACE_ITEMS = new String[]{"-", "-", "", ":", ":", "", "-"};

    private String handleDateStr(String date) {
        log.info("处理时间:[{}]", date);
        // 空 || 已u开头
        if (StringUtils.isBlank(date) || date.startsWith("u")) {
            return "";
        }
        // 替换年,月,日,时,分,秒,/
        date = StringUtils.replaceEach(date, DATE_REPLACE_SEARCH_ITEMS, DATE_REPLACE_REPLACE_ITEMS);
        // 不包含unkw uk 简单用u判断
        int index = date.indexOf('u');
        // 包含
        String result = index < 0 ? date : date.substring(0, index - 1);
        log.info("时间[{}]处理后结果[{}]", date, result);
        return result;
    }

    @ApiOperation("获取configMap数据")
    @PostMapping(value = "getConfigMap")
    public void getConfigMap(@RequestBody ConfigMapReq req) {
        Map<String, List<String>> v4v5ServicesMap = Maps.newLinkedHashMap();
        v4v5ServicesMap.put("v4", v4_services);
        v4v5ServicesMap.put("v5", V5_SERVICES);
        Map<String, String> result = Maps.newLinkedHashMap();
        v4v5ServicesMap.forEach((v, vServices) -> {
            // 查询配置
            Map<String, String> servicePropertiesMap = vServices.stream().collect(Collectors.toMap(service -> service, service -> QueryDBHelper.getConfigMapStr(service, restTemplate, req.getCookie()), (o, n) -> n, LinkedHashMap::new));
            // 替换数据
            servicePropertiesMap.forEach((service, serviceConfigMapStr) -> {
                System.out.println("configMap start -> " + service);
                replacePropertiesValue(servicePropertiesMap, result, v);
                System.out.println("configMap end -> " + service);
            });
        });
        // 替换之后生成文件
        result.forEach(this::writeFile);
        System.out.println();
    }

    private void writeFile(String serviceName, String properties) {
        String serviceFolder = String.format("C:\\Users\\shunjian.hu\\Desktop\\configMap\\%s", serviceName);
        // 创建文件夹
        mkdir(serviceFolder);
        // 文件的完整路径
        String filePath = String.format("%s\\%s.properties", serviceFolder, serviceName);
        newFile(filePath, properties);
        System.out.println("服务：" + serviceName + "已成功创建并写入文本到文件：" + filePath);
    }

    private void read(ExcelData excelData) {
        Set<String> eventSeriousness = Sets.newHashSet();
        List<String> seriousnessStrs = excelData.getSheetRowsMap().get("aer_file name Partner List").stream().map(row -> row.get("30")).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        for (String seriousnessStr : seriousnessStrs) {
            System.out.println(seriousnessStr);
            for (String s : seriousnessStr.split(",")) {
                if (s.contains(":") && s.split(":")[1].contains("Yes")) {
                    eventSeriousness.add(s.split(":")[0].trim());
                }
            }
        }
    }

    private static final List<String> ignore_keys = Lists.newArrayList("spring.application.name", "spring.datasource.url", "server.port");

    @ApiOperation("读取configMap数据")
    @PostMapping(value = "readConfigMap")
    public void readConfigMap(@RequestBody List<String> keys) {
        Path directory = Paths.get("D:\\project\\service");
        try (Stream<Path> paths = Files.walk(directory)) {
            // 过滤出所有的目录，并打印出来
            paths.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().equals("config"))
                    .filter(this::hasPropertiesFile)
                    .forEach(path -> showPropertiesKeyValue(path, keys))
            //.forEach(System.out::println)
            ;
        } catch (IOException e) {
            System.err.println("无法遍历目录: " + e.getMessage());
        }
    }

    @ApiOperation("读取文件夹下")
    @GetMapping(value = "readFolderExcel")
    public void readFolderExcel() {
        String folderPath = "C:\\Users\\shunjian.hu\\Desktop\\文件";
        List<ExportInfoDTO> all = Lists.newArrayList();
        try (Stream<Path> stream = Files.walk(Paths.get(folderPath), 1)) {
            for (Path path : stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xls") ||
                            path.toString().toLowerCase().endsWith(".xlsx")).collect(Collectors.toList())) {
                // 文件名
                System.out.println(path.getFileName());
                String fileName = path.getFileName().toString();
                // 批号
                String batchNo = fileName.split("批")[0].replace("第", "");
                // 总数
                String total = fileName.split("共")[1];
                // 读取信息
                String totalNum;
                String fileType;
                if (fileName.endsWith("行数据.xlsx")) {
                    // 导出数据
                    totalNum = total.replace("行数据.xlsx", "");
                    fileType = "导出行数据";
                } else {
                    // 报告id
                    totalNum = total.replace("个报告id.xlsx", "");
                    fileType = "报告id";
                }
                all.add(ExportInfoDTO.builder()
                        .batchNo(Integer.valueOf(batchNo))
                        .total(Integer.valueOf(totalNum))
                        .fileName(fileName)
                        .fileType(fileType)
                        .build());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        all.sort(Comparator.comparing(ExportInfoDTO::getBatchNo));
        System.out.println();
        //QueryDBHelper.exportExcel(headers, table, "结果");
        all.stream().collect(Collectors.groupingBy(ExportInfoDTO::getBatchNo)).forEach((batchNo, batchNoFiles) -> {
            if (batchNoFiles.size() != 2) {
                log.info("批号导出有问题:[{}]", batchNo);
            }
            batchNoFiles.forEach(batchNoFile -> {
                // 读取文件信息
                ExcelData excelData = readExcelData(folderPath, batchNoFile.getFileName());
                List<Map<String, String>> rows = excelData.getSheetRowsMap().get("Sheet1");
                if (rows.size() != batchNoFile.getTotal()) {
                    log.info("批号导出文件有问题:[{}],总数:[{}],实际总数:[{}]", batchNoFile.getFileName(), batchNoFile.getTotal(), rows.size());
                }
            });
        });
    }

    @ApiOperation("替换configMap数据")
    @PostMapping(value = "replaceConfigMap")
    public CompareServiceDTO replaceConfigMap(@RequestParam String inputCompareService) {
        // 读取目录下所有项目
        // 指定你要遍历的目录路径
        Path directory = Paths.get("D:\\project\\service");
        Map<String, Map<String, Map<Object, Object>>> envServicePropertyMap = Maps.newHashMap();
        try (Stream<Path> paths = Files.walk(directory)) {
            // 过滤出所有的目录，并打印出来
            paths.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().equals("config"))
                    .filter(path -> hasPropertiesFile(path, inputCompareService))
                    .forEach(path -> processPropertiesFiles(path, envServicePropertyMap))
                    //.forEach(System.out::println)
                    ;
        } catch (IOException e) {
            System.err.println("无法遍历目录: " + e.getMessage());
        }
        Map<String, Map<String, List<String>>> addPropertiesMap = Maps.newHashMap();
        //
        Map<String, Map<Object, Map<String, Object>>> updatePropertiesMap = Maps.newHashMap();
        // 需要对比的
        List<String> compareServices = Lists.newArrayList(inputCompareService);
        // 循环每个环境
        envServicePropertyMap.forEach((env, envServicesMap) -> {
            log.info("比对:[{}]", env);
            // 循环主要对比的
            compareServices.forEach(compareService -> {
                //
                Map<Object, Object> compareServicePropertiesMap = envServicesMap.get(compareService);
                // 与其他的比对
                envServicesMap.forEach((serviceName, servicePropertiesMap) -> {
                    if (!StringUtils.equals(serviceName, compareService)) {
                        List<Object> notHaveKeys = ListUtils.removeAll(compareServicePropertiesMap.keySet(), servicePropertiesMap.keySet());
                        // 打印
                        notHaveKeys.stream().filter(key -> !StringUtils.containsAny(key.toString(), ".flyway.", "eureka")).forEach(key -> {
                            //log.info("缺少key:[{}:{}]", key, compareServicePropertiesMap.get(key));
                            addPropertiesMap.computeIfAbsent(env, v -> Maps.newHashMap()).computeIfAbsent(serviceName, v -> Lists.newArrayList()).add(String.format("%s=%s", key, compareServicePropertiesMap.get(key)));
                        });
                        // 两边都有的,看下内容
                        List<Object> bothHaveKeys = ListUtils.retainAll(compareServicePropertiesMap.keySet(), servicePropertiesMap.keySet());
                        // 看下内容
                        bothHaveKeys.stream().filter(key -> !ignore_keys.contains(String.valueOf(key))).forEach(key -> {
                            Object o1 = compareServicePropertiesMap.get(key);
                            Object o2 = servicePropertiesMap.get(key);
                            if (ObjectUtils.notEqual(o1, o2)) {
                                log.info("两边都存在的key:[{}],compareService[name:{}=value:{}],serviceName:[name:{}=value:{}]", key, compareService, o1, serviceName, o2);
                                Map<String, Object> serviceKeyValueMap = updatePropertiesMap.computeIfAbsent(env, v -> Maps.newHashMap()).computeIfAbsent(key, v -> Maps.newHashMap());
                                serviceKeyValueMap.put(compareService, o1);
                                serviceKeyValueMap.put(serviceName, o2);
                            }
                        });
                    }
                });
            });
        });
        System.out.println();
        Map<String, Map<String, String>> addResultMap = Maps.newHashMap();
        addPropertiesMap.forEach((env, serviceAddPropertiesMap) ->
                serviceAddPropertiesMap.forEach((serviceName, addProperties) ->
                        addResultMap.computeIfAbsent(env, v -> Maps.newHashMap()).computeIfAbsent(serviceName, v -> String.join("\n", addProperties))));
        System.out.println();
        return new CompareServiceDTO(updatePropertiesMap, addResultMap);
    }

    @SneakyThrows
    private void showPropertiesKeyValue(Path configDir, List<String> keys) {
        try (Stream<Path> files = Files.list(configDir)) {
            files.filter(file -> file.toString().endsWith(".properties"))
                    .forEach(file -> {
                        try {
                            Properties properties = new Properties();
                            properties.load(Files.newInputStream(file));
                            String[] split = file.toString().split("\\\\");
                            String env = split[split.length - 1];
                            String service = split[split.length - 3];
                            if (StringUtils.equals(env, "itaimei-test.properties")) {
                                keys.forEach(key -> log.info("服务:[{}]-环境:[{}]-key:[{}]-value:[{}]", service, env, key, properties.getProperty(key)));
                            }
                        } catch (IOException e) {
                            System.err.println("无法处理文件: " + file + ", " + e.getMessage());
                        }
                    });
        }
    }

    @SneakyThrows
    private void processPropertiesFiles(Path configDir, Map<String, Map<String, Map<Object, Object>>> map) {
        try (Stream<Path> files = Files.list(configDir)) {
            files.filter(file -> file.toString().endsWith(".properties"))
                    .forEach(file -> {
                        try {
                            Properties properties = new Properties();
                            properties.load(Files.newInputStream(file));
                            String[] split = file.toString().split("\\\\");
                            String env = split[split.length - 1];
                            String service = split[split.length - 3];
                            Map<Object, Object> propertiesMap = Maps.newHashMap();
                            properties.forEach(propertiesMap::put);
                            // 打印文件中的所有键值对
                            //properties.forEach((key, value) -> System.out.printf("File: %s, Key: %s, Value: %s%n", file, key, value));
                            map.computeIfAbsent(env, v -> Maps.newHashMap()).put(service, propertiesMap);
                        } catch (IOException e) {
                            System.err.println("无法处理文件: " + file + ", " + e.getMessage());
                        }
                    });
        }
    }


    @SneakyThrows
    private void processPropertiesFiles(Path configDir) {
        try (Stream<Path> files = Files.list(configDir)) {
            files.filter(file -> file.toString().endsWith(".properties"))
                    .forEach(file -> {
                        try {
                            Properties properties = new Properties();
                            properties.load(Files.newInputStream(file));

                            // 打印文件中的所有键值对
                            properties.forEach((key, value) ->
                                    System.out.printf("File: %s, Key: %s, Value: %s%n",
                                            file, key, value));

                            // 如果需要修改属性值或保存文件，请取消注释以下代码
                            // properties.setProperty("newKey", "newValue");
                            // properties.store(Files.newOutputStream(file), null);

                        } catch (IOException e) {
                            System.err.println("无法处理文件: " + file + ", " + e.getMessage());
                        }
                    });
        }
    }

    @SneakyThrows
    private boolean hasPropertiesFile(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.anyMatch(file -> file.toString().endsWith(".properties"));
        }
    }

    @SneakyThrows
    private boolean hasPropertiesFile(Path dir, String compareService) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.anyMatch(file -> file.toString().endsWith(".properties") && StringUtils.containsAny(file.toString(), compareService, "pvs-report"));
        }
    }

    @SneakyThrows
    private void fdaError(ExcelData excelData) {
        List<Map<String, String>> rows = excelData.getSheetRowsMap().get("Rejection and Warning Rules");
        // 数据库字段信息
        List<Map<String, String>> dbInfos = excelData.getSheetRowsMap().get("db_info");
        Map<String, List<String>> fieldDbInfosMap = Maps.newHashMap();
        ObjectMapper mapper = new ObjectMapper();
        for (Map<String, String> dbInfo : dbInfos) {
            String fieldNos = dbInfo.get("3");
            Map<String, String> map = mapper.readValue(fieldNos, Map.class);
            for (String fieldNo : map.values()) {
                for (String s : fieldNo.split("/")) {
                    fieldDbInfosMap.put(s, Lists.newArrayList(dbInfo.get("0"), dbInfo.get("1"), dbInfo.get("2")));
                }
            }
        }
        List<String> errors = Lists.newArrayList();
        List<String> errorEnums = Lists.newArrayList();
        // 错误信息
        rows.forEach(row -> {
            String rejection = row.get("3");
            // 会被拒绝的
            if (StringUtils.equals(rejection, "ü")) {
                // 错误id
                String errorId = row.get("5");
                // 错误备注
                String errorDescription = row.get("6");
                String error = String.format("public static final ErrorInfo FDA_%s_ERROR = new ErrorInfo(\"FDA_%s_ERROR\", \"%s\");", errorId, errorId, errorDescription.replaceAll("\"", "'"));
                System.out.println(error);
                errors.add(error);
                String fieldNo = row.get("0");
                List<String> fieldNoDbInfos = fieldDbInfosMap.get(fieldNo);
                if (CollectionUtils.isNotEmpty(fieldNoDbInfos)) {
                    String errorEnum = String.format("FDA_%s(FDA_%s_ERROR, \"%s\", \"%s\", \"%s\", \"%s\"),", errorId, errorId, fieldNoDbInfos.get(0), fieldNoDbInfos.get(1), fieldNoDbInfos.get(2), fieldNo);
                    System.out.println(errorEnum);
                    errorEnums.add(errorEnum);
                }
            }
        });
        System.out.println();
        errors.forEach(System.out::println);
        errorEnums.forEach(System.out::println);

    }

    @SneakyThrows
    private Map<String, String> read(String fieldNumber, ObjectMapper mapper) {
        return mapper.readValue(fieldNumber, Map.class);
    }

    private void fda(ExcelData excelData) {
        List<Map<String, String>> rows = excelData.getSheetRowsMap().get("ICSR Data Elements");
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> nullFlavorInfos = excelData.getSheetRowsMap().get("null_flavor");
        Map<String, FDAFieldInfo> dbFieldInfoMap = Maps.newHashMap();
        for (Map<String, String> nullFlavorInfo : nullFlavorInfos) {
            String fieldNumber = nullFlavorInfo.get("0");
            String nullFlavor = nullFlavorInfo.get("1");
            String maxLength = nullFlavorInfo.get("2");
            String dbType = nullFlavorInfo.get("3");
            Map<String, String> map = read(fieldNumber, mapper);
            for (String fieldNo : map.values()) {
                for (String s : fieldNo.split("/")) {
                    dbFieldInfoMap.put(s, FDAFieldInfo.builder()
                            .fieldNo(s)
                            .dbMaxLength(maxLength)
                            .dbNullFlavorItems(StringUtils.isBlank(nullFlavor) ? Lists.newArrayList() : Lists.newArrayList(nullFlavor.split(",")))
                            .dbType(dbType)
                            .i18nKey(nullFlavorInfo.get("4"))
                            .fieldKey(nullFlavorInfo.get("5"))
                            .table(nullFlavorInfo.get("6"))
                            .columnName(nullFlavorInfo.get("7"))
                            .itemClassId(nullFlavorInfo.get("8"))
                            .build());
                }
            }
        }
        // FDA字段信息
        List<FDAFieldInfo> fdaFieldInfos = Lists.newArrayList();
        // 循环行
        rows.forEach(row -> {
            if (!StringUtils.equals(row.get("2"), "(Header)") && StringUtils.isNotBlank(row.get("2"))) {
                // nullFlavorItem
                Set<String> nullFlavorItems = Sets.newHashSet();
                nullFlavorItems.add(StringUtils.equals(row.get("20"), "Yes") ? "NI" : null);
                nullFlavorItems.add(StringUtils.equals(row.get("21"), "Yes") ? "MSK" : null);
                nullFlavorItems.add(StringUtils.equals(row.get("22"), "Yes") ? "UNK" : null);
                nullFlavorItems.add(StringUtils.equals(row.get("23"), "Yes") ? "NA" : null);
                nullFlavorItems.add(StringUtils.equals(row.get("24"), "Yes") ? "ASKU" : null);
                nullFlavorItems.add(StringUtils.equals(row.get("25"), "Yes") ? "NASK" : null);
                nullFlavorItems.add(StringUtils.equals(row.get("26"), "Yes") ? "NINF" : null);
                nullFlavorItems.add(StringUtils.equals(row.get("27"), "Yes") ? "PINF" : null);
                nullFlavorItems.add(StringUtils.equals(row.get("28"), "Yes") ? "OTH" : null);
                nullFlavorItems.remove(null);
                FDAFieldInfo fdaFieldInfo = FDAFieldInfo.builder()
                        .filedSource(row.get("0"))
                        .fieldNo(row.get("2"))
                        .maxlength(row.get("4"))
                        .dateType(row.get("5"))
                        .nullFlavorItems(nullFlavorItems)
                        .build();
                FDAFieldInfo dbFieldInfo = dbFieldInfoMap.get(fdaFieldInfo.getFieldNo());
                if (dbFieldInfo == null) {
                    log.info("缺少字段:[{}]", fdaFieldInfo.getFieldNo());
                    fdaFieldInfo.setDbNullFlavorItems(Lists.newArrayList());
                } else {
                    // 有的,判断
                    fdaFieldInfo.setDbMaxLength(dbFieldInfo.getDbMaxLength());
                    fdaFieldInfo.setDbNullFlavorItems(dbFieldInfo.getDbNullFlavorItems());
                    fdaFieldInfo.setDbType(dbFieldInfo.getDbType());
                    fdaFieldInfo.setI18nKey(dbFieldInfo.getI18nKey());
                    fdaFieldInfo.setFieldKey(dbFieldInfo.getFieldKey());
                    fdaFieldInfo.setTable(dbFieldInfo.getTable());
                    fdaFieldInfo.setColumnName(dbFieldInfo.getColumnName());
                    fdaFieldInfo.setItemClassId(dbFieldInfo.getItemClassId());
                }
                // 判断nullFlavor是否相同
                if (CollectionUtils.isNotEmpty(fdaFieldInfo.getDbNullFlavorItems()) || CollectionUtils.isNotEmpty(fdaFieldInfo.getNullFlavorItems())) {
                    log.info("判断字段:[{}]nullFlavor", fdaFieldInfo.getFieldNo());
                    boolean a = CollectionUtils.containsAll(fdaFieldInfo.getDbNullFlavorItems(), fdaFieldInfo.getNullFlavorItems());
                    boolean b = CollectionUtils.containsAll(fdaFieldInfo.getNullFlavorItems(), fdaFieldInfo.getDbNullFlavorItems());
                    fdaFieldInfo.setNullFlavorMatch(a && b);
                }
                if (StringUtils.isNotBlank(fdaFieldInfo.getMaxlength()) || StringUtils.isNotBlank(fdaFieldInfo.getDbMaxLength())) {
                    log.info("判断字段:[{}]maxLength", fdaFieldInfo.getFieldNo());
                    fdaFieldInfo.setMaxLengthMatch(StringUtils.equals(fdaFieldInfo.getMaxlength(), fdaFieldInfo.getDbMaxLength()));
                }
                fdaFieldInfos.add(fdaFieldInfo);
            }
        });
        System.out.println();
        // nullFlavor不匹配的
        fdaFieldInfos.stream().filter(fda -> BooleanUtils.isFalse(fda.getNullFlavorMatch())).forEach(fda -> {
            log.info("nullFlavor信息不匹配-字段:[{}],FDA支持的:[{}],系统支持的:[{}]", fda.getFieldNo(), String.join(",", fda.getNullFlavorItems()), String.join(",", fda.getDbNullFlavorItems()));
        });
        List<String> headers = Lists.newArrayList("字段", "FDA字段长度", "系统字段长度");
        List<List<String>> table = Lists.newArrayList();
        // 最大长度不匹配的
        fdaFieldInfos.stream().filter(fda -> !Lists.newArrayList("select", "date", "radio", "dateTypePicker").contains(fda.getDbType()) && !StringUtils.equals(fda.getMaxlength(), "-") && BooleanUtils.isFalse(fda.getMaxLengthMatch())).forEach(fda -> {
            log.info("maxLength信息不匹配-字段:[{}],FDA长度:[{}],系统长度:[{}]", fda.getFieldNo(), fda.getMaxlength(), fda.getDbMaxLength());
            table.add(Lists.newArrayList(fda.getFieldNo(), fda.getMaxlength(), fda.getDbMaxLength()));
        });
        System.out.println();
        // 字段配置
        // fdaFieldInfos.stream().filter(fda -> StringUtils.isNotBlank(fda.getDbType()) && Lists.newArrayList("select", "date", "radio", "dateTypePicker", "checkbox").contains(fda.getDbType()) && !StringUtils.equals(fda.getMaxlength(), "-")).forEach(fda -> {
        // fdaFieldInfos.stream().filter(fda -> StringUtils.isNotBlank(fda.getDbType()) && Lists.newArrayList("date", "dateTypePicker").contains(fda.getDbType())).forEach(fda -> {
        fdaFieldInfos.stream().filter(fda -> StringUtils.isNotBlank(fda.getDbType()) && Lists.newArrayList("select", "radio", "checkbox").contains(fda.getDbType())).forEach(fda -> {
            bb(fda);
        });
        //QueryDBHelper.exportExcel(headers.stream().map(Lists::newArrayList).collect(Collectors.toList()), table, "长度不匹配");
        System.out.println();
    }

    private void bb(FDAFieldInfo fda) {
        String format = String.format("%s(\n" +
                        "            %sDTO.class,\n" +
                        "            obj -> ((%sDTO)obj).get%s(),\n" +
                        "            \"%s\",\n" +
                        "            \"%s\",\n" +
                        "            \"%s\",\n" +
                        "            Lists.newArrayList(\n" +
                        "                    FieldConfigInfo.builder().fieldType(\"Item\").businessCode(\"FDA\").nullFlavorItems(Lists.newArrayList(%s)).itemClassId(\"%s\").build()\n" +
                        "            )\n" +
                        "    ),",
                fda.getTable().toUpperCase() + "_" + fda.getColumnName().toUpperCase(),
                StringUtils.capitalize(StringUtils.uncapitalize(toPascalCase(fda.getTable()))),
                StringUtils.capitalize(StringUtils.uncapitalize(toPascalCase(fda.getTable()))),
                StringUtils.capitalize(StringUtils.uncapitalize(toPascalCase(fda.getColumnName()))),
                StringUtils.uncapitalize(toPascalCase(fda.getColumnName())),
                fda.getI18nKey(),
                fda.getFieldNo(),
                CollectionUtils.isEmpty(fda.getNullFlavorItems()) ? "" : "\"" + String.join("\", \"", fda.getNullFlavorItems()) + "\"",
                fda.getItemClassId()
        );

        System.out.println();
        System.out.println(format);
    }

    private void aa(FDAFieldInfo fda) {
        String format = String.format("%s(\n" +
                        "            %sDTO.class,\n" +
                        "            obj -> ((%sDTO)obj).get%s(),\n" +
                        "            \"%s\",\n" +
                        "            \"%s\",\n" +
                        "            \"%s\",\n" +
                        "            Lists.newArrayList(\n" +
                        "                    FieldConfigInfo.builder().fieldType(\"DateString\").businessCode(\"FDA\").nullFlavorItems(Lists.newArrayList(%s)).build()\n" +
                        "            )\n" +
                        "    ),",
                fda.getTable().toUpperCase() + "_" + fda.getColumnName().toUpperCase(),
                StringUtils.capitalize(StringUtils.uncapitalize(toPascalCase(fda.getTable()))),
                StringUtils.capitalize(StringUtils.uncapitalize(toPascalCase(fda.getTable()))),
                StringUtils.capitalize(StringUtils.uncapitalize(toPascalCase(fda.getColumnName()))),
                StringUtils.uncapitalize(toPascalCase(fda.getColumnName())),
                fda.getI18nKey(),
                fda.getFieldNo(),
                CollectionUtils.isEmpty(fda.getNullFlavorItems()) ? "" : "\"" + String.join("\", \"", fda.getNullFlavorItems()) + "\""
        );

        System.out.println();
        System.out.println(format);
    }

    private static String toPascalCase(String snakeCaseStr) {
        String[] parts = snakeCaseStr.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    @ApiOperation("testNum")
    @GetMapping(value = "testNum")
    public void testNum() {
        String folderPath = "C:\\Users\\shunjian.hu\\Desktop";
        List<String> fileNames = Lists.newArrayList("Listing.xlsx");
        Map<String, List<String>> reportNoFileNameMap = Maps.newHashMap();
        fileNames.forEach(fileName -> {
            ExcelData excelData = readExcelData(folderPath, fileName);
            List<Map<String, String>> rows = excelData.getSheetRowsMap().get("Sheet1");
            rows.forEach(row -> reportNoFileNameMap.computeIfAbsent(row.get("报告编号"), v -> Lists.newArrayList()).add(fileName));
        });
        reportNoFileNameMap.forEach((reportNo, reportFileNames) -> {
            if (reportFileNames.size() != fileNames.size()) {
                System.out.println(reportNo);
            }
        });
    }

    @ApiOperation("读取Excel数据")
    @GetMapping(value = "readExcel")
    public void readExcel(@RequestParam String fileName,
                          @RequestParam ExcelAnalysisModel analysisModel) {
        ExcelData excelData = readExcelData(fileName, analysisModel);
        //fdaError(excelData);
        fda(excelData);
/*        List<String> insertSql = Lists.newArrayList();
        excelData.getSheetRowsMap().get("Sheet1").forEach(row -> {
            String date = row.get("时间");
            insertSql.add(String.format("INSERT INTO `hsj_test`.`test_date`(`id`, `date`) VALUES ('%s', '%s');", date, date));
        });
        Map<String, List<Map<String, String>>> sheetRowsMap = excelData.getSheetRowsMap();
        List<Map<String, String>> rows = sheetRowsMap.get("Sheet1");
        AtomicInteger index = new AtomicInteger(300);
        List<String> ch = Lists.newArrayList();
        List<String> en = Lists.newArrayList();
        rows.forEach(row -> {
            String a = row.get("1|中文翻译");
            String b = row.get("2|是否已有");
            if (!StringUtils.equals(a, b)) {
                String id = UUID.randomUUID().toString().replace("-", "");
                String uniqueCode = UUID.randomUUID().toString().replace("-", "");
                ch.add(uniqueCode + "=" + a);
                en.add(uniqueCode + "=" + row.get("0|english"));
                insertSql.add(String.format("INSERT INTO `pvs_middle_data`.`items`(`id`, `item_class_id`, `name`, `unique_code`, `sort_index`, `is_enable`, `version`, `create_time`, `create_by`, `update_time`, `update_by`, `is_deleted`, `e2b_value`, `is_r3`, `parent_unique_code`, `source_type`) VALUES ('%s', '689307c7-6af5-42fd-b63b-774f77e1c258', '%s', '%s', %d, 1, 0, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);",
                        id, a, uniqueCode, index.getAndAdd(10)));

            }
        });*/

        System.out.println();
    }



    private void fillExportInfo(QueryDBReq req, RestTemplate restTemplate) {
        List<XianShenReportNoRecord> all = xianShenReportNoDao.findAll();
        // 分批查询
        Lists.partition(all, 100).forEach(partReportNos -> {
            // 报告id
            List<String> reportIds = partReportNos.stream().map(XianShenReportNoRecord::getId).collect(Collectors.toList());
            // 查询产品
            List<Drug> drugs = queryDrug(req, reportIds, restTemplate);
            // 按照报告分组
            Map<String, List<Drug>> reportDrugsMap = drugs.stream().collect(Collectors.groupingBy(Drug::getReportId));
            // 循环处理
            partReportNos.forEach(xianShenReportNoRecord -> {
                // 获取药品信息
                List<Drug> reportDrugs = reportDrugsMap.get(xianShenReportNoRecord.getId());
                if (CollectionUtils.isEmpty(reportDrugs)) {
                    log.info("报告:[{}]没有产品", xianShenReportNoRecord.getId());
                } else {
                    // 产品排序
                    List<Drug> sortDrugs = sortTmDrugs(reportDrugs, Lists.newArrayList(new Items("aaa14e3d-053a-4bee-9c91-9da75b20b9e4"), new Items("e073d9c1-c4a9-4af1-a5c3-0675b40ddea6"), new Items("f68a6421-9dab-4e7e-aa75-1d0948acc025")));
                    Drug firstDrug = sortDrugs.get(0);
                    if (!StringUtils.equals(firstDrug.getDrugType(), "aaa14e3d-053a-4bee-9c91-9da75b20b9e4")) {
                        log.info("报告:[{}]第一个药品[{}]不是怀疑产品", xianShenReportNoRecord.getId(), firstDrug.getId());
                    }
                    // 取第一个
                    xianShenReportNoRecord.setFirstDrugGenericName(firstDrug.getGenericName());
                }
            });
        });
        xianShenReportNoDao.update(all);
    }

    public List<Drug> getOurCompanyDrugs(List<Drug> items) {
        if (CollectionUtils.isEmpty(items)) {
            return new ArrayList<>();
        }
        // 本公司药物
        return items.stream().filter(tmDrugDto -> StringUtils.isNotBlank(tmDrugDto.getManufacture()))
                .filter(tmDrugDto -> StringUtils.isNotBlank(tmDrugDto.getPsurDrugId())).collect(Collectors.toList());
    }

    public List<Drug> sortTmDrugs(List<Drug> tmDrugListDto, List<Items> items) {
        tmDrugListDto = tmDrugListDto == null ? Lists.newArrayList() : tmDrugListDto;
        // 本公司药物
        List<Drug> ourCompanyDrugs = getOurCompanyDrugs(tmDrugListDto);
        Sets.SetView<Drug> otherCompanyDrugs = Sets.difference(Sets.newHashSet(tmDrugListDto), Sets.newHashSet(ourCompanyDrugs));
        List<Drug> drugDtos = new ArrayList<>();
        List<Drug> collect;
        //根据药物类型排序
        for (Items item : items) {
            if (CollectionUtils.isNotEmpty(ourCompanyDrugs)) {
                collect = ourCompanyDrugs.stream().filter(c -> item.getUniqueCode().equalsIgnoreCase(c.getDrugType())).collect(Collectors.toList());
                sortDrugs(drugDtos, collect);
            }
            if (CollectionUtils.isNotEmpty(otherCompanyDrugs)) {
                collect = otherCompanyDrugs.stream().filter(c -> item.getUniqueCode().equalsIgnoreCase(c.getDrugType())).collect(Collectors.toList());
                sortDrugs(drugDtos, collect);
            }
        }
        //TODO: Drugtype 可能出现非空非正确数据丢失
        sortDrugs(drugDtos, ourCompanyDrugs.stream().filter(c -> StringUtils.isBlank(c.getDrugType())).collect(Collectors.toList()));
        sortDrugs(drugDtos, otherCompanyDrugs.stream().filter(c -> StringUtils.isBlank(c.getDrugType())).collect(Collectors.toList()));
        // 盲态的药品排在第一位
        if (CollectionUtils.isNotEmpty(drugDtos)) {
            Collections.sort(drugDtos, new Comparator<Drug>() {
                public int compare(Drug o1, Drug o2) {
                    if (null != o1.getUnblindingType() && TmDrugUnblindingTypeEnum.unblinding.getType() == o1.getUnblindingType()) {
                        return -1;
                    }
                    if (null != o2.getUnblindingType() && TmDrugUnblindingTypeEnum.unblinding.getType() == o2.getUnblindingType()) {
                        return 1;
                    }
                    return 0;
                }
            });
        }
        return drugDtos;
    }

    private void sortDrugs(List<Drug> drugDtos, List<Drug> collect) {
        if (CollectionUtils.isNotEmpty(collect)) {
            Collections.sort(collect, (Object o1, Object o2) -> {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                Drug drug1 = (Drug) o1;
                Drug drug2 = (Drug) o2;

                if (drug1.getCreateTime() == null) {
                    return 1;
                }
                if (drug2.getCreateTime() == null) {
                    return -1;
                }

                int compare = drug1.getCreateTime().compareTo(drug2.getCreateTime());
                if (compare == 0) {
                    return StringUtils.defaultString(drug1.getGenericName()).compareTo(StringUtils.defaultString(drug2.getGenericName())); //避免导入药物创建时间一直的第二排序字段
                }
                return compare;
            });
            drugDtos.addAll(collect);
        }
    }

    private List<Drug> queryDrug(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate) {
        // 查询报告下的药品数据
        String querySql = String.format("select id, BrandName, GenericName, ReportId, Manufacture, psur_drug_id, DrugType, create_time, unblinding_type from tm_drug where ReportId in ('%s') and is_deleted = 0", String.join("','", reportIds));
        // 按照报告分组
        req.setSqlContent(querySql);
        List<Drug> drugs = Lists.newArrayList();
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            Drug drug = new Drug();
            drug.setId(row.get(0));
            drug.setBrandName(row.get(1));
            drug.setGenericName(row.get(2));
            drug.setReportId(row.get(3));
            drug.setManufacture(row.get(4));
            drug.setPsurDrugId(row.get(5));
            drug.setDrugType(row.get(6));
            drug.setCreateTime(row.get(7));
            drug.setUnblindingType(row.get(8) == null ? null :Integer.valueOf(row.get(8)));
            drugs.add(drug);
        }
        return drugs;
    }


    private void replacePropertiesValue(Map<String, String> servicePropertiesMap, Map<String, String> result, String v) {
        // 替换数据
        servicePropertiesMap.forEach((service, properties) -> {
            String[] split = properties.split("\n");
            for (int i = 0; i < split.length; i++) {
                split[i] = replacePropertiesValue(v, service, split[i]);
            }
            result.put(service, StringUtils.joinWith("\n", split));
        });
    }
}
