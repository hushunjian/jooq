package com.hushunjian.jooq.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hushunjian.jooq.dao.ReportCommonQueryDao;
import com.hushunjian.jooq.dao.SchemaDao;
import com.hushunjian.jooq.generator.tables.records.ReportCommonQueryRecord;
import com.hushunjian.jooq.generator.tables.records.SchemaFieldRecord;
import com.hushunjian.jooq.generator.tables.records.SchemaModuleRecord;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.*;
import com.hushunjian.jooq.res.LogEventRes;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;

@Slf4j
@RequestMapping("reportCommonQuery")
@RestController(value = "reportCommonQuery")
public class ReportCommonQueryController {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private SchemaDao schemaDao;

    @Resource
    private ReportCommonQueryDao reportCommonQueryDao;


    @ApiOperation("获取所有标签信息")
    @PostMapping(value = "getAll")
    public void getAll(@RequestBody QueryDBReq req) {
        // 查询esafety-test3租户的schemaField数据
        List<SchemaFieldRecord> allSchemaFields = schemaDao.findAllSchemaFields();
        // 按照报告字段id转map处理
        Map<String, String> reportFieldIdMap = allSchemaFields.stream().collect(Collectors.toMap(SchemaFieldRecord::getReportFieldId, SchemaFieldRecord::getId));
        // count所有
        int total = reportCommonQueryDao.count();
        // 页大小
        int pageSize = 5000;
        // 总页数
        int totalPage = totalPage(total, pageSize);
        // 所有字段id
        Set<String> allSchemaFieldIds = Sets.newHashSet();
        // 分页查询处理
        for (int i = 0; i < totalPage; i++) {
            // 查询当前页数据
            List<ReportCommonQueryRecord> pageRecords = reportCommonQueryDao.getPage(i, pageSize);
            // 字段id
            Set<String> schemaFieldIds = Sets.newHashSet();
            // 模块id
            Set<String> moduleFieldIds = Sets.newHashSet();
            // 当前页所有查询数据
            List<CommonSearchItemDto> pageAllSearchItems = Lists.newArrayList();
            // 收集所有的字段id,模块id
            pageRecords.forEach(reportCommonQueryRecord -> {
                // 转对象
                AdvanceSearchDTO advanceSearch = parseObject(reportCommonQueryRecord.getQueryCondition().toString(), AdvanceSearchDTO.class);
                // 获取所有的高级检索数据
                advanceSearch.getCommonSearchDTO().getSearchItems().forEach(searchItem -> {
                    schemaFieldIds.add(searchItem.getFieldId());
                    moduleFieldIds.add(searchItem.getPageId());
                    pageAllSearchItems.add(searchItem);
                });
                reportCommonQueryRecord.setVersion(reportCommonQueryRecord.getVersion() + 1);
            });
            // 查询字段信息
            Pair<Map<String, Map<String, String>>, Map<String, Map<String, String>>> pair = getProdInfoMap(req, schemaFieldIds, moduleFieldIds);
            // 在循环一次,转换成测试环境的字段id
            pageAllSearchItems.forEach(searchItem -> {
                // 测试环境schemaFieldId
                String testSchemaFieldId = reportFieldIdMap.get(pair.getKey().get(searchItem.getFieldId()).get("report_field_id"));
                if (StringUtils.isBlank(testSchemaFieldId)) {
                    throw new RuntimeException();
                }
                allSchemaFieldIds.add(testSchemaFieldId);
            });
            // 更新version
            reportCommonQueryDao.batchUpdate(pageRecords);
        }
        // 所有的字段id
        log.info("所有用于查询的字段id:[{}]", String.join("','", allSchemaFieldIds));
    }

