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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field15Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中实验室检查记录中，无检查日期时，
        // 将检查日期字段的null flavor处选择“UNK”
        List<String> fixSql = Lists.newArrayList();
        // 实验室检查
        List<Lab> labs = Lists.newArrayList();
        // 查询报告中实验室无检查日期时，
        Lists.partition(handleReportNos, 200).forEach(partReportNos -> {
            // 查询实验室检查
            queryLab(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, labs);
        });
        // 循环赋值
        labs.forEach(lab -> fixSql.add(String.format("INSERT INTO `null_flavor_info`(`id`, `item_id`, `value`, `report_id`, `field_name`, `tenant_id`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`, `version`) VALUES ('%s', '%s', 'UNK', '%s', 'startdate', 'af632df0-3999-4a35-9c2d-7269b4ecc6a0', 'system', '2024-03-29 10:35:06.000000', NULL, NULL, 0, 0);", UUID.randomUUID(), lab.getId(), lab.getReportId())));
        return fixSql;
    }

    private void queryLab(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<Lab> labs) {
        String querySql = String.format("select t1.id, t1.ReportId from tm_labdata t1 left join null_flavor_info t2 on t1.id = t2.item_id and t2.is_deleted = 0 where t1.ReportId in (%s) and t1.is_deleted = 0 and (t1.StartDate is null or t1.StartDate = '') and t2.id is null", "'" + StringUtils.join(reportIds, "','") + "'");
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            Lab lab = new Lab();
            lab.setId(row.get(0));
            lab.setReportId(row.get(1));
            labs.add(lab);
        }
    }


    @Override
    public String fixField() {
        return "请填写检查日期|实验室检查|检查日期（F.r.1）";
    }

    @Override
    public int getOrder() {
        return 15;
    }
}
