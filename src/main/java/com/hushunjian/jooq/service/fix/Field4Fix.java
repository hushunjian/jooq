package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Field4Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告分类为上市后境内报告或上市后境外报告时，并且字段“企业信息来源（C.1.CN.1）“为空时，
        // 根据企业报告类型赋值：
        // -反馈数据：选择监管机构
        // -非反馈数据字典项：选择其他
        List<String> fixSql = Lists.newArrayList();
        handleReportNos.forEach(handleReportNo ->
                fixSql.add(
                        String.format("update report_value set SourceInfoId = '%s' where id = '%s';",
                                // 反馈数据：选择监管机构 非反馈数据字典项：选择其他
                                StringUtils.equals(handleReportNo.getReceivedFromId(), "5f4e1f7b-1169-4894-8ec6-90439d99e013") ? "3d037301-9495-11e9-86d9-000c29ee981b" : "6b7b2dc4-657c-45cf-8936-904809b33105",
                                handleReportNo.getId())
                ));
        return fixSql;
    }

    @Override
    public String fixField() {
        return "当报告分类为上市后境内报告或上市后境外报告时，则该字段为必填，请补充完善该值|报告详情|企业信息来源（C.1.CN.1）";
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
