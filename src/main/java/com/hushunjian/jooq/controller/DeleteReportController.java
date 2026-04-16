package com.hushunjian.jooq.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.FixData;
import com.hushunjian.jooq.res.FixData;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("deleteReport")
@RestController(value = "deleteReport")
public class DeleteReportController {

    @Resource
    private RestTemplate restTemplate;

    private static final List<String> reportIds = Lists.newArrayList(
            "8a8d83f69cbd8c9c019d1a24da07184b",
            "8a8d83f69cbd8c9c019d1a24da071852",
            "8a8d83f69cbd8c9c019d1a24da07185f",
            "8a8d83f69cbd8c9c019d1a24da071869",
            "8a8d83f69cbd8c9c019d1a24da07188e",
            "8a8d83f69cbd8c9c019d1a24da0718ab",
            "8a8d83f69cbd8c9c019d1a24dd901955",
            "8a8d83f69cbd8c9c019d1a24debd197f",
            "8a8d83f69cbd8c9c019d1a24e01019b3",
            "8a8d83f69cbd8c9c019d1a24e19a19e1",
            "8a8d83f69cbd8c9c019d1a24e30e1a0b",
            "8a8d83f69cbd8c9c019d1a24e47d1a48",
            "8a8d83f69cbd8c9c019d1a24e6331a7a",
            "8a8d83f69cbd8c9c019d1a24e7581aad",
            "8a8d83f69cbd8c9c019d1a24e9151adf",
            "8a8d83f69cbd8c9c019d1a24ea3c1b09",
            "8a8d83f69cbd8c9c019d1a24ebb01b57",
            "8a8d83f69cbd8c9c019d1a24ed781b9d",
            "8a8d83f69cbd8c9c019d1a24eecc1bc4",
            "8a8d83f69cbd8c9c019d1a24f0471bf3",
            "8a8d83f69cbd8c9c019d1a24f2101c22",
            "8a8d83f69cbd8c9c019d1a24f32e1c48",
            "8a8d83f69cbd8c9c019d1a24f4e21c7a",
            "8a8d83f69cbd8c9c019d1a24f60c1ca5",
            "8a8d83f69cbd8c9c019d1a24f7831cd4",
            "8a8d83f69cbd8c9c019d1a24f8f61d02",
            "8a8d83f69cbd8c9c019d1a24fa7e1d34",
            "8a8d83f69cbd8c9c019d1a24fc0e1d6e",
            "8a8d83f69cbd8c9c019d1a24fd9d1da6",
            "8a8d83f69cbd8c9c019d1a24ff0f1de7",
            "8a8d83f69cbd8c9c019d1a2500971e1c",
            "8a8d83f69cbd8c9c019d1a2502011e47",
            "8a8d83f69cbd8c9c019d1a2503711e71",
            "8a8d83f69cbd8c9c019d1a2504e51ea0",
            "8a8d83f69cbd8c9c019d1a2506901eda",
            "8a8d83f69cbd8c9c019d1a2507b91f25",
            "8a8d83f69cbd8c9c019d1a25093a1f58",
            "8a8d83f69cbd8c9c019d1a250ad91f85",
            "8a8d83f69cbd8c9c019d1a250c4d1fb1",
            "8a8d83f69cbd8c9c019d1a250df32005",
            "8a8d83f69cbd8c9c019d1a250f52203e",
            "8a8d83f69cbd8c9c019d1a2510c4206d",
            "8a8d83f69cbd8c9c019d1a251268209a",
            "8a8d83f69cbd8c9c019d1a2513c720c7",
            "8a8d83f69cbd8c9c019d1a25153a20fc",
            "8a8d83f69cbd8c9c019d1a251696212e",
            "8a8d83f69cbd8c9c019d1a2518932165",
            "8a8d83f69cbd8c9c019d1a251a3b2194",
            "8a8d83f69cbd8c9c019d1a251b6521c6",
            "8a8d83f69cbd8c9c019d1a251cdd21f5",
            "8a8d83f69cbd8c9c019d1a251e55222c",
            "8a8d83f69cbd8c9c019d1a251fcd2263",
            "8a8d83f69cbd8c9c019d1a25218c229d",
            "8a8d83f69cbd8c9c019d1a2522c922c1",
            "8a8d83f69cbd8c9c019d1a2524b922f7",
            "8a8d83f69cbd8c9c019d1a2525df2321",
            "8a8d83f69cbd8c9c019d1a25276a235e",
            "8a8d83f69cbd8c9c019d1a2528da238a",
            "8a8d83f69cbd8c9c019d1a252a3923bf",
            "8a8d83f69cbd8c9c019d1a252bca23ed",
            "8a8d83f69cbd8c9c019d1a252db52434",
            "8a8d83f69cbd8c9c019d1a252f412466",
            "8a8d83f69cbd8c9c019d1a2530842493",
            "8a8d83f69cbd8c9c019d1a2531ff24cc",
            "8a8d83f69cbd8c9c019d1a2533fe2507",
            "8a8d83f69cbd8c9c019d1a253550253b",
            "8a8d83f69cbd8c9c019d1a2537252572",
            "8a8d83f69cbd8c9c019d1a25386c2599",
            "8a8d83f69cbd8c9c019d1a2539f025c9",
            "8a8d83f69cbd8c9c019d1a253b7325fe",
            "8a8d83f69cbd8c9c019d1a253cf9263c",
            "8a8d83f69cbd8c9c019d1a253e792680",
            "8a8d83f69cbd8c9c019d1a25400526bc",
            "8a8d83f69cbd8c9c019d1a2541cc26eb",
            "8a8d83f69cbd8c9c019d1a2542f82714",
            "8a8d83f69cbd8c9c019d1a25449a277a",
            "8a8d83f69cbd8c9c019d1a2545c327b1",
            "8a8d83f69cbd8c9c019d1a25479927ee",
            "8a8d83f69cbd8c9c019d1a254938281e",
            "8a8d83f69cbd8c9c019d1a254ad0284a",
            "8a8d83f69cbd8c9c019d1a254c042872",
            "8a8d83f69cbd8c9c019d1a254dc228ae",
            "8a8d83f69cbd8c9c019d1a25526228f9",
            "8a8d83f69cbd8c9c019d1a2553e02922",
            "8a8d83f69cbd8c9c019d1a25554c2955",
            "8a8d83f69cbd8c9c019d1a255b5729a9",
            "8a8d83f69cbd8c9c019d1a255f7929ea",
            "8a8d83f69cbd8c9c019d1a2563742a24",
            "8a8d83f69cbd8c9c019d1a2567ce2a75",
            "8a8d83f69cbd8c9c019d1a256bc92aa5",
            "8a8d83f69cbd8c9c019d1a256fb92ad1",
            "8a8d83f69cbd8c9c019d1a25738b2b01",
            "8a8d83f69cbd8c9c019d1a2577832b32",
            "8a8d80539d1a3164019d1a63c8fd108b",
            "8a8d80539d1a3164019d1a649f2d1356",
            "8a8d80539d1a3164019d1a6524ca14c8",
            "8a8d80539d1a3164019d1a66c6401b65",
            "8a8d80539d1a3164019d1a6787321d01",
            "8a8d80539d1a3164019d1a67ba2c1d35",
            "8a8d80539d1a3164019d1a68d2b71ee5",
            "8a8d80539d1a3164019d1a6907821f1f",
            "8a8d80539d1a3164019d1a6943ef1f5e",
            "8a8d80539d1a3164019d1a69f85c2052"
    );

