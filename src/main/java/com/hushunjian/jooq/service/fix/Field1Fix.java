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
public class Field1Fix implements XianShenBase {


    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，“作废/修正”有值，且字段“作废/修正的原因”为空时，将“因数据迁移补充该字段信息”补充至字段“作废/修正的原因”中
        // createnewversionreason 作废/修正原因 C.1.11.2
        // reportnullificationamendment 作废/修正 C.1.11.1
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo -> fixSql.add(String.format("update report_value set createnewversionreason = '因数据迁移补充该字段信息' where id = '%s';", handleReportNo.getId())));
        return fixSql;
    }

    @Override
    public String fixField() {
        return "请填写作废/修正的原因|报告详情|作废/修正的原因（C.1.11.2）";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
