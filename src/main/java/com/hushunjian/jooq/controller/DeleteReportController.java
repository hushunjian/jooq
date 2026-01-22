package com.hushunjian.jooq.controller;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

}
