package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.generator.tables.records.RelatedToCodeRecord;
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
public class Field13Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 该选项不属于R3字典值，可将结果及单位填写至检查“结果/单位”字段中
        // 1、将检查结果和单位填写至检查结果/单位（F.r.3.4）字段中 格式：检查结果 单位
        // 2、填写后，将原检查结果和单位字段清空
        List<String> fixSql = Lists.newArrayList();
        // 实验室检查
        List<Lab> labs = Lists.newArrayList();
        // 查询报告中实验室检查F.r.3.3该选项不属于R3字典值，可将结果及单位填写至检查“结果/单位”字段中
        Lists.partition(handleReportNos, 200).forEach(partReportNos -> {
            // 查询实验室检查
            queryLab(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, labs);
        });
        // 循环赋值
        labs.forEach(lab -> fixSql.add(String.format("update tm_labdata set LabDataResults = '', Units = '', resultUnstructuredData = '%s' where id = '%s';", lab.getResultUnstructuredData(), lab.getId())));
        return fixSql;
    }

    private void queryLab(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<Lab> labs) {
        String querySql = String.format("select t1.id, t1.LabDataResults, t1.Units, t1.resultUnstructuredData, t2.Unique_code, t2.Name_eng, t2.Name_chs, CONCAT(IFNULL(resultUnstructuredData,''),REPLACE(t1.LabDataResults, '#',''), ' ', t2.Name_eng, '[', t2.Name_chs, ']') from tm_labdata t1, items t2 where t1.ReportId in (%s) and t1.is_deleted = 0 and t1.Units not in (select item_unique_code from e2b_r3_estri_item_config where element_number = 'F.r.3.3') and (t1.Units is not null and t1.Units != '') and t1.Units = t2.Unique_code", "'" + StringUtils.join(reportIds, "','") + "'");
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            Lab lab = new Lab();
            lab.setId(row.get(0));
            lab.setResultUnstructuredData(row.get(7));
            labs.add(lab);
        }
    }


    @Override
    public String fixField() {
        return "该选项不属于R3字典值，可将结果及单位填写至检查“结果/单位”字段中|实验室检查|结果单位（F.r.3.3）";
    }

    @Override
    public int getOrder() {
        return 13;
    }
}
