package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class Field17Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，患者年龄单位有值，且患者年龄为空时，
        // 将年龄单位（D.2.2b）的选项清空
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo -> fixSql.add(String.format("update report_value set AgeatTimeofOnsetunit = '' where id = '%s';", handleReportNo.getId())));
        return fixSql;
    }


    @Override
    public String fixField() {
        return "请填写患者年龄|患者信息|年龄（D.2.2a）";
    }

    @Override
    public int getOrder() {
        return 17;
    }
}
