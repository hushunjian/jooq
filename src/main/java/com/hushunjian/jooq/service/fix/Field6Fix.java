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
public class Field6Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 批量赋值持有人标识符为固定id 8a8d812a8df484ea018e12c181376efd
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo ->
                fixSql.add(String.format("update report_value set mah_id = '8a8d812a8df484ea018e12c181376efd' where id = '%s';", handleReportNo.getId())));
        return fixSql;
    }

    @Override
    public String fixField() {
        return "当报告分类为上市后境内报告或上市后境外报告时，则该字段为必填，请补充完善该值|报告详情|持有人标识（C.1.CN.3）";
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
