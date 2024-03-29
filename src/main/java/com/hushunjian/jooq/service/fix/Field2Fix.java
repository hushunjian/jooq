package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Field2Fix implements XianShenBase {

    private static final Map<String, String> map = Maps.newHashMap();

    static {
        // 来自研究的报告-上市前境内报告
        map.put("f6a10042-94d0-4f85-9dd7-d4452f676e3b", "e9b3acd3-11af-11ea-8022-000c29ee981b");
        // 反馈数据-上市后境内报告
        map.put("5f4e1f7b-1169-4894-8ec6-90439d99e013", "e9b3ace4-11af-11ea-8022-000c29ee981b");
        // 自发报告-上市后境内报告
        map.put("289c1dc9-0ef7-4183-bf04-1dada7168228", "e9b3ace4-11af-11ea-8022-000c29ee981b");
    }

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，字段“报告分类（C.1.CN.2）“为空时，
        // 根据企业报告类型进行赋值：
        // -来自研究的报告：选择上市前境内报告
        // -反馈数据：选择上市后境内报告
        // -自发报告：选择上市后境内报告
        // -其他：选择上市后境内报告
        List<String> fixSql = Lists.newArrayList();
        // 查询这些报告下的数据
        handleReportNos.forEach(handleReportNo -> fixSql.add(String.format("update report_value set classify_of_report = '%s' where id = '%s';", map.get(handleReportNo.getReceivedFromId()), handleReportNo.getId())));
        return fixSql;
    }

    @Override
    public String fixField() {
        return "该字段为必填项，请补充完善该值|报告详情|报告分类（C.1.CN.2）";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
