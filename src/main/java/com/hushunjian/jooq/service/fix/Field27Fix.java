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
public class Field27Fix implements XianShenBase {


    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 对于首次报告，首次获悉日期 = 收到最新信息日期
        // 对于随访报告，首次获悉日期 = 首次报告的 首次获悉日期
        List<String> fixSql = Lists.newArrayList();
        Map<Boolean, List<XianShenReportNo>> reportMap = Maps.newHashMap();
        // 区分首次跟随访
        handleReportNos.forEach(handleReportNo -> reportMap.computeIfAbsent(handleReportNo.getReportNo().contains("-"), v -> Lists.newArrayList()).add(handleReportNo));
        // 先处理首次
        if (reportMap.containsKey(false)) {
            // 首次
            reportMap.get(false).forEach(handleReportNo -> fixSql.add(String.format("update report_value set firstreceivedreportdate = Report_Receive_Date where id = '%s';", handleReportNo.getId())));
        }
        if (reportMap.containsKey(true)) {
            // 随访
            reportMap.get(true).forEach(handleReportNo -> fixSql.add(String.format("update report_value t1 join (select t2.Safetyreportid, t2.firstreceivedreportdate from report_value t2 where t2.Safetyreportid = '%s' and t2.tenant_id = 'af632df0-3999-4a35-9c2d-7269b4ecc6a0' and t2.InitialFollowId = 'ec63fd9c-b715-4571-96b8-ea43e562cb23') as t3 on t1.Safetyreportid = t3.Safetyreportid set t1.firstreceivedreportdate = t3.firstreceivedreportdate where t1.id = '%s';", handleReportNo.getReportNo().split("-")[0], handleReportNo.getId())));
        }
        return fixSql;
    }

    @Override
    public String fixField() {
        return "请填写首次报告获悉日期|报告详情|首次报告获悉日期（C.1.4）";
    }

    @Override
    public int getOrder() {
        return 27;
    }
}
