package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.Lab;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field14Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，字段“检查结果”和“结果单位”均为空时，
        // 将字段“检查结果/单位“补充”不详”
        List<String> fixSql = Lists.newArrayList();
        // 实验室检查
        List<Lab> labs = Lists.newArrayList();
        // 查询报告中实验室字段“检查结果”和“结果单位”均为空时，
        Lists.partition(handleReportNos, 300).forEach(partReportNos -> {
            // 查询实验室检查
            queryLab(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, labs);
        });
        // 循环赋值
        labs.forEach(lab -> fixSql.add(String.format("update tm_labdata set resultUnstructuredData = '不详' where id = '%s';", lab.getId())));
        return fixSql;
    }

    private void queryLab(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<Lab> labs) {
        String querySql = String.format("select * from tm_labdata where ReportId in (%s) and (LabDataResults is null or LabDataResults = '') and (Units is null or Units = '') and (LabTest is not null and LabTest != '') and (LabTestAssessment is null or LabTestAssessment = '') and (resultUnstructuredData is null or resultUnstructuredData = '') and is_deleted = 0", "'" + StringUtils.join(reportIds, "','") + "'");
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            Lab lab = new Lab();
            lab.setId(row.get(0));
            labs.add(lab);
        }
    }


    @Override
    public String fixField() {
        return "请填写检查结果/单位|实验室检查|检查结果/单位（F.r.3.4）";
    }

    @Override
    public int getOrder() {
        return 14;
    }
}
