package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.generator.tables.records.EventRecord;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field10Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，不良事件结果为空时，
        // 将不良事件结果补充选择”不详“
        List<String> fixSql = Lists.newArrayList();
        // 不良事件
        List<EventRecord> eventRecords = Lists.newArrayList();
        // 查询报告中不良事件结果为空的
        Lists.partition(handleReportNos, 200).forEach(partReportNos -> {
            // 查询不良事件
            queryEvent(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, eventRecords);
        });
        // 循环不良事件,设置为不详
        eventRecords.forEach(eventRecord ->
                fixSql.add(String.format("update tm_adverseevent set EventOutcome = '%s' where id = '%s';", "69270192-656e-4135-a32f-4aee5496462a", eventRecord.getId()))
        );
        return fixSql;
    }

    private void queryEvent(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<EventRecord> eventRecords) {
        String queryEventSql = String.format("select id, ReportId as 'report_id', EventTerm as 'event_name', EventOutcome as 'event_outcome' from tm_adverseevent where ReportId in (%s) and is_deleted = 0 and (EventOutcome is null or EventOutcome = '')", "'" + StringUtils.join(reportIds, "','") + "'");
        req.setSqlContent(queryEventSql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            EventRecord eventRecord = new EventRecord();
            eventRecord.setId(row.get(0));
            eventRecord.setReportId(row.get(1));
            eventRecord.setEventName(row.get(2));
            eventRecord.setEventOutcome(row.get(3));
            eventRecords.add(eventRecord);
        }
    }


    @Override
    public String fixField() {
        return "导出R3时不良事件的结果为必填|不良事件|不良事件的结果（E.i.7）";
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