    // 需要删除的报告数据表
    private static final List<String> needDeletedReportTables = Lists.newArrayList(
            // 报告信息表
            "patient_medical_history",
            "patient_family_drug_history",
            "report_description",
            "evaluation",
            "blind_drug",
            "report_document",
            "blind_file",
            "patient_death",
            "patient_lab_data",
            "report_value",
            "report_summary",
            "patient_autopsy",
            "evaluation_causality",
            "drug_substance",
            "report_project",
            "patient_parent_drug_history",
            "patient_parent_medical_history",
            "meddra_field_info",
            "report_sender_diagnosis",
            "patient_baby",
            "patient_pregnancy",
            "drug_reason",
            "patient_parent_base",
            "drug",
            "patient_medical_other",
            "reporter",
            "report_attachment",
            "med_watch",
            "related_to_code",
            "null_flavor_info",
            "patient_drug_history",
            "evaluation_naranjo",
            "patient_vaccine",
            "whodrug_field_info",
            "patient",
            "evaluation_national_way",
            "adverse_event",
            "drug_dose",
            "sender",
            // 报告流程信息
            "task_report_info",
            "task_report_log_detail"
    );

    @ApiOperation("物理删除线上报告")
    @PostMapping(value = "deleteReport")
    public void deleteReport(@RequestBody QueryDBReq req) {
        req.setLimitNum("0");
        // 租户id
        String tenantId = "";
        // 报告编号
        List<String> reportNos = Lists.newArrayList();
        // 根据报告编号查询报告id sql
        String queryReportIdSql = String.format("select id from report_value where tenant_id = '%s' and safety_report_id in ('%s');", tenantId, String.join("','", reportNos));
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        req.setSqlContent(queryReportIdSql);
        // 查询报告id
        List<Map<String, String>> idRows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id"));
        // 取出所有的报告id
        List<String> reportIds = idRows.stream().map(row -> row.get("id")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(reportIds)) {
            return;
        }
        // 需要执行的SQL
        List<String> deletedSQls = Lists.newArrayList();
        // 循环报告表,删除报告数据
        needDeletedReportTables.forEach(tableName -> deletedSQls.add(String.format("delete from pvs_report_all.%s where report_id in ('%s');", tableName, String.join("','", reportIds))));
        // 查询多语言分表
        String queryShardingTable = String.format("select * from t_sharding_mapping where tenant_id = '%s';", tenantId);
        req.setSqlContent(queryShardingTable);
        // 查询报告分表
        List<Map<String, String>> shardingTableNames = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("sharding_table_name"));
        // 租户多语言表
        String tenantMultilingualValueTable = shardingTableNames.get(0).get("sharding_table_name");
        // 删除多语言表下的数据
        deletedSQls.add(String.format("delete from pvs_report_all.%s where report_id in ('%s');", tenantMultilingualValueTable, String.join("','", reportIds)));
        // track数据现在不删

