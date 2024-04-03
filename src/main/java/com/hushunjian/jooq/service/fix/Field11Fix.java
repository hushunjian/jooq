package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.generator.tables.records.MedicalHistoryRecord;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field11Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，相关病史模块下，病史类型为“家族药物史“和”既往药物史“的记录中，字段药物名称（通用名称）“为空时，
        // -将“药物名称(商品名称)”的值赋值到“药物名称(通用名称)”上
        List<String> fixSql = Lists.newArrayList();
        // 相关病史
        List<MedicalHistoryRecord> medicalHistoryRecords = Lists.newArrayList();
        // 查询报告中病史类型为“家族药物史“和”既往药物史“的记录中，字段药物名称（通用名称）“为空的
        Lists.partition(handleReportNos, 200).forEach(partReportNos -> {
            // 查询相关病史
            queryMedicalHistory(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, medicalHistoryRecords);
        });
        // 循环赋值
        medicalHistoryRecords.forEach(medicalHistoryRecord -> fixSql.add(String.format("update tm_othermedicalhistory set TreatmentDrug = MedHistoryDrugBrandName where id = '%s';", medicalHistoryRecord.getId())));
        return fixSql;
    }

    private void queryMedicalHistory(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<MedicalHistoryRecord> medicalHistoryRecords) {
        String queryMedicalSql = String.format("select id, MedicalHistoryType as '类型', ReportId as 'report_id', MedHistoryDrugBrandName as '商品名称', TreatmentDrug as '通用名称'  from tm_othermedicalhistory where ReportId in (%s) and is_deleted = 0 and MedicalHistoryType in ('bba6aa95-fafd-4487-a973-f5f7c89f5dad', 'be434fc2-2bd0-4cb2-a174-458ee19764aa') and (TreatmentDrug is null or TreatmentDrug = '')", "'" + StringUtils.join(reportIds, "','") + "'");
        req.setSqlContent(queryMedicalSql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            MedicalHistoryRecord medicalHistoryRecord = new MedicalHistoryRecord();
            medicalHistoryRecord.setId(row.get(0));
            medicalHistoryRecord.setType(row.get(1));
            medicalHistoryRecord.setReportId(row.get(2));
            medicalHistoryRecord.setProductName(row.get(3));
            medicalHistoryRecord.setCommonName(row.get(4));
            medicalHistoryRecords.add(medicalHistoryRecord);
        }
    }


    @Override
    public String fixField() {
        return "请填写药物通用名称|相关病史|药物名称(通用名称)（D.8.r.1）";
    }

    @Override
    public int getOrder() {
        return 11;
    }
}
