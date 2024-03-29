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
public class Field7Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告的“首次/随访”字段为“首次”，并且报告的“收到报告日期”与“首次报告获悉日期”的值不相等时，
        // 将“收到报告日期”的值赋值到“首次报告获悉日期”
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo ->
                fixSql.add(String.format("update report_value set firstreceivedreportdate = Report_Receive_Date where id = '%s';", handleReportNo.getId())));
        return fixSql;
    }

    @Override
    public String fixField() {
        return "首次递交NMPA的报告，首次报告获悉日期与收到报告日期（即D0）需一致|报告详情|首次报告获悉日期（C.1.4）";
    }

    @Override
    public int getOrder() {
        return 7;
    }
}