        // 删除首页任务
        // 1.先根据报告查到工作流任务
        String queryReportTaskInfoIdSql = String.format("select id from task_report_info where report_id in ('%s');", String.join("','", reportIds));
        req.setSqlContent(queryReportTaskInfoIdSql);
        // 查询报告流程id
        List<Map<String, String>> reportTaskInfoRows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id"));
        // 所有任务id
        List<String> reportTaskInfoIds = reportTaskInfoRows.stream().map(row -> row.get("id")).collect(Collectors.toList());
        // 删除首页任务
        deletedSQls.add(String.format("delete from gvp_workbench.task_info where unique_key in ('%s');", String.join("','", reportTaskInfoIds)));
    }


    @ApiOperation("移动报告")
    @PostMapping(value = "removeReport")
    public void removeReport(@RequestBody QueryDBReq req) {
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        req.setLimitNum("0");
        String targetTenantId = "1110";
        // 修改表数据
        Map<String, List<FixData>> tableFixDataMap = Maps.newHashMap();
        // 循环报告表,调整租户id
        needDeletedReportTables.forEach(table -> {
            String querySql = String.format("select * from %s where report_id in ('%s') and tenant_id != '%s';", table, String.join("','", reportIds), targetTenantId);
            req.setSqlContent(querySql);
            // 查询获取id
            List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id"));
            // 组装参数
            if (CollectionUtils.isNotEmpty(rows)) {
                // 修改租户id
                Map<String, String> columnValueMap = Maps.newHashMap();
                columnValueMap.put("tenant_id", targetTenantId);
                putTrail_2728ac(table, req, columnValueMap);
                // 生成修复SQL
                rows.forEach(row -> tableFixDataMap.computeIfAbsent(table, v -> Lists.newArrayList()).add(FixData.builder()
                        .id(row.get("id"))
                        .table(String.format("pvs_report_all.%s", table))
                        .columnValueMap(columnValueMap)
                        .build()));
            }
        });
        // 再把多语言表的数据删掉
        // 原租户id
        String sourceTenantId = "8a81813e763fc30f017646131ddd51ce";
        String queryMultilingualValueTableSql = String.format("select * from t_sharding_mapping where tenant_id = '%s'", sourceTenantId);
        req.setSqlContent(queryMultilingualValueTableSql);
        String multilingualValueTableName = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("sharding_table_name")).get(0).get("sharding_table_name");
        // 这些租户下的多语言数据全删
        String queryMultilingualValueRowSql = String.format("select * from %s where report_id in ('%s')", multilingualValueTableName, String.join("','", reportIds));
        req.setSqlContent(queryMultilingualValueRowSql);
        // 查询
        List<Map<String, String>> queryMultilingualValueRows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id"));
        // 组装条件
        if (CollectionUtils.isNotEmpty(queryMultilingualValueRows)) {
            Map<String, String> deleteColumnValueMap = Maps.newHashMap();
            deleteColumnValueMap.put("is_deleted", "1");
            putTrail_2728ac(multilingualValueTableName, req, deleteColumnValueMap);
            // 生成修复SQL
            queryMultilingualValueRows.forEach(row -> tableFixDataMap.computeIfAbsent(multilingualValueTableName, v -> Lists.newArrayList()).add(FixData.builder()
                    .id(row.get("id"))
                    .table(String.format("pvs_report_all.%s", multilingualValueTableName))
                    .columnValueMap(deleteColumnValueMap)
                    .build()));
        }
        // 输出内容
        System.out.println("开始修复");
        tableFixDataMap.forEach((tableName, fixInfos) -> ListUtils.partition(fixInfos, 500).forEach(row -> {
            System.out.printf("修复表数据:[%s]%n", tableName);
            System.out.println(JSON.toJSONString(row, SerializerFeature.DisableCircularReferenceDetect));
        }));
    }

    private void putTrail_2728ac(String table, QueryDBReq req, Map<String, String> columnValueMap) {
        String showCreateTableSql = "SHOW CREATE TABLE " + table;
        req.setSqlContent(showCreateTableSql);
        // 查询
        D res1 = QueryDBHelper.getRes(req, restTemplate);
        List<List<String>> createTables = res1.getData().getRows();
        if (createTables.get(0).get(1).contains("trail_2728ac")) {
            columnValueMap.put("trail_2728ac", "null");
        }
    }



    @ApiOperation("test3")
    @PostMapping(value = "test3")
    public void test3(@RequestBody List<String> ids) {
        List<FixData> result = Lists.newArrayList();
        ids.forEach(id -> {
            Map<String, String> columnValueMap = Maps.newHashMap();
            columnValueMap.put("is_deleted", "1");
            result.add(FixData.builder().id(id).table("gvp_workbench.task_info").columnValueMap(columnValueMap).build());
        });
        System.out.println();
    }

}
