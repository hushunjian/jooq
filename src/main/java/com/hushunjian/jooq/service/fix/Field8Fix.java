package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class Field8Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，字段“报告首位发送者”字段为空时，
        // 根据企业报告类型赋值：
        // -反馈数据：主管机构
        // -非反馈数据字典项：选择其他
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo ->
                fixSql.add(
                        String.format("update report_value set firstsender = '%s' where id = '%s';",
                                // 等于反馈数据 主管机构 非反馈数据字典项 其他
                                StringUtils.equals(handleReportNo.getReceivedFromId(), "5f4e1f7b-1169-4894-8ec6-90439d99e013") ? "3891ec54-624e-11e8-9882-000c29399d7c" : "3d10489c-624e-11e8-9882-000c29399d7c",
                                handleReportNo.getId())));
        return fixSql;
    }

    @Override
    public String fixField() {
        return "请选择报告首位发送者|报告详情|报告首位发送者（C.1.8.2）";
    }

    @Override
    public int getOrder() {
        return 8;
    }
}
