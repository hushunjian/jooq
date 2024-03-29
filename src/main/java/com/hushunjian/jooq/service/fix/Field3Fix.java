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
public class Field3Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中字段“加速报告”为空时，
        // 将字段“加速报告”的null flavor选择“NI”
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo -> fixSql.add(String.format("INSERT INTO `null_flavor_info`(`id`, `item_id`, `value`, `report_id`, `field_name`, `tenant_id`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`, `version`) VALUES ('%s', '%s', 'NI', '%s', 'fulfillexpeditecriteria', 'af632df0-3999-4a35-9c2d-7269b4ecc6a0', 'system', '2024-03-29 10:35:06.000000', NULL, NULL, 0, 0);", UUID.randomUUID(), handleReportNo.getId(), handleReportNo.getId())));
        return fixSql;
    }

    @Override
    public String fixField() {
        return "请填写加速报告|报告详情|加速报告(C.1.7）";
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
