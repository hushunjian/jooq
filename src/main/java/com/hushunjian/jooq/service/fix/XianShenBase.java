package com.hushunjian.jooq.service.fix;

import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.XianShenReportNo;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public interface XianShenBase {

    /**
     * 获取修复的字段
     */
    String fixField();

    /**
     * 修复数据
     */
    List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate);

    /**
     * 顺序
     */
    int getOrder();
}
