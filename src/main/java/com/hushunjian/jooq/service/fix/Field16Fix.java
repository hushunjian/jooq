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
public class Field16Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中患者的现病史和既往病史为空，并且字段“相关病史及并发疾病说明（D.7.2）”也为空时，
        // 将字段“相关病史及并发疾病说明（D.7.2）”补充“无”
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo -> fixSql.add(String.format("update report_value set relevantmedicalhistory = '无' where id = '%s';", handleReportNo.getId())));
        return fixSql;
    }


    @Override
    public String fixField() {
        return "若患者无现病史与既往病史记录时，该字段为必填。若没有信息提供，可填写“不详”|患者信息|相关病史及并发疾病说明（D.7.2）";
    }

    @Override
    public int getOrder() {
        return 16;
    }
}
