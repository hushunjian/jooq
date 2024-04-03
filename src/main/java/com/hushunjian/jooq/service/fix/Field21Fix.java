package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.Drug;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field21Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 删除剂量（G.k.4.r.1a）
        List<String> fixSql = Lists.newArrayList();
        // 剂量ids
        List<String> drugDoseIds = Lists.newArrayList();
        // 查询剂量
        Lists.partition(handleReportNos, 200).forEach(partReportNos ->
                queryIds(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, drugDoseIds));
        // 清空剂量
        drugDoseIds.forEach(drugDoseId -> fixSql.add(String.format("update tm_drug_dose set dose_number = '' where id = '%s';", drugDoseId)));
        return fixSql;
    }

    private void queryIds(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<String> drugDoseIds) {
        String querySql = String.format("select id, dose_number, dose_unit from tm_drug_dose where drug_id in (select id from tm_drug where reportId in ('%s') and is_deleted = 0) and (dose_number is not null and dose_number != '') and (dose_unit is null or dose_unit = '') and is_deleted = 0", String.join("','", reportIds));
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            drugDoseIds.add(row.get(0));
        }
    }

    @Override
    public String fixField() {
        return "请选择剂量单位|产品信息|剂量单位（G.k.4.r.1b）";
    }

    @Override
    public int getOrder() {
        return 21;
    }
}
