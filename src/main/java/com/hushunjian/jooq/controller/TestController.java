package com.hushunjian.jooq.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hushunjian.jooq.helper.ExcelData;
import com.hushunjian.jooq.helper.ExcelDataHelper;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.excel.EasyExcelFactory.write;
import static com.alibaba.excel.EasyExcelFactory.writerSheet;

@Slf4j
@RequestMapping("test")
@RestController(value = "test")
public class TestController {


    @Resource
    private RestTemplate restTemplate;

    private static final List<String> v4_services = Lists.newArrayList("esae-template-service");

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

            "pvs-report-service",
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

    static {
        // 全部替换的
        REPLACE_VALUE_MAP.put("spring.redis.host", "spring.redis.host=192.168.132.57:6379");
        REPLACE_VALUE_MAP.put("spring.redis.password", "spring.redis.password=redis123");
        REPLACE_VALUE_MAP.put("spring.data.mongodb.uri", "spring.data.mongodb.uri=mongodb://admin:Taimei@123.@192.168.132.57:27017/pv?replicaSet=prod_pv_rs&authSource=admin");
        REPLACE_VALUE_MAP.put("spring.rabbitmq.host", "spring.rabbitmq.host=192.168.132.55:15672");
        REPLACE_VALUE_MAP.put("spring.rabbitmq.username", "spring.rabbitmq.username=admin");
        REPLACE_VALUE_MAP.put("spring.rabbitmq.password", "spring.rabbitmq.password=Taimei@123.");
        REPLACE_VALUE_MAP.put("spring.elasticsearch.rest.uris", "spring.elasticsearch.rest.uris=192.168.132.55:9200,192.168.132.56:9200,192.168.132.57:9200");
        REPLACE_VALUE_MAP.put("spring.elasticsearch.rest.username", "spring.elasticsearch.rest.username=admin");
        REPLACE_VALUE_MAP.put("spring.elasticsearch.rest.password", "spring.elasticsearch.rest.password=FBzn39tOUaiZLfGb");
        REPLACE_VALUE_MAP.put("com.taimeitech.framework.zkAddress", "com.taimeitech.framework.zkAddress=192.168.132.57:2181,192.168.132.55:2181,192.168.132.56:2181");
        REPLACE_VALUE_MAP.put("spring.kafka.bootstrap-servers", "spring.kafka.bootstrap-servers=192.168.132.57:9092,192.168.132.56:9092,192.168.132.55:9092");
        REPLACE_VALUE_MAP.put("spring.datasource.username", "spring.datasource.username=root");
        REPLACE_VALUE_MAP.put("spring.datasource.password", "spring.datasource.password=taimei@123");

        // V5部分替换的
        V5_REPLACE_PART_VALUE_MAP.put("spring.datasource.url", Pair.of("prod-mysql-pv-caps-dmp.taimei.com:3310", "192.168.132.62:3307"));
        // V4部分替换的
        V4_REPLACE_PART_VALUE_MAP.put("spring.datasource.url", Pair.of("prod-mysql-pv-dmp.taimei.com:3310", "192.168.132.61:3307"));

        V4_V5_REPLACE_PART_VALUE_MAP.put("v4", V4_REPLACE_PART_VALUE_MAP);
        V4_V5_REPLACE_PART_VALUE_MAP.put("v5", V5_REPLACE_PART_VALUE_MAP);
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
            // 替换数据
            servicePropertiesMap.forEach((service, properties) -> {
                String[] split = properties.split("\n");
                for (int i = 0; i < split.length; i++) {
                    split[i] = replacePropertiesValue(v, service, split[i]);
                }
                result.put(service, StringUtils.joinWith("\n", split));
            });
        });
        System.out.println();
    }

    @ApiOperation("导出线上数据到本地,传入搜索条件")
    @PostMapping(value = "test3")
    public void test3(@RequestBody QueryDBReq req) {
        initCellMaxTextLength();
        D res = getRes(req);
        System.out.println();
        exportExcel(res.getData(), req.getFileName());
    }

    @ApiOperation("导出线上数据到本地,写代码")
    @PostMapping(value = "exportData")
    public void exportData(@RequestBody QueryDBReq req) {
        // 导出最大
        req.setLimitNum("0");
        // 需要导出的库和表
        Map<String, List<String>> dbTablesMap = Maps.newHashMap();
        dbTablesMap.put("esafety5_e2b", Lists.newArrayList("e2b_r3_estri_item_config", "e2b_r3_m2estri_config"));
        dbTablesMap.put("pvs_middle_data", Lists.newArrayList("items", "item_class"));
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
                D res = getRes(req);
                // 输出所有的表
                List<List<String>> allTables = res.getData().getRows();
                // 然后循环每一个表,输出建表语句
                allTables.forEach(tables -> {
                    String table = tables.get(0);
                    // 输出这个表的建表语句
                    String showCreateTableSql = "SHOW CREATE TABLE " + table;
                    req.setSqlContent(showCreateTableSql);
                    // 查询
                    D res1 = getRes(req);
                    List<List<String>> createTables = res1.getData().getRows();
                    System.out.println(createTables.get(0).get(1));
                    instanceDBCreateTableSqlMap.computeIfAbsent(instanceDb, v -> Lists.newArrayList()).add(createTables.get(0).get(1));
                    // 判断这个表是不是配置表
                    if (configTables.contains(table)) {
                        // 输出这个表所有的列
                        String showColumnSql = String.format("SELECT column_name FROM information_schema.columns WHERE table_name = '%s'", table);
                        req.setSqlContent(showColumnSql);
                        D res2 = getRes(req);
                        // 列
                        List<String> columns = Lists.newArrayList();
                        res2.getData().getRows().forEach(resColumns -> columns.add(resColumns.get(0)));
                        String sql = String.format("CONCAT(\"INSERT INTO `%s`(`%s`) VALUES ('\", %s ,\"');\")", table, String.join("`, `", columns),
                                columns.stream().map(column -> String.format("IFNULL(%s,'')", column)).collect(Collectors.joining(", \"', '\", ")));
                        String configSql = String.format("SELECT %s FROM %s WHERE tenant_id IN ('default', 'system')", sql, table);
                        // 查询数据
                        req.setSqlContent(configSql);
                        D res3 = getRes(req);
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
                // "gvp",
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
            "signature_log_detail"
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
                // 导出文件
                exportExcel(exportDataMap, dbPath, db);
            });
        });
    }

    @ApiOperation("导出线上表结构和数据,自己写的")
    @PostMapping(value = "exportProdTableAndData")
    public void exportProdTableAndData(@RequestBody QueryDBReq req) {
        // 配置数据
        Map<String, Map<String, List<String>>> instanceDbTablesMap = Maps.newHashMap();
        //
        instanceDbTablesMap.computeIfAbsent("prod-mysql-pvcaps-ro", v -> Maps.newHashMap()).computeIfAbsent("esafety5_e2b", v -> Lists.newArrayList()).add("e2b_r3_estri_item_config");
        instanceDbTablesMap.computeIfAbsent("prod-mysql-pvcaps-ro", v -> Maps.newHashMap()).computeIfAbsent("esafety5_e2b", v -> Lists.newArrayList()).add("e2b_r3_m2estri_config");
        instanceDbTablesMap.computeIfAbsent("prod-mysql-pvcaps-ro", v -> Maps.newHashMap()).computeIfAbsent("pvs_middle_data", v -> Lists.newArrayList()).add("items");
        instanceDbTablesMap.computeIfAbsent("prod-mysql-pvcaps-ro", v -> Maps.newHashMap()).computeIfAbsent("pvs_middle_data", v -> Lists.newArrayList()).add("item_class");
        exportDbTables(req, instanceDbTablesMap);
    }

    @ApiOperation("导出线上表结构和数据,所有的")
    @PostMapping(value = "exportProdTable")
    public void exportProdTable(@RequestBody QueryDBReq req) {
        // 放到最大
        initCellMaxTextLength();
        // 需要处理的数据库
        Map<String, List<String>> dbMap = Maps.newHashMap();
        // 默认的
        dbMap.putAll(dbMap());
        // dbMap.put("prod-mysql-pvscommon-ro", Lists.newArrayList("pvs_common"));
        // dbMap.put("prod-mysql-pvcaps-ro", Lists.newArrayList("esafety5_e2b"));
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
        // 导出文件
        exportFile(all);
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
        // 页数据
        D pageSystemData = null;
        // 所有数据
        List<List<String>> allSystemData = Lists.newArrayList();
        // 查询总数
        int count = countSystemData(req, tableName, tenantColumn);
        log.info("处理表[{}]数据导出总条数:[{}]", tableName, count);
        // 总页数
        int totalPage = count / 5000;
        // 分页查询
        for (int i = 0; i <= totalPage; i++) {
            log.info("处理表[{}]数据导出,进度:[{}-{}]", tableName, i, totalPage);
            // 当前页数据
            pageSystemData = findPageSystemData(req, tableName, tenantColumn, i * 5000, 5000);
            // 追加行数据
            allSystemData.addAll(pageSystemData.getData().getRows().stream().map(Lists::newArrayList).collect(Collectors.toList()));
        }
        if (pageSystemData == null) {
            throw new RuntimeException("查询失败");
        }
        // 标题
        List<List<String>> headers = pageSystemData.getData().getColumn_list().stream().map(Lists::newArrayList).collect(Collectors.toList());
        return Pair.of(headers, allSystemData);
    }

    private D findPageSystemData(QueryDBReq req, String tableName, String tenantColumn, int off, int size) {
        String findSql;
        if (StringUtils.isBlank(tenantColumn)) {
            findSql = String.format("SELECT * FROM %s LIMIT %d, %d;", tableName, off, size);
        } else {
            findSql = String.format("SELECT * FROM %s WHERE %s IN ('system', 'default', 'sae-preview-system-default') LIMIT %d, %d;", tableName, tenantColumn, off, size);
        }
        // 设置最大
        req.setLimitNum("5000");
        req.setSqlContent(findSql);
        // 查询
        return getRes(req);
    }

    private Integer countSystemData(QueryDBReq req, String tableName, String tenantColumn) {
        String countSql;
        if (StringUtils.isBlank(tenantColumn)) {
            countSql = String.format("SELECT count(1) FROM %s;", tableName);
        } else {
            countSql = String.format("SELECT count(1) FROM %s WHERE %s IN ('system', 'default', 'sae-preview-system-default');", tableName, tenantColumn);
        }
        req.setSqlContent(countSql);
        // 查询
        D res = getRes(req);
        // 获取总数
        return Integer.valueOf(res.getData().getRows().get(0).get(0));
    }

    private boolean hasSystemData(QueryDBReq req, String tableName, String tenantColumn) {
        return countSystemData(req, tableName, tenantColumn) != 0;
    }



    private boolean needExportData(QueryDBReq req, String tableName, String createTableSql) {
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
        D res = getRes(req);
        return res.getData().getRows().get(0).get(1);
    }


    private List<String> showAllTables(QueryDBReq req) {
        // 输出所有的表
        String showTablesSql = "show tables";
        req.setSqlContent(showTablesSql);
        D res = getRes(req);
        List<String> allTables = Lists.newArrayList();
        res.getData().getRows().forEach(tables -> allTables.add(tables.get(0)));
        return allTables;
    }

    private void exportExcel(DData resData, String fileName) {
        List<List<String>> headers = resData.getColumn_list().stream().map(Lists::newArrayList).collect(Collectors.toList());
        // 生成临时文件
        File tempFile = genTempFile(fileName);
        List<List<String>> table = resData.getRows().stream().map(Lists::newArrayList).collect(Collectors.toList());
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

    private void exportExcel(Map<String, Pair<List<List<String>>, List<List<String>>>> exportDataMap,
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

    private WriteSheet createSheet(List<List<String>> headers, String sheetName, Integer sheetNo) {
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


    private WriteSheet createSheet(List<List<String>> headers) {
        return createSheet("Sheet1", headers);
    }

    private WriteSheet createSheet(String sheetName, List<List<String>> headers) {
        // 创建sheet,并设置表头样式
        WriteSheet sheet = writerSheet(sheetName).registerWriteHandler(getHandler()).build();
        // 设置表头
        sheet.setHead(headers);
        // 设置限制
        return sheet;
    }

    private WriteHandler getHandler() {
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

    private File genTempFile(String fileName) {
        return genTempFile("C:\\Users\\shunjian.hu\\Desktop", genName(fileName));
    }

    private File genTempFile(String path, String fileName) {
        return Paths.get(path, fileName).toFile();
    }

    private String genName(String fileName) {
        return String.format("%s.xlsx", StringUtils.isBlank(fileName) ? UUID.randomUUID().toString() : fileName);
    }

    @Data
    static class D {
        private String msg;

        private DData data;
    }

    @Data
    static class DData {

        private List<String> column_list;

        private List<List<String>> rows;
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


    @SneakyThrows
    private D getRes(QueryDBReq req) {
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
        return body;
    }


    @Data
    static class QueryDBReq {

        @ApiModelProperty(value = "cookie")
        private String cookie;

        @ApiModelProperty(value = "csrfToken")
        private String csrfToken;

        @ApiModelProperty(value = "实例名")
        private String instanceName;

        @ApiModelProperty(value = "数据库名")
        private String dbName;

        @ApiModelProperty(value = "查询数据库返回条数")
        private String limitNum;

        @ApiModelProperty(value = "SQL语句")
        private String sqlContent;

        @ApiModelProperty(value = "文件名称")
        private String fileName;
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

    @Data
    static class A {
        private String content;
    }

    @SneakyThrows
    @ApiOperation("修复时间里有uk的")
    @PostMapping(value = "fixDateUkSql")
    public void fixSql(@RequestBody List<String> fileNames) {
        // 输出的更新语句
        List<String> result = Lists.newArrayList();
        List<String> files = Lists.newArrayList("", "", "", "");
        files.add("实验室检查");
        files.add("相关病史");
        files.add("相关药物史");
        files.add("剂量信息");
        if (CollectionUtils.isNotEmpty(fileNames)) {
            files.addAll(fileNames);
        }
        Set<String> dates = Sets.newHashSet();
        for (String fileName : files) {
            String filePath = String.format("C:\\Users\\shunjian.hu\\Desktop\\%s.xlsx", fileName);
            ExcelData excelData = null;
            try {
                excelData = ExcelDataHelper.readExcelData(new FileInputStream(filePath));
            } catch (FileNotFoundException exception) {
                log.info("文件:[{}]不存在,结束", fileName);
                continue;
            }
            // 获取内容
            List<Map<String, String>> rows = excelData.getSheetRowsMap().get("Sheet1");
            switch (fileName) {
                case "实验室检查":
                    rows.forEach(row -> {
                        // 调整内容
                        String labDate = handleDateStr(row.get("实验室检查时间"));
                        dates.add(labDate);
                        result.add(String.format("UPDATE patient_lab_data SET lab_date = '%s' WHERE id = '%s';", labDate, row.get("实验室检查id")));
                    });
                    break;
                case "相关病史":
                    rows.forEach(row -> {
                        // 调整内容
                        String startDate = handleDateStr(row.get("开始时间"));
                        dates.add(startDate);
                        String stopDate = handleDateStr(row.get("结束时间"));
                        dates.add(stopDate);
                        result.add(String.format("UPDATE patient_medical_history SET start_date = '%s', stop_date = '%s' WHERE id = '%s';", startDate, stopDate, row.get("病史id")));
                    });
                    break;
                case "相关药物史":
                    rows.forEach(row -> {
                        // 调整内容
                        String startDate = handleDateStr(row.get("开始时间"));
                        dates.add(startDate);
                        String stopDate = handleDateStr(row.get("结束时间"));
                        dates.add(stopDate);
                        result.add(String.format("UPDATE patient_drug_history SET start_date = '%s', stop_date = '%s' WHERE id = '%s';", startDate, stopDate, row.get("相关药物史id")));
                    });
                    break;
                case "剂量信息":
                    rows.forEach(row -> {
                        // 调整内容
                        String startDate = handleDateStr(row.get("开始时间"));
                        dates.add(startDate);
                        String stopDate = handleDateStr(row.get("结束时间"));
                        dates.add(stopDate);
                        result.add(String.format("UPDATE drug_dose SET start_date = '%s', stop_date = '%s' WHERE id = '%s';", startDate, stopDate, row.get("剂量id")));
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
        System.out.println("================更新SQL");
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
}
