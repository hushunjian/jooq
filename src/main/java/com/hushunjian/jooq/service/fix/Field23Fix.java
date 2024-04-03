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
public class Field23Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告分类为上市后境内报告或上市前境内报告或上市前境外报告，并且字段“批准文号/受理号（G.k.CN.4）“为空时，
        // 将字段“批准文号/受理号（G.k.CN.4）”补充填写“不详”
        List<String> fixSql = Lists.newArrayList();
        // 产品ids
        List<String> drugIds = Lists.newArrayList();
        // 查询产品
        Lists.partition(handleReportNos, 200).forEach(partReportNos ->
                queryIds(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, drugIds));
        // 批准文号/受理号（G.k.CN.4）”补充填写“不详”
        drugIds.forEach(drugId -> fixSql.add(String.format("update tm_drug set LicenceNumber = '不详' where id = '%s';", drugId)));
        return fixSql;
    }

    private void queryIds(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<String> drugIds) {
        String querySql = String.format("select id, LicenceNumber from tm_drug where ReportId in ('%s') and is_deleted = 0 and (LicenceNumber is null or LicenceNumber = '');", String.join("','", reportIds));
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            drugIds.add(row.get(0));
        }
    }

    @Override
    public String fixField() {
        return "当报告分类为上市后境内报告或上市前境内报告或上市前境外报告时，该字段为必填，请补充完善该值|产品信息|批准文号/受理号（G.k.CN.4）";
    }

    @Override
    public int getOrder() {
        return 23;
    }
}
