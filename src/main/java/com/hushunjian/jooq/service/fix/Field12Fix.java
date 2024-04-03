package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.generator.tables.records.RelatedToCodeRecord;
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
public class Field12Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，关联报告模块，编号类型为“前系统编号”或“国家系统编号”，并且字段“备注”为空的记录，
        // 将此类记录中的“备注”字段补充填写NA
        List<String> fixSql = Lists.newArrayList();
        // 关联报告
        List<RelatedToCodeRecord> relatedToCodeRecords = Lists.newArrayList();
        // 查询报告中编号类型为“前系统编号”或“国家系统编号”，并且字段“备注”为空的记录，
        Lists.partition(handleReportNos, 200).forEach(partReportNos -> {
            // 查询关联报告
            queryRelatedCode(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, relatedToCodeRecords);
        });
        // 循环赋值
        relatedToCodeRecords.forEach(relatedToCodeRecord -> fixSql.add(String.format("update tm_relatedtocode set Remark = 'NA' where id = '%s';", relatedToCodeRecord.getId())));
        return fixSql;
    }

    private void queryRelatedCode(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<RelatedToCodeRecord> relatedToCodeRecords) {
        String querySql = String.format("select id, TypeItemId as 'type', Code, Remark from tm_relatedtocode where ReportId in (%s) and is_deleted = 0 and TypeItemId in ('7e34ea67-63a0-4f4a-9a5b-74e1bbc64340', 'a44894d1-45df-4fe3-9458-70173c41e238') and (Remark is null or Remark = '')", "'" + StringUtils.join(reportIds, "','") + "'");
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            RelatedToCodeRecord relatedToCodeRecord = new RelatedToCodeRecord();
            relatedToCodeRecord.setId(row.get(0));
            relatedToCodeRecord.setType(row.get(1));
            relatedToCodeRecord.setCode(row.get(2));
            relatedToCodeRecords.add(relatedToCodeRecord);
        }
    }


    @Override
    public String fixField() {
        return "【编号类型】为“前系统编号”“国家系统编号”，备注字段为必填。|关联报告|备注";
    }

    @Override
    public int getOrder() {
        return 12;
    }
}
