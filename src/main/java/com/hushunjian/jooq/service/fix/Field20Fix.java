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
public class Field20Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告分类为上市后境内报告或上市后境外报告，并且“是否为本持有人产品（G.k.CN.3）“字段为空时，
        // 将字段“是否为本持有人产品（G.k.CN.3）”选择“否”
        List<String> fixSql = Lists.newArrayList();
        // 需要更新的产品信息
        List<Drug> drugs = Lists.newArrayList();
        // 查询产品
        Lists.partition(handleReportNos, 200).forEach(partReportNos ->
                queryDrug(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, drugs));
        // 更新产品信息
        drugs.forEach(drug -> fixSql.add(String.format("update tm_drug set self_drug_product = 'a9992944-8d9d-4081-aaa8-ac4d9f4a6d08' where id = '%s';", drug.getId())));
        return fixSql;
    }

    private void queryDrug(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<Drug> drugs) {
        String querySql = String.format("select id, self_drug_product from tm_drug where ReportId in ('%s') and is_deleted = 0 and (self_drug_product is null or self_drug_product = '')", String.join("','", reportIds));
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            Drug drug = new Drug();
            drug.setId(row.get(0));
            drug.setSelfDrugProduct(row.get(1));
            drugs.add(drug);
        }
    }

    @Override
    public String fixField() {
        return "当报告分类为上市后境内报告或上市后境外报告时，则该字段为必填，请补充完善该值|产品信息|是否为本持有人产品（G.k.CN.3）";
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
