package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field22Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告的剂量模块中，字段“使用时长单位“有值，且”使用时长“为空时，
        // 将“使用时长单位（G.k.4.r.6b）”的字段值清空
        List<String> fixSql = Lists.newArrayList();
        // 剂量ids
        List<String> drugDoseIds = Lists.newArrayList();
        // 查询剂量
        Lists.partition(handleReportNos, 200).forEach(partReportNos ->
                queryIds(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, drugDoseIds));
        // 清空使用时长单位（G.k.4.r.6b）
        drugDoseIds.forEach(drugDoseId -> fixSql.add(String.format("update tm_drug_dose set duration_unit = '' where id = '%s';", drugDoseId)));
        return fixSql;
    }

    private void queryIds(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<String> drugDoseIds) {
        String querySql = String.format("select id, duration_number, duration_unit from tm_drug_dose where drug_id in (select id from tm_drug where reportId in ('%s') and is_deleted = 0) and (duration_unit is not null and duration_unit != '') and (duration_number is null or duration_number = '') and is_deleted = 0;", String.join("','", reportIds));
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            drugDoseIds.add(row.get(0));
        }
    }

    @Override
    public String fixField() {
        return "请填写用药时长|产品信息|使用时长（G.k.4.r.6a）";
    }

    @Override
    public int getOrder() {
        return 22;
    }
}