    private Pair<Map<String, Map<String, String>>, Map<String, Map<String, String>>> getProdInfoMap(QueryDBReq req, Collection<String> schemaFieldIds, Collection<String> moduleFieldIds) {
        String queryFieldInfoSql = String.format("select * from schema_field where id in ('%s');", String.join("','", schemaFieldIds));
        req.setSqlContent(queryFieldInfoSql);
        List<Map<String, String>> schemaFieldRows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "report_field_id", "module_id"));
        // 根据id转map处理
        Map<String, Map<String, String>> schemaFieldMap = schemaFieldRows.stream().collect(Collectors.toMap(row -> row.get("id"), row -> row));
        // 查询模块信息
        String queryModuleInfoSql = String.format("select * from schema_module where id in ('%s');", String.join("','", moduleFieldIds));
        req.setSqlContent(queryModuleInfoSql);
        List<Map<String, String>> schemaModuleRows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "code"));
        // 根据id转map处理
        Map<String, Map<String, String>> schemaModuleMap = schemaModuleRows.stream().collect(Collectors.toMap(row -> row.get("id"), row -> row));
        return Pair.of(schemaFieldMap, schemaModuleMap);
    }

    private static int totalPage(int totalCount, int pageSize) {
        if (pageSize == 0) {
            return 0;
        } else {
            return totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        }
    }

    @ApiOperation("查询线上查询标签")
    @PostMapping(value = "queryReportCommonQuery")
    public void queryReportCommonQuery(@RequestBody QueryDBReq req) {
        String querySql = "select * from report_common_query where is_deleted = 0 and tenant_id not in ('1110', 'pv-esae', 'global01-pv', 'local01-pv', 'local02-eng-pv') order by length(query_condition) desc;";
        req.setSqlContent(querySql);
        List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "name", "query_condition"));
        // 查询esafety-test3租户的schemaField数据
        List<SchemaFieldRecord> allSchemaFields = schemaDao.findAllSchemaFields();
        // 按照报告字段id转map处理
        Map<String, String> reportFieldIdMap = allSchemaFields.stream().collect(Collectors.toMap(SchemaFieldRecord::getReportFieldId, SchemaFieldRecord::getId));
        // 查询esafety-test3租户的schemaModule数据
        List<SchemaModuleRecord> allSchemaModules = schemaDao.findAllSchemaModules();
        // 按照模块code转map处理
        Map<String, String> codeIdMap = allSchemaModules.stream().collect(Collectors.toMap(SchemaModuleRecord::getCode, SchemaModuleRecord::getId));
        // 字段ids
        Set<String> schemaFieldIds = Sets.newHashSet();
        // 字段模块ids
        Set<String> schemaModuleIds = Sets.newHashSet();
        // 循环获取字段
        rows.forEach(row -> {
            String queryCondition = row.get("query_condition");
            // 转对象
            CommonSearchDTO commonSearch = parseObject(queryCondition, CommonSearchDTO.class);
            // 找出schema数据
            List<CommonSearchItemDto> searchItems = commonSearch.getSearchItems();
            if (CollectionUtils.isNotEmpty(searchItems)) {
                searchItems.forEach(searchItem -> {
                    schemaFieldIds.add(searchItem.getFieldId());
                    schemaModuleIds.add(searchItem.getPageId());
                });
            }
        });
        Pair<Map<String, Map<String, String>>, Map<String, Map<String, String>>> pair = getProdInfoMap(req, schemaFieldIds, schemaModuleIds);
        Map<String, Map<String, String>> schemaFieldMap = pair.getKey();
        Map<String, Map<String, String>> schemaModuleMap = pair.getValue();
        // 新增SQL
        List<String> insertSql = Lists.newArrayList();
        // 所有字段id
        Set<String> allSchemaFieldIds = Sets.newHashSet();
        // 替换生产id
        rows.forEach(row -> {
            String queryCondition = row.get("query_condition");
            // 转对象
            CommonSearchDTO commonSearch = parseObject(queryCondition, CommonSearchDTO.class);
            // 找出schema数据
            List<CommonSearchItemDto> searchItems = commonSearch.getSearchItems();
            if (CollectionUtils.isNotEmpty(searchItems)) {
                // 替换里面的id
                searchItems.forEach(searchItem -> {
                    // 测试环境schemaFieldId
                    String testSchemaFieldId = reportFieldIdMap.get(schemaFieldMap.get(searchItem.getFieldId()).get("report_field_id"));
                    // 测试环境schemaModule
                    String testSchemaModuleId = codeIdMap.get(schemaModuleMap.get(searchItem.getPageId()).get("code"));
                    if (StringUtils.isAnyBlank(testSchemaFieldId, testSchemaModuleId)) {
                        throw new RuntimeException();
                    }
                    allSchemaFieldIds.add(testSchemaFieldId);
                    searchItem.setFieldId(testSchemaFieldId);
                    searchItem.setPageId(testSchemaModuleId);
                });
            }
            insertSql.add(String.format("INSERT INTO `report_common_query`(`id`, `name`, `query_condition`, `tenant_id`, `version`, `create_time`, `create_by`, `update_time`, `update_by`, `is_deleted`) VALUES ('%s', '%s', '%s', 'esafety-test3', 0, '2026-01-06 10:45:08.047000', '8ac0b8cd89c090f50189cdbd0c330182', NULL, '8ac0b8cd89c090f50189cdbd0c330182', 0);",
                    row.get("id"), row.get("name"), JSON.toJSONString(commonSearch)));
        });
        System.out.println();
    }

    @ApiOperation("查询日志中的高级检索")
    @PostMapping(value = "queryLogEventSearchAdvance")
    public void queryLogEvent(@RequestBody QueryLogEventReq req) {
        queryAll(req);
    }

    private void queryAll(QueryLogEventReq req) {
        // 设置查询时间范围：2025年1月1日 00:00:00 开始
        LocalDateTime startDateTime = LocalDateTime.of(2025, 6, 5, 0, 0, 0);
        // 查询到当前时间
        LocalDateTime endDateTime = LocalDateTime.now();
        // 6小时间隔
        Duration interval = Duration.ofHours(6);
        // 开始时间
        LocalDateTime currentStart = startDateTime;
        Pattern pattern = Pattern.compile("requestBody=(.+?), startTime=");
        Pattern timePattern = Pattern.compile("F\\s+(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3})");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        while (currentStart.isBefore(endDateTime)) {
            LocalDateTime currentEnd = currentStart.plus(interval);
            if (currentEnd.isAfter(endDateTime)) {
                currentEnd = endDateTime;
            }
            // 转换为纳秒级时间戳
            long startTimestamp = currentStart.toEpochSecond(ZoneOffset.UTC) * 1_000_000_000L;
            long endTimestamp = currentEnd.toEpochSecond(ZoneOffset.UTC) * 1_000_000_000L;
            // 构建URL
            String url = String.format(
                    "http://logevent.taimei.com/prod/api/v1/loki/query_range?app=pvs-report&start=%d&end=%d&level=&limit=2000&size=20&middleStart=&middleEnd=&pod=&all=true&dsc=true&filters[]=searchAdvance&filters[]=RequestLogClientImpl",
                    startTimestamp, endTimestamp
            );
            req.setUrl(url);
            LogEventRes res = QueryDBHelper.getRes(req, restTemplate);
            if (res != null && res.getQuery() != null) {
                // 获取信息
                List<String> messages = res.getQuery().stream().map(info -> info.getInfo().getMessage()).collect(Collectors.toList());
                List<ReportCommonQueryRecord> reportCommonQueryRecords = Lists.newArrayList();
                // 提取数据
                messages.forEach(message -> {
                    String query = findGroup(pattern, message);
                    ReportCommonQueryRecord reportCommonQueryRecord = new ReportCommonQueryRecord();
                    reportCommonQueryRecord.setId(UUID.randomUUID().toString().replace("-", ""));
                    reportCommonQueryRecord.setQueryCondition(org.jooq.JSON.json(query));
                    String time = findGroup(timePattern, message);
                    LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
                    reportCommonQueryRecord.setCreateTime(localDateTime);
                    reportCommonQueryRecords.add(reportCommonQueryRecord);
                });
                try {
                    reportCommonQueryDao.batchInsert(reportCommonQueryRecords);
                } catch (Exception ex) {

                }

            }
            // 移动到下一个时间段
            currentStart = currentEnd;
        }
    }

    private String findGroup(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "没找到";
    }


}
