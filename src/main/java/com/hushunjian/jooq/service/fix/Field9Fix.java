package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.generator.tables.records.ReporterRecord;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field9Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // null flavor选择“UNK”
        List<String> fixSql = Lists.newArrayList();
        // 报告者信息
        List<ReporterRecord> reporterRecords = Lists.newArrayList();
        // 查询报告的首要报告者
        Lists.partition(handleReportNos, 200).forEach(partReportNos -> {
            // 查询首要报告者信息
            queryPrimaryReporters(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, reporterRecords);
        });
        // 按照报告toMap, 一个报告最多只会有一个首要报告者
        Map<String, ReporterRecord> reporterRecordMap = reporterRecords.stream().collect(Collectors.toMap(ReporterRecord::getReportId, reporterRecord -> reporterRecord, (o, n) -> n));
        // 循环报告
        handleReportNos.forEach(handleReportNo -> {
            ReporterRecord reporterRecord = reporterRecordMap.get(handleReportNo.getId());
            if (reporterRecord != null) {
                // 有报告者信息,插入一条UNK的nullFlavor
                fixSql.add(String.format("INSERT INTO `null_flavor_info`(`id`, `item_id`, `value`, `report_id`, `field_name`, `tenant_id`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`, `version`) VALUES ('%s', '%s', 'UNK', '%s', 'reporteroccupation', 'af632df0-3999-4a35-9c2d-7269b4ecc6a0', 'system', '2024-03-29 10:35:06.000000', NULL, NULL, 0, 0);", UUID.randomUUID(), reporterRecord.getId(), handleReportNo.getId()));
            }
        });
        return fixSql;
    }

    private void queryPrimaryReporters(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<ReporterRecord> reporterRecords) {
        String queryReporterSql = String.format("select id, PrimaryReporter as 'primary_reporter', ReporterCountry as 'reporter_country', ReportId as 'report_id', ReporterOccupation as 'reporter_occupation' from tm_reporter where ReportId in (%s) and is_deleted = 0 and PrimaryReporter = '3464e6b0-358a-4ef5-911c-c2b7cd438ae0'", "'" + StringUtils.join(reportIds, "','") + "'");
        req.setSqlContent(queryReporterSql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            ReporterRecord reporterRecord = new ReporterRecord();
            reporterRecord.setId(row.get(0));
            reporterRecord.setPrimaryReporter(row.get(1));
            reporterRecord.setReporterCountry(row.get(2));
            reporterRecord.setReportId(row.get(3));
            reporterRecord.setReporterOccupation(row.get(4));
            reporterRecords.add(reporterRecord);
        }
    }

    @Override
    public String fixField() {
        return "首要报告者选择是，需填写报告者职业|报告者信息|报告者职业（C.2.r.4）";
    }

    @Override
    public int getOrder() {
        return 9;
    }
}
