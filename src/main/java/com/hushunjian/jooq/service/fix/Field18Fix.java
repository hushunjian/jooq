package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class Field18Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，“患者姓名”字段为空时，
        // 将患者姓名字段的null flavor选择“UNK”
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo -> fixSql.add(String.format("INSERT INTO `null_flavor_info`(`id`, `item_id`, `value`, `report_id`, `field_name`, `tenant_id`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`, `version`) VALUES ('%s', '%s', 'UNK', '%s', 'patientname', 'af632df0-3999-4a35-9c2d-7269b4ecc6a0', 'system', '2024-03-29 10:35:06.000000', NULL, NULL, 0, 0);", UUID.randomUUID(), handleReportNo.getId(), handleReportNo.getId())));
        return fixSql;
    }

    @Override
    public String fixField() {
        return "请填写患者姓名|患者信息|患者姓名（D.1）";
    }

    @Override
    public int getOrder() {
        return 18;
    }
}
