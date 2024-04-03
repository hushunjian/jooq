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
public class Field24Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，因果评价模块，评价记录中，字段“停用或减量后，反应/事件是否消失或减轻（G.k.9.i.CN.2）”为空时，
        // 将该字段补充选择“不详”
        List<String> fixSql = Lists.newArrayList();
        // 评价id
        List<String> evaluationIds = Lists.newArrayList();
        // 查询评价
        Lists.partition(handleReportNos, 200).forEach(partReportNos ->
                queryIds(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, evaluationIds));
        // 补充选择“不详”
        evaluationIds.forEach(evaluationId -> fixSql.add(String.format("update tm_causality set Dechallenge = 'bc0db3f2-78a4-4cbf-9bb4-23f63c959b61' where id = '%s';", evaluationId)));
        return fixSql;
    }

    private void queryIds(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<String> evaluationIds) {
        String querySql = String.format("select id, Dechallenge from tm_causality where ReportId in ('%s') and is_deleted = 0 and (Dechallenge is null or Dechallenge = '');", String.join("','", reportIds));
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            evaluationIds.add(row.get(0));
        }
    }

    @Override
    public String fixField() {
        return "该字段为必填项，请补充完善该值|报告评价|停用或减量后，反应/事件是否消失或减轻（G.k.9.i.CN.2）";
    }

    @Override
    public int getOrder() {
        return 24;
    }
}
